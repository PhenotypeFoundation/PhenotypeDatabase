/**
 * SimpleWizardController Controler
 *
 * Description of my controller
 *
 * @author  your email (+name?)
 * @since	2010mmdd
 * @package	???
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.studycapturing

import org.dbnp.gdt.*
import grails.plugins.springsecurity.Secured
import dbnp.authentication.SecUser
import dbnp.importer.ImportCell
import dbnp.importer.ImportRecord
import dbnp.importer.MappingColumn

@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class SimpleWizardController extends StudyWizardController {
	def authenticationService
	def fileService
	def importerService
	def gdtService

	/**
	 * index closure
	 */
	def index = {
		redirect( action: "study" );
	}

	/**
	 * Shows the study page
	 */
	def study = {
		// Retrieve the correct study
		Study study = getStudyInWizard( params );

		// If no study is found in the wizard, this is the entry page of the
		// wizard. Retrieve the study from request parameters
		if( !study ) {
			study = getStudyFromRequest( params );

			// Some error might have occurred during the retrieval of the study
			if( !study ) {
				redirect( controller: 'simpleWizard', action: 'study' );
				return;
			}

			session.simpleWizard = [ study: study ]
		}

		def event = getEvent(params);

		// If any event on this page is triggered, we should save the entered data.
		// If no event is triggered, the user gets here from another page. In that case,
		// we don't set the values
		if( event ) {
			// Only continue to the next or previous page if the information entered is correct
			if( handleStudy( study, params ) ) {
				// Now determine what action to perform
				// If the user clicks next, the study should be validated
				if( event == "next" && validateObject( study ) ) {
					if( study.samples?.size() ) {
						// This wizard can only handle simple studies. If the
						// study is too complex, this wizard doesn't provide the
						// possibility to edit samples/subjects etc.
						if( checkStudySimplicity( study ) ) {
							toPage( "existingSamples" );
						} else {
							toPage( "complexStudy" );
						}
					} else {
						toPage( "samples" );
					}
					
					return;
				}
			}
		}

		// Give the study to the user
		[ study: study ]
	}
	
	/**
	 * Shows a page to mention that the study being edited is too complex
	 */
	def complexStudy = {
		// Retrieve the correct study
		study = getStudyInWizard( params );
		if( !study ) {
			redirect( controller: 'simpleWizard', action: 'study' );
			return;
		}

		def event = getEvent(params);

		// If any event on this page is triggered
		if( event ) {
			// Now determine what action to perform
			if( event == "save" ) {
				toPage( "save" );
				return;
			} else if( event == "previous" ) {
				toPage( "study" );
				return;
			}
		}

		// Give the study and other data to the user
		[ study: study ]
	}

	/**
	 * Shows the samples page
	 */
	def existingSamples = {
		// Retrieve the correct study
		study = getStudyInWizard( params );
		if( !study ) {
			redirect( controller: 'simpleWizard', action: 'study' );
			return;
		}

		def event = getEvent(params);

		// If any event on this page is triggered, we should save the entered data.
		// If no event is triggered, the user gets here from another page. In that case,
		// we don't set the values
		if( event ) {
			// Now determine what action to perform
			if( event == "next" && handleExistingSamples( study, params )) {
				// Only continue to the next page if the information entered is correct
				toPage( "assays" );
				return;
			} else if( event == "previous" ) {
				// The user may go to the previous page, even if none of the data entered is OK.
				toPage( "study" );
				return;
			} else if( event == "update" && handleExistingSamples( study, params ) ) {
				// The user may update the samples using excel. Before that, the sample date should be saved
				toPage( "samples" );
				return;
			} else if( event == "skip" ) {
				// The user may skip the complete samples page
				toPage( "assays" );
				return;
			}
		}

		// Give the study and other data to the user
		def records = importerService.getRecords( study );

		[ study: study,
			records: records,
			templateCombinations: records.templateCombination.unique(),
			templates: [ 
				'Sample': Template.findAllByEntity( Sample.class ),
				'Subject': Template.findAllByEntity( Subject.class ),
				'Event': Template.findAllByEntity( Event.class ),
				'SamplingEvent': Template.findAllByEntity( SamplingEvent.class )
			],
			encodedEntity: [ 
				'Sample': gdtService.encryptEntity( Sample.class.name ), 
				'Subject': gdtService.encryptEntity( Subject.class.name ), 
				'Event': gdtService.encryptEntity( Event.class.name ), 
				'SamplingEvent': gdtService.encryptEntity( SamplingEvent.class.name )
			],
			existingSampleForm: session.simpleWizard.existingSampleForm 
		]
	}

	/**
	 * Shows the samples page
	 */
	def samples = {
		// Retrieve the correct study
		study = getStudyInWizard( params );
		if( !study ) {
			redirect( controller: 'simpleWizard', action: 'study' );
			return;
		}

		def event = getEvent(params);

		// If any event on this page is triggered, we should save the entered data.
		// If no event is triggered, the user gets here from another page. In that case,
		// we don't set the values
		if( event ) {
			// Now determine what action to perform
			if( event == "next" && handleSamples( study, params )) {
				// Only continue to the next page if the information entered is correct
				toPage( "columns" );
				return;
			} else if( event == "previous" ) {
				// The user may go to the previous page, even if none of the data entered is OK.
				if( study.samples?.size() )
					toPage( "existingSamples" );
				else
					toPage( "study" );

				return;
			} else if( event == "skip" ) {
				// The user may skip the complete samples page
				toPage( "assays" );
				return;
			}
		}

		// Give the study and other data to the user
		[ study: study, 
			templates: [ 
				'Sample': Template.findAllByEntity( Sample.class ),
				'Subject': Template.findAllByEntity( Subject.class ),
				'Event': Template.findAllByEntity( Event.class ),
				'SamplingEvent': Template.findAllByEntity( SamplingEvent.class )
			],
			encodedEntity: [ 
				'Sample': gdtService.encryptEntity( Sample.class.name ), 
				'Subject': gdtService.encryptEntity( Subject.class.name ), 
				'Event': gdtService.encryptEntity( Event.class.name ), 
				'SamplingEvent': gdtService.encryptEntity( SamplingEvent.class.name )
			],
			sampleForm: session.simpleWizard.sampleForm ]
	}

	/**
	 * Shows the columns page
	 */
	def columns = {
		// Retrieve the correct study
		study = getStudyInWizard( params );
		if( !study ) {
			redirect( controller: 'simpleWizard', action: 'study' );
			return;
		}

		def event = getEvent(params);

		// If any event on this page is triggered, we should save the entered data.
		// If no event is triggered, the user gets here from another page. In that case,
		// we don't set the values
		if( event ) {
			// Now determine what action to perform
			if( event == "next" && handleColumns( study, params ) ) {
				
				// Only continue to the next page if the information entered is correct
				if( session.simpleWizard.imported.numInvalidEntities > 0 ) {
					toPage( "missingFields" );
				} else {
					// The import of the excel file has finished. Now delete the excelfile
					if( session.simpleWizard.sampleForm.importFile )
						fileService.delete( session.simpleWizard.sampleForm.importFile );

					session.simpleWizard.sampleForm = null

					toPage( "assays" );
				}
				return;
			} else if( event == "previous" ) {
				// The user may go to the previous page, even if the data is not correct
				toPage( "samples" );
				return;
			}
		}
		
		def templates = [:]; 
		def domainFields = [:]

		session.simpleWizard.sampleForm.templateId.each { 
			templates[ it.key ] = it.value ? Template.get( it.value ) : null;
			if( it.value ) {
				domainFields[ it.key ] = templates[ it.key ].entity.giveDomainFields();
			}
		} 
		
		// Give the study and other data to the user
		[ study: study,
					filename: session.simpleWizard.sampleForm.importFile,
					templates: templates,
					domainFields: domainFields,
					excel: session.simpleWizard.excel]
	}

	/**
	 * Shows the page where missing fields can be filled in
	 */
	def missingFields = {
		// Retrieve the correct study
		study = getStudyInWizard( params );
		if( !study ) {
			redirect( controller: 'simpleWizard', action: 'study' );
			return;
		}

		def event = getEvent(params);

		// If any event on this page is triggered, we should save the entered data.
		// If no event is triggered, the user gets here from another page. In that case,
		// we don't set the values
		if( event ) {
			// Now determine what action to perform
			if( event == "next" && handleMissingFields( study, params ) ) {
				if( session.simpleWizard.imported.numInvalidEntities == 0 ) {
					// Only continue to the next page if the information entered is correct

					// The import of the excel file has finished. Now delete the excelfile
					if( session.simpleWizard.sampleForm.importFile ) {
						fileService.delete( session.simpleWizard.sampleForm.importFile );
					}
					session.simpleWizard.sampleForm = null

					toPage( "assays" );
					return;
				}
			} else if( event == "previous" ) {
				// THe user may go to the previous page, even if the data is not correct
				toPage( "columns" );
				return;
			}
		}

		// If any errors have occurred during validation, show them to the user. However,
		// the same error might have occurred for multiple entities. For that reason,
		// we only show unique errors
		def rules
		if( session.simpleWizard.imported.errors ) {
			rules = session.simpleWizard.imported.errors*.values().flatten().unique().join( "<br /> \n" );
		}

		// Create the right format for showing the data
		def records = [];
		session.simpleWizard.imported.data.each { row ->
			def record = [:];
			row.each { object ->
				// Attach template to session
				if( object.template )
					attachTemplate( object.template );
				
				def entityName = object.class.name[ object.class.name.lastIndexOf( '.' ) + 1 .. -1 ]
				record[ entityName ] = object;
			}
			records << record;
		}
		
		// Give the study and other data to the user
		println "Imported: " + session.simpleWizard.imported.failedCells;
		
		[ study: study, imported: session.simpleWizard.imported, records: records, rules: rules ]
	}

	/**
	 * Shows the assay page
	 */
	def assays = {
		// Retrieve the correct study
		Study study = getStudyInWizard( params );
		if( !study ) {
			redirect( controller: 'simpleWizard', action: 'study' );
			return;
		}

		Assay assay = getAssayInWizard( study );

		def event = getEvent(params);

		// If any event on this page is triggered, we should save the entered data.
		// If no event is triggered, the user gets here from another page. In that case,
		// we don't set the values
		if( event ) {
			// Only continue to the next or previous page if the information entered is correct
			if( event == "skip" ) {
				// The user may skip the complete assay page

				// In case the user has created an assay before, it should only be kept if it
				// existed before this step
				if( session.simpleWizard.assay != null && !session.simpleWizard.assay.id ) {
					session.simpleWizard.remove( "assay" )
				}

				toPage( "overview" );
				return;
			} else if( handleAssays( assay, params ) ) {
				// Now determine what action to perform
				if( event == "next" && validateObject( assay ) ) {
					toPage( "overview" );
					return;
				} else if( event == "previous" ) {
					if( study.samples?.size() )
						toPage( "existingSamples" )
					else
						toPage( "samples" );

					return;
				}
			}
		}

		// Give the study to the user
		[ study: study, wizardAssay: assay ]
	}

	/**
	 * Shows an overview of the entered study
	 */
	def overview = {
		// Retrieve the correct study
		Study study = getStudyInWizard( params );
		if( !study ) {
			redirect( controller: 'simpleWizard', action: 'study' );
			return;
		}

		Assay assay = getAssayInWizard();

		def event = getEvent(params);

		// If any event on this page is triggered, we should save the entered data.
		// If no event is triggered, the user gets here from another page. In that case,
		// we don't set the values
		if( event ) {
			// Now determine what action to perform
			if( event == "save" ) {
				toPage( "save" );
				return;
			} else if( event == "previous" ) {
				toPage( "assays" )
				return;
			}
		}

		// Give the study to the user
		[ study: study, wizardAssay: assay ]
	}

	def save = {
		// Retrieve the correct study
		Study study = getStudyInWizard( params );
		if( !study ) {
			redirect( controller: 'simpleWizard', action: 'study' );
			return;
		}

		def event = getEvent(params);

		if( event && event == "previous" ) {
			toPage( "assays" );
			return;
		}
		
		Assay newAssay = getAssayInWizard();

		if( newAssay && !study.assays?.contains( newAssay ) ) {
			study.addToAssays( newAssay );
		}

		//attachAndValidateEntities( study );
		
		// Save the study. The study must be merged if it is loaded from
		// the database before, since otherwise it will raise 'lazy initialization' errors
		// The validation is done in the other steps, so it is skipped here.
		def success
		
		if(
		study.id ?
		study.merge( validate: false, flush: true ) :
		study.save( flush: true )
		) {
			// Make sure all samples are attached to all assays
			study.assays.each { assay ->
				def l = []+ assay.samples;
				l.each { sample ->
					if( sample )
						assay.removeFromSamples( sample );
				}
				assay.samples?.clear();

				study.samples.each { sample ->
					assay.addToSamples( sample )
				}
			}

			// Clear session
			session.simpleWizard = null;

			flash.message = "Your study is succesfully saved.";
			success = true
		} else {
			flash.error = "An error occurred while saving your study";

			study.getErrors().each { println it }

			// Remove the assay from the study again, since it is still available
			// in the session
			if( newAssay ) {
				study.removeFromAssays( newAssay );
				newAssay.parent = study;
			}

			//validateObject( study );
			success = false
		}

		// Give the study to the user
		[ study: study, success: success ]
	}

	/** 
	 * Handles study input
	 * @param study		Study to update
	 * @param params	Request parameter map
	 * @return			True if everything went OK, false otherwise. An error message is put in flash.error
	 */
	def handleStudy( study, params ) {
		// did the study template change?
		if (params.get('template') && study.template?.name != params.get('template')) {
			// set the template
			study.template = Template.findByName(params.remove('template'))
		}

		// does the study have a template set?
		if (study.template && study.template instanceof Template) {
			// yes, iterate through template fields
			study.giveFields().each() {
				// and set their values
				study.setFieldValue(it.name, params.get(it.escapedName()))
			}
		}

		// handle public checkbox
		if (params.get("publicstudy")) {
			study.publicstudy = params.get("publicstudy")
		}

		// handle publications
		handleStudyPublications(study, params)

		// handle contacts
		handleStudyContacts(study, params)

		// handle users (readers, writers)
		handleStudyUsers(study, params, 'readers')
		handleStudyUsers(study, params, 'writers')

		return true
	}

	/**
	 * Handles the editing of existing samples
	 * @param study		Study to update
	 * @param params		Request parameter map
	 * @return			True if everything went OK, false otherwise. An error message is put in flash.error
	 */
	def handleExistingSamples( study, params ) {
		session.simpleWizard.existingSampleForm = params
		flash.validationErrors = [:];

		def errors = false;
		
		// iterate through objects; set field values and validate the object
		def eventgroups = study.samples.parentEventGroup.findAll { it }
		def events;
		if( !eventgroups )
			events = []
		else
			events = eventgroups.events?.getAt(0);
		
		def objects = [ 
			'Sample': study.samples, 
			'Subject': study.samples.parentSubject.findAll { it }, 
			'SamplingEvent': study.samples.parentEvent.findAll { it }, 
			'Event': events.flatten().findAll { it }
		]; 	
		objects.each {
			def type = it.key;
			def entities = it.value;
			
			entities.each { entity ->
				// iterate through entity fields
				entity.giveFields().each() { field ->
					def value = params.get( type.toLowerCase() + '_' + entity.getIdentifier() + '_' + field.escapedName())

					// set field value; name cannot be set to an empty value
					if (field.name != 'name' || value) {
						log.info "setting "+field.name+" to "+value
						entity.setFieldValue(field.name, value)
					}
				}
				
				// has the template changed? 
				def templateName = params.get(type.toLowerCase() + '_' + entity.getIdentifier() + '_template')
				if (templateName && entity.template?.name != templateName) {
					entity.template = Template.findByName(templateName)
				}
	
				// validate sample
				if (!entity.validate()) {
					errors = true;
					flash.validationErrors << getHumanReadableErrors( entity )
				}
			}
		}

		return !errors
	}

	/**
	 * Handles the upload of sample data
	 * @param study		Study to update
	 * @param params	Request parameter map
	 * @return			True if everything went OK, false otherwise. An error message is put in flash.error
	 */
	def handleSamples( study, params ) {
		def filename = params.get( 'importfile' );

		// Handle 'existing*' in front of the filename. This is put in front to make a distinction between
		// an already uploaded file test.txt (maybe moved to some other directory) and a newly uploaded file test.txt
		// still being in the temporary directory.
		// This import step doesn't have to make that distinction, since all files remain in the temporary directory.
		if( filename == 'existing*' )
			filename = '';
		else if( filename[0..8] == 'existing*' )
			filename = filename[9..-1]
		
		def sampleTemplateId  = params.long( 'sample_template_id' )
		def subjectTemplateId  = params.long( 'subject_template_id' )
		def eventTemplateId  = params.long( 'event_template_id' )
		def samplingEventTemplateId  = params.long( 'samplingEvent_template_id' )
		
		// These fields have been removed from the form, so will always contain
		// their default value. The code however remains like this for future use.
		int sheetIndex = (params.int( 'sheetindex' ) ?: 1 )
		int dataMatrixStart = (params.int( 'datamatrix_start' ) ?: 2 )
		int headerRow = (params.int( 'headerrow' ) ?: 1 )

		// Save form data in session
		session.simpleWizard.sampleForm = [
					importFile: filename,
					templateId: [ 
						'Sample': sampleTemplateId,
						'Subject': subjectTemplateId,
						'Event': eventTemplateId,
						'SampingEvent': samplingEventTemplateId
					],
					sheetIndex: sheetIndex,
					dataMatrixStart: dataMatrixStart,
					headerRow: headerRow
				];

		// Check whether the template exists
		if (!sampleTemplateId || !Template.get( sampleTemplateId ) ){
			log.error ".simple study wizard not all fields are filled in: " + sampleTemplateId
			flash.error = "No template was chosen. Please choose a template for the samples you provided."
			return false
		}

		def importedfile = fileService.get( filename )
		def workbook
		if (importedfile.exists()) {
			try {
				workbook = importerService.getWorkbook(new FileInputStream(importedfile))
			} catch (Exception e) {
				log.error ".simple study wizard could not load file: " + e
				flash.error = "The given file doesn't seem to be an excel file. Please provide an excel file for entering samples.";
				return false
			}
		} else {
			log.error ".simple study wizard no file given";
			flash.error = "No file was given. Please provide an excel file for entering samples.";
			return false;
		}

		if( !workbook ) {
			log.error ".simple study wizard could not load file into a workbook"
			flash.error = "The given file doesn't seem to be an excel file. Please provide an excel file for entering samples.";
			return false
		}

		def selectedentities = []

		if( !excelChecks( workbook, sheetIndex, headerRow, dataMatrixStart ) )
			return false;

		// Get the header from the Excel file using the arguments given in the first step of the wizard
		def importerHeader;
		def importerDataMatrix;

		try {
			importerHeader = importerService.getHeader(workbook,
					sheetIndex - 1, 		// 0 == first sheet
					headerRow,				// 1 == first row :s
					dataMatrixStart - 1, 	// 0 == first row
					Sample.class)

			importerDataMatrix = importerService.getDatamatrix(
					workbook,
					importerHeader,
					sheetIndex - 1, 		// 0 == first sheet
					dataMatrixStart - 1, 	// 0 == first row
					5)
		} catch( Exception e ) {
			// An error occurred while reading the excel file.
			log.error ".simple study wizard error while reading the excel file";
			e.printStackTrace();

			// Show a message to the user
			flash.error = "An error occurred while reading the excel file. Have you provided the right sheet number and row numbers. Contact your system administrator if this problem persists.";
			return false;
		}

		// Save read excel data into session
		session.simpleWizard.excel = [
					workbook: workbook,
					sheetIndex: sheetIndex,
					dataMatrixStart: dataMatrixStart,
					headerRow: headerRow,
					data: [
						header: importerHeader,
						dataMatrix: importerDataMatrix
					]
				]

		return true
	}

	/**
	 * Handles the matching of template fields with excel columns by the user
	 * @param study		Study to update
	 * @param params	Request parameter map
	 * @return			True if everything went OK, false otherwise. An error message is put in flash.error
	 * 					The field session.simpleWizard.imported.numInvalidEntities reflects the number of 
	 * 					entities that have errors, and should be fixed before saving. The errors for those entities
	 * 					are saved into session.simpleWizard.imported.errors
	 */
	def handleColumns( study, params ) {
		// Find actual Template object from the chosen template name
		def templates = [:];
		session.simpleWizard.sampleForm.templateId.each {
			templates[ it.key ] = it.value ? Template.get( it.value ) : null;
		}
		
		def headers = session.simpleWizard.excel.data.header;

		if( !params.matches ) {
			log.error( ".simple study wizard no column matches given" );
			flash.error = "No column matches given";
			return false;
		}

		// Retrieve the chosen matches from the request parameters and put them into
		// the headers-structure, for later reference
		params.matches.index.each { columnindex, value ->
			// Determine the entity and property by splitting it
			def parts = value.toString().tokenize( "||" );
			
			def property
			def entityName
			if( parts.size() > 1 ) {
				property = parts[ 1 ];
				entityName = "dbnp.studycapturing." + parts[ 0 ];
			} else if( parts.size() == 1 ) {
				property = parts[ 0 ];
				entityName = headers[columnindex.toInteger()].entityclass.getName();
			}
			
			// Create an actual class instance of the selected entity with the selected template
			// This should be inside the closure because in some cases in the advanced importer, the fields can have different target entities
			def entityClass = Class.forName( entityName, true, this.getClass().getClassLoader())
			def entityObj = entityClass.newInstance(template: templates[ entityName[entityName.lastIndexOf( '.' ) + 1..-1] ])

			headers[ columnindex.toInteger() ].entityclass = entityClass
			
			// Store the selected property for this column into the column map for the ImporterService
			headers[columnindex.toInteger()].property = property

			// Look up the template field type of the target TemplateField and store it also in the map
			headers[columnindex.toInteger()].templatefieldtype = entityObj.giveFieldType(property)

			// Is a "Don't import" property assigned to the column?
			headers[columnindex.toInteger()].dontimport = (property == "dontimport") ? true : false

			//if it's an identifier set the mapping column true or false
			entityClass.giveDomainFields().each {
				headers[columnindex.toInteger()].identifier = ( it.preferredIdentifier && (it.name == property) )
			}
		}

		// Import the workbook and store the table with entity records and store the failed cells
		println "Importing samples for study " + study + " (" + study.id + ")";
		
		def imported = importerService.importOrUpdateDataBySampleIdentifier(templates,
				session.simpleWizard.excel.workbook,
				session.simpleWizard.excel.sheetIndex - 1,
				session.simpleWizard.excel.dataMatrixStart - 1,
				session.simpleWizard.excel.data.header,
				study,
				true			// Also create entities for which no data is imported but where templates were chosen 
		);

		def table = imported.table
		def failedcells = imported.failedCells

		session.simpleWizard.imported = [
			data: table,
			failedCells: failedcells
		];

		// loop through all entities to validate them and add them to failedcells if an error occurs
		def numInvalidEntities = 0;
		def errors = [];

		// Add all samples
		table.each { record ->
			record.each { entity ->
				if( entity ) {
					// Determine entity class and add a parent
					def preferredIdentifier = importerService.givePreferredIdentifier( entity.class );
					def equalClosure = { it.getFieldValue( preferredIdentifier.name ) == entity.getFieldValue( preferredIdentifier.name ) }
	
					entity.parent = study
					
					switch( entity.class ) {
						case Sample:
							if( preferredIdentifier && !study.samples?.find( equalClosure ) ) {
								study.addToSamples( entity );
							}
							break;
						case Subject:
							if( preferredIdentifier && !study.subjects?.find( equalClosure ) ) {
								study.addToSubjects( entity );
							}
							break;
						case Event:
							if( preferredIdentifier && !study.events?.find( equalClosure ) ) {
								study.addToEvents( entity );
							}
							break;
						case SamplingEvent:
							if( preferredIdentifier && !study.samplingEvents?.find( equalClosure ) ) {
								study.addToSamplingEvents( entity );
							}
							break;
					}
					
					if (!entity.validate()) {
						numInvalidEntities++;
						
						// Add this field to the list of failed cells, in order to give the user feedback
						failedcells = addNonValidatingCells( failedcells, entity )
	
						// Also create a full list of errors
						errors += getHumanReadableErrors( entity );
					}
				}
			}
		}

		session.simpleWizard.imported.numInvalidEntities = numInvalidEntities + failedcells?.size();
		session.simpleWizard.imported.errors = errors;

		return true
	}

	/**
	 * Handles the update of the edited fields by the user
	 * @param study		Study to update
	 * @param params		Request parameter map
	 * @return			True if everything went OK, false otherwise. An error message is put in flash.error.
	 * 					The field session.simpleWizard.imported.numInvalidEntities reflects the number of
	 * 					entities that still have errors, and should be fixed before saving. The errors for those entities
	 * 					are saved into session.simpleWizard.imported.errors
	 */
	def handleMissingFields( study, params ) {
		def numInvalidEntities = 0;
		def errors = [];

		// Check which fields failed previously
		def failedCells = session.simpleWizard.imported.failedCells

		session.simpleWizard.imported.data.each { table ->
			table.each { entity ->
				def invalidFields = 0

				// Set the fields for this entity by retrieving values from the params
				entity.giveFields().each { field ->
					def fieldName = importerService.getFieldNameInTableEditor( entity, field );

					if( params[ fieldName ] == "#invalidterm" ) {
						// If the value '#invalidterm' is chosen, the user hasn't fixed anything, so this field is still incorrect
						invalidFields++;
					} else {
						if( field.type == org.dbnp.gdt.TemplateFieldType.ONTOLOGYTERM || field.type == org.dbnp.gdt.TemplateFieldType.STRINGLIST ) {
							// If this field is an ontologyterm field or a stringlist field, the value has changed, so remove the field from
							// the failedCells list
							importerService.removeFailedCell( failedCells, entity, field )
						}

						// Update the field, regardless of the type of field
						entity.setFieldValue(field.name, params[ fieldName ] )
					}
				}

				// Determine entity class and add a parent
				entity.parent = study;

				// Try to validate the entity now all fields have been set. If it fails, return an error
				if (!entity.validate() || invalidFields) {
					numInvalidEntities++;

					// Add this field to the list of failed cells, in order to give the user feedback
					failedCells = addNonValidatingCells( failedCells, entity )

					// Also create a full list of errors
					errors += getHumanReadableErrors( entity );
				} else {
					importerService.removeFailedCell( failedCells, entity )
				}
			} // end of record
		} // end of table

		session.simpleWizard.imported.numInvalidEntities = numInvalidEntities;
		session.simpleWizard.imported.errors = errors;

		return true
	}

	/**
	 * Handles assay input
	 * @param study		Study to update
	 * @param params		Request parameter map
	 * @return			True if everything went OK, false otherwise. An error message is put in flash.error
	 */
	def handleAssays( assay, params ) {
		// did the study template change?
		if (params.get('template') && assay.template?.name != params.get('template')) {
			// set the template
			assay.template = Template.findByName(params.remove('template'))
		}

		// does the study have a template set?
		if (assay.template && assay.template instanceof Template) {
			// yes, iterate through template fields
			assay.giveFields().each() {
				// and set their values
				assay.setFieldValue(it.name, params.get(it.escapedName()))
			}
		}

		// Save the assay in session
		session.simpleWizard.assay = assay;

		return true
	}
	
	/**
	 * Checks whether the given study is simple enough to be edited using this controller.
	 * 
	 * The study is simple enough if the samples, subjects, events and samplingEvents can be 
	 * edited as a flat table. That is: 
	 * 		- Every subject belongs to 0 or 1 eventgroup
	 * 		- Every eventgroup belongs to 0 or 1 sample
	 * 		- Every eventgroup has 0 or 1 subjects, 0 or 1 event and 0 or 1 samplingEvents
	 * 		- If a sample belongs to an eventgroup:
	 * 			- If that eventgroup has a samplingEvent, that same samplingEvent must also be 
	 * 				the sampling event that generated this sample
	 * 			- If that eventgroup has a subject, that same subject must also be the subject
	 * 				from whom the sample was taken
	 * 
	 * @param study		Study to check
	 * @return			True if the study can be edited by this controller, false otherwise
	 */
	def checkStudySimplicity( study ) {
		def simplicity = true;
		
		if( !study )
			return false

		if( study.eventGroups ) {
			study.eventGroups.each { eventGroup ->
				// Check for simplicity of eventgroups: only 0 or 1 subject, 0 or 1 event and 0 or 1 samplingEvent
				if( eventGroup.subjects?.size() > 1 || eventGroup.events?.size() > 1 || eventGroup.samplingEvents?.size() > 1 ) {
					flash.message = "One or more eventgroups contain multiple subjects or events."
					simplicity = false;
				}
				
				// Check whether this eventgroup only belongs to (max) 1 sample
				def numSamples = 0;
				study.samples.each { sample ->
					// If no id is given for the eventGroup, it has been entered in this wizard, but 
					// not yet saved. In that case, it is always OK
					if( eventGroup.id && sample.parentEventGroup?.id == eventGroup.id )
						numSamples++;
				}
				
				if( numSamples > 1 ) { 
					flash.message = "One or more eventgroups belong to multiple samples."
					simplicity = false;
				}
			}
			
			if( !simplicity ) return false;

			// Check whether subject only belong to zero or one event group
			if( study.subjects ) {
				study.subjects.each { subject ->
					def numEventGroups = 0
					study.eventGroups.each { eventGroup ->
						// If no id is given for the subject, it has been entered in this wizard, but
						// not yet saved. In that case, it is always OK
						if( subject.id && eventGroup.subjects[0]?.id == subject.id )
							numEventGroups++
					}
					
					if( numEventGroups > 1 ) {
						flash.message = "One or more subjects belong to multiple eventgroups."
						simplicity = false;
					}
				}
			}

			if( !simplicity ) return false;
			
			// Check whether the samples that belong to an eventgroup have the right parentObjects
			study.samples.each { sample ->
				if( sample.parentEventGroup ) {
					// If no id is given for the subject, it has been entered in this wizard, but
					// not yet saved. In that case, it is always OK
					if( sample.parentSubject && sample.parentSubject.id) {
						if( !sample.parentEventGroup.subjects || sample.parentEventGroup.subjects[0]?.id != sample.parentSubject.id ) {
							flash.message = "The structure of the eventgroups of one or more samples is too complex"
							simplicity = false;
						}
					}
					
					// If no id is given for the sampling event, it has been entered in this wizard, but
					// not yet saved. In that case, it is always OK
					if( sample.parentEvent && sample.parentEvent.id) {
						if( !sample.parentEventGroup.samplingEvents || sample.parentEventGroup.samplingEvents[0]?.id != sample.parentEvent.id ) {
							flash.message = "The structure of the eventgroups of one or more samples is too complex"
							simplicity = false;
						}
					}
				}
			}
			
			if( !simplicity ) return false;
		}
		
		return simplicity;
	}

	def attachAndValidateEntities( def study ) {
		if( !session.simpleWizard?.imported?.data ) 
			return
			 
		def table = session.simpleWizard.imported.data
		def numInvalidEntities = 0;
		
		// Add all samples
		table.each { record ->
			println record*.class
			record.each { entity ->
				if( entity ) {
					if( entity.validate() ) {
						println "Saving: " + entity + " (" + entity.class.name + ")"
						println study.samples;
						println study.subjects;
						
						// Determine entity class and add a parent
						def preferredIdentifier = importerService.givePreferredIdentifier( entity.class );
						def equalClosure = { it.getFieldValue( preferredIdentifier.name ) == entity.getFieldValue( preferredIdentifier.name ) }
		
						//entity.parent = study
						switch( entity.class ) {
							case Sample:
								if( preferredIdentifier && !study.samples?.find( equalClosure ) ) {
									study.addToSamples( entity );
								}
								break;
							case Subject:
								if( preferredIdentifier && !study.subjects?.find( equalClosure ) ) {
									study.addToSubjects( entity );
								}
								break;
							case Event:
								if( preferredIdentifier && !study.events?.find( equalClosure ) ) {
									study.addToEvents( entity );
								}
								break;
							case SamplingEvent:
								if( preferredIdentifier && !study.samplingEvents?.find( equalClosure ) ) {
									study.addToSamplingEvents( entity );
								}
								break;
						}
						
						entity.save();
						
					} else {
						numInvalidEntities++;
					}
				}
			}
		}

		return numInvalidEntities == 0;
	}
		
	/**
	 * Validates an object and puts human readable errors in validationErrors variable
	 * @param entity		Entity to validate
	 * @return			True iff the entity validates, false otherwise
	 */
	protected boolean validateObject( def entity ) {
		if( !entity.validate() ) {
			flash.validationErrors = getHumanReadableErrors( entity )
			return false;
		}
		return true;
	}

	/**
	 * Adds all fields of this entity that have given an error when validating to the failedcells list
	 * @param failedcells	Current list of ImportRecords
	 * @param entity		Entity to check. The entity must have been validated before
	 * @return				Updated list of ImportRecords
	 */
	protected def addNonValidatingCells( failedcells, entity ) {
		// Add this entity and the fields with an error to the failedCells list
		ImportRecord failedRecord = new ImportRecord();

		entity.getErrors().getFieldErrors().each { error ->
			String field = error.getField();
			
			def mc = importerService.findMappingColumn( session.simpleWizard.excel.data.header, field );
			def mcInstance = new MappingColumn( name: field, entityClass: Sample.class, index: -1, property: field.toLowerCase(), templateFieldType: entity.giveFieldType( field ) );

			// Create a clone of the mapping column
			if( mc ) {
				mcInstance.properties = mc.properties
			}

			failedRecord.addToImportcells( new ImportCell(mappingcolumn: mcInstance, value: error.getRejectedValue(), entityidentifier: importerService.getFieldNameInTableEditor( entity, field ) ) )
		}
		failedcells.add( failedRecord );

		return failedcells
	}


	/**
	 * Checks an excel workbook whether the given sheetindex and rownumbers are correct	
	 * @param workbook			Excel workbook to read
	 * @param sheetIndex		1-based sheet index for the sheet to read (1=first sheet)
	 * @param headerRow			1-based row number for the header row (1=first row)
	 * @param dataMatrixStart	1-based row number for the first data row (1=first row)
	 * @return					True if the sheet index and row numbers are correct.
	 */
	protected boolean excelChecks( def workbook, int sheetIndex, int headerRow, int dataMatrixStart ) {
		// Perform some basic checks on the excel file. These checks should be performed by the importerservice
		// in a perfect scenario.
		if( sheetIndex > workbook.getNumberOfSheets() ) {
			log.error ".simple study wizard Sheet index is too high: " + sheetIndex + " / " + workbook.getNumberOfSheets();
			flash.error = "Your excel sheet contains too few excel sheets. The provided excel sheet has only " + workbook.getNumberOfSheets() + " sheet(s).";
			return false
		}

		def sheet = workbook.getSheetAt(sheetIndex - 1);
		def firstRowNum = sheet.getFirstRowNum();
		def lastRowNum = sheet.getLastRowNum();
		def numRows = lastRowNum - firstRowNum + 1;

		if( headerRow > numRows  ) {
			log.error ".simple study wizard Header row number is incorrect: " + headerRow + " / " + numRows;
			flash.error = "Your excel sheet doesn't contain enough rows (" + numRows + "). Please provide an excel sheet with one header row and data below";
			return false
		}

		if( dataMatrixStart > numRows  ) {
			log.error ".simple study wizard Data row number is incorrect: " + dataMatrixStart + " / " + numRows;
			flash.error = "Your excel sheet doesn't contain enough rows (" + numRows + "). Please provide an excel sheet with one header row and data below";
			return false
		}

		return true;
	}

	/**
	 * Redirects the user to the page with the given name
	 * @param action
	 */
	protected void toPage( String action ) {
		println "Redirecting to: " + action;
		redirect( action: action, params: [ "wizard": true ] );
	}

	/**
	 * Returns the event that is specified by the user form
	 * @param params
	 * @return
	 */
	protected String getEvent( def params ) {
		return params.get( 'event' );
	}

	/**
	 * Retrieves the required study from the database or return an empty Study object if 
	 * no id is given
	 * 
	 * @param params	Request parameters with params.id being the ID of the study to be retrieved
	 * @return			A study from the database or an empty study if no id was given
	 */
	protected Study getStudyFromRequest( def params ) {
		int id = params.int( "id" );

		if( !id ) {
			return new Study( title: "New study", owner: authenticationService.getLoggedInUser() );
		}

		Study s = Study.get( id );

		if( !s ) {
			flash.error = "No study found with given id";
			return null;
		}
		if( !s.canWrite( authenticationService.getLoggedInUser() ) ) {
			flash.error = "No authorization to edit this study."
			return null;
		}

		return s
	}

	/**
	 * Attach a template to the session
	 * @param t
	 * @return
	 */
	protected attachTemplate( Template t ) {
		if( t && !t.isAttached() ) {
			t.attach();

			t.fields.each { field ->
				if( field && !field.isAttached() )
					field.attach();
	
				field.listEntries?.each { entry ->
					if( entry && !entry.isAttached() )
						entry.attach();
				}
				field.ontologies?.each { entry ->
					if( entry && !entry.isAttached() )
						entry.attach();
				}
			}
		}
	}
	
	/**
	 * Retrieves the study that is saved in the wizard, 
	 * 
	 * @param params	Request parameters
	 * @return			The found study object, or null if no study object is found
	 */
	protected Study getStudyInWizard( def params ) {
		if( params.wizard && session.simpleWizard && session.simpleWizard.study ) {
			// The user came here by clicking previous or a link on another page. Use the existing study
			Study s = session.simpleWizard.study;

			if( s.id && !s.isAttached() ) {
				s.attach();
			}
			
			s.samples?.each {
				if( it && it.id && !it.isAttached() )
					it.attach();
					 
				attachTemplate( it.template )
			}
			s.subjects?.each { 
				if( it && it.id && !it.isAttached() )
					it.attach();
					 
				attachTemplate( it.template )
			}
			s.events?.each { 
				if( it && it.id && !it.isAttached() )
					it.attach();
					 
				attachTemplate( it.template )
			}

			s.samplingEvents?.each {
				if( it && it.id && !it.isAttached() )
					it.attach();
					 
				attachTemplate( it.template )
			}
			s.assays?.each { 
				if( it && it.id && !it.isAttached() )
					it.attach();
					 
				attachTemplate( it.template )
			}

			return s;
		} else {
			// The user didn't get here from the wizard or no study is found
			return null;
		}
	}

	/**
	 * Retrieves the assay to edit in the wizard
	 * @param s		Study that the assay will be in
	 * @return		Assay object (may be empty)
	 */
	protected Assay getAssayInWizard( Study study = null ) {
		if( session.simpleWizard && session.simpleWizard.assay ) {
			Assay a = session.simpleWizard.assay;

			if( a.id && !a.isAttached() ) {
				a.attach();

				attachTemplate( a.template );
			}

			return a;
		} else if( study ) {
			// The user came on the assay page for the first time
			if( study.assays?.size() ) {
				def assay = study.assays[0];

				return assay;
			} else {
				return new Assay( parent: study );
			}
		} else {
			return null;
		}
	}

	/**
	 * transform domain class validation errors into a human readable
	 * linked hash map
	 * @param object validated domain class
	 * @return object  linkedHashMap
	 */
	def getHumanReadableErrors(object) {
		def errors = [:]
		object.errors.getAllErrors().each() { error ->
			// error.codes.each() { code -> println code }

			// generally speaking g.message(...) should work,
			// however it fails in some steps of the wizard
			// (add event, add assay, etc) so g is not always
			// availably. Using our own instance of the
			// validationTagLib instead so it is always
			// available to us
			errors[error.getArguments()[0]] = validationTagLib.message(error: error)
		}

		return errors
	}
}
