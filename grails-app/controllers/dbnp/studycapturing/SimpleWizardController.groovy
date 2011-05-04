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

import org.apache.poi.ss.usermodel.DataFormatter
import org.dbnp.gdt.*
import grails.plugins.springsecurity.Secured
import dbnp.authentication.SecUser
import dbnp.importer.ImportCell
import dbnp.importer.ImportRecord
import dbnp.importer.MappingColumn
import org.hibernate.SessionFactory;

@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class SimpleWizardController extends StudyWizardController {
	def authenticationService
	def fileService
	def importerService
	def gdtService = new GdtService()
	def sessionFactory

	/**
	 * index closure
	 */
	def index = {
		if( params.id )
			redirect( action: "simpleWizard", id: params.id );
		else
			redirect( action: "simpleWizard" );
	}

	def simpleWizardFlow = {
		entry {
			action{
				flow.study = getStudyFromRequest( params )
				if (!flow.study) retrievalError()
				
				// Search for studies
				flow.studies = Study.giveWritableStudies( authenticationService.getLoggedInUser(), 100 )
			}
			on("retrievalError").to "handleError"
			on("success").to "study"
		}

		study {
			on("next") {
				handleStudy( flow.study, params )
				if( !validateObject( flow.study ) )
					error()
			}.to "decisionState"
			on("open") {
				// Send the user to the URL of the simple wizard in order
				// to avoid code duplication for loading the study
				if( params.study ) {
					flow.openStudyId = params.study
				} else {
					flash.error = "No study selected";
					return error();
				}
			}.to "openStudy"
			on("refresh") {  handleStudy( flow.study, params ); }.to "study"
			on( "save" ) {
				handleStudy( flow.study, params );
				if( !validateObject( flow.study ) )
					return error()
				
				if( flow.study.save( flush: true ) ) {
					flash.message = "Your study is succesfully saved.";
				} else {
					flash.error = "An error occurred while saving your study: <br />"
					flow.study.getErrors().each { flash.error += it.toString() + "<br />"}
				}
				success()		
			}.to "study"
		}
		
		openStudy {
			redirect( action: "simpleWizard", id: flow.openStudyId );
		}

		decisionState {
			action {
				// Create data in the flow
				flow.templates = [
							'Subject': Template.findAllByEntity( Subject.class ),
							'Event': Template.findAllByEntity( Event.class ),
							'SamplingEvent': Template.findAllByEntity( SamplingEvent.class ),
							'Sample': Template.findAllByEntity( Sample.class )
				];
			
				flow.encodedEntity = [
							'Subject': gdtService.encryptEntity( Subject.class.name ),
							'Event': gdtService.encryptEntity( Event.class.name ),
							'SamplingEvent': gdtService.encryptEntity( SamplingEvent.class.name ),
							'Sample': gdtService.encryptEntity( Sample.class.name )
				]

				if (flow.study.samples)
					checkStudySimplicity(flow.study) ? existingSamples() : complexStudy()
				else
					samples()
			}
			on ("existingSamples").to "startExistingSamples"
			on ("complexStudy").to "complexStudy"
			on ("samples").to "samples"
		}
		
		startExistingSamples {
			action {
				def records = importerService.getRecords( flow.study );
				flow.records = records
				flow.templateCombinations = records.templateCombination.unique()
				success();
			}
			on( "success" ).to "existingSamples"
		}

		existingSamples {
			on("next") {
				handleExistingSamples( flow.study, params, flow ) ? success() : error()
			}.to "startAssays"
			on( "save" ) {
				if( !handleExistingSamples( flow.study, params, flow ) )
					return error()
				
				if( flow.study.save( flush: true ) ) {
					flash.message = "Your study is succesfully saved.";
				} else {
					flash.error = "An error occurred while saving your study: <br />"
					flow.study.getErrors().each { flash.error += it.toString() + "<br />"}
				}
				success()
			}.to "existingSamples"
			on("previous").to "study"
			on("refresh") {
				if( !handleExistingSamples( flow.study, params, flow ) )
					return error()

				// Refresh the templates, since the template editor has been opened.
				flow.templates = [
						'Subject': Template.findAllByEntity( Subject.class ).collect { it.refresh(); return it },
						'Event': Template.findAllByEntity( Event.class ).collect { it.refresh(); return it },
						'SamplingEvent': Template.findAllByEntity( SamplingEvent.class ).collect { it.refresh(); return it },
						'Sample': Template.findAllByEntity( Sample.class ).collect { it.refresh(); return it }
				];
			}.to "existingSamples"
			on("update") {
				handleExistingSamples( flow.study, params, flow ) ? success() : error()
			}.to "samples"

			on("skip").to "startAssays"
		}

		complexStudy {
			on("save").to "save"
			on("previous").to "study"
		}

		samples {
			on("next") {
				if( !handleSamples( flow.study, params, flow ) )
					return error();
				
				// Add domain fields for all entities
				flow.domainFields = [:]
				
				flow.templates.each { 
					if( it.value ) {
						flow.domainFields[ it.key ] = it.value[0].entity.giveDomainFields();
					}
				}
				
				//println flow.sampleForm.template
			}.to "columns"
			on("refresh") {
				def filename = params.get( 'importfile' );
		
				handleSampleForm( flow.study, params, flow )

				// Handle 'existing*' in front of the filename. This is put in front to make a distinction between
				// an already uploaded file test.txt (maybe moved to some other directory) and a newly uploaded file test.txt
				// still being in the temporary directory.
				// This import step doesn't have to make that distinction, since all files remain in the temporary directory.
				if( filename == 'existing*' )
					filename = '';
				else if( filename[0..8] == 'existing*' )
					filename = filename[9..-1]
				
				flow.sampleForm.importFile = filename
					
				// Refresh the templates, since the template editor has been opened. 
				flow.templates = [
						'Subject': Template.findAllByEntity( Subject.class ).collect { it.refresh(); return it },
						'Event': Template.findAllByEntity( Event.class ).collect { it.refresh(); return it },
						'SamplingEvent': Template.findAllByEntity( SamplingEvent.class ).collect { it.refresh(); return it },
						'Sample': Template.findAllByEntity( Sample.class ).collect { it.refresh(); return it }
				];
			}.to "samples"
			on("previous").to "returnFromSamples"
			on("study").to "study"
			on("skip").to "startAssays"
		}

		returnFromSamples {
			action {
				flow.study.samples ? existingSamples() : study();
			}
			on( "existingSamples" ).to "startExistingSamples"
			on( "study" ).to "study"
		}
		
		columns {
			on( "next" ) {
				flow.editImportedData = params.get( 'editAfterwards' ) ? true : false;
				handleColumns( flow.study, params, flow ) ? success() : error()
			}.to "checkImportedEntities"
			on( "previous" ).to "samples" 
		}
		
		checkImportedEntities {
			action {
				// Only continue to the next page if the information entered is correct
				if( flow.editImportedData || flow.imported.numInvalidEntities > 0 ) {
					missingFields();
				} else {
					// The import of the excel file has finished. Now delete the excelfile
					if( flow.excel.filename )
						fileService.delete( flow.excel.filename );
	
					flow.sampleForm = null
	
					assays();
				}
			}
			on( "missingFields" ).to "missingFields"
			on( "assays" ).to "startAssays" 
		}
		
		missingFields {
			on( "refresh" ) {
				handleMissingFields( flow.study, params, flow );
				success();
			}.to "missingFields"
			on( "next" ) {
				if( !handleMissingFields( flow.study, params, flow ) ) {
					return error();
				}
				
				// The import of the excel file has finished. Now delete the excelfile
				if( flow.excel.filename )
					fileService.delete( flow.excel.filename );

				flow.sampleForm = null
				
				success();
			}.to "startAssays"
			on( "previous" ) {
				// The user goes back to the previous page, so the already imported entities
				// (of which some gave an error) should be removed again.
				// Add all samples
				flow.imported.data.each { record ->
					record.each { entity ->
						if( entity ) {
							switch( entity.class ) {
								case Sample:	flow.study.removeFromSamples( entity ); break;
								case Subject:	flow.study.removeFromSubjects( entity ); break;
								case Event:		flow.study.removeFromEvents( entity ); break;
								case SamplingEvent:	flow.study.removeFromSamplingEvents( entity ); break;
							}
						}
					}
				}
				
				success();
			}.to "columns"
		}
		
		startAssays {
			action {
				if( !flow.assay ) {
					if( flow.study.assays ) {
						flow.assay = flow.study.assays[ 0 ]
						//println "Existing assay: " + flow.assay
					} else {
						flow.assay = new Assay( parent: flow.study );
					}
				}
				success();
			}
			on( "success" ).to "assays"
		}
		
		assays {
			on( "next" ) { 
				handleAssays( flow.assay, params, flow );
				if( flow.assay.template && !validateObject( flow.assay ) )
					error();
					
			 }.to "overview"
			on( "skip" ) {
				// In case the user has created an assay before he clicked 'skip', it should only be kept if it
				// existed before this step
				if( flow.assay != null && !flow.assay.id ) {
					flow.remove( "assay" )
				}

			 }.to "overview"
			on( "previous" ).to "returnFromAssays"
			on( "save" ) {
				handleAssays( flow.assay, params, flow );
				if( flow.assay.template && !validateObject( flow.assay ) )
					error();

				if( saveStudyToDatabase( flow ) ) {
					flash.message = "Your study is succesfully saved.";
				} else {
					flash.error = "An error occurred while saving your study: <br />"
					flow.study.getErrors().each { flash.error += it.toString() + "<br />"}
					return error();
				}
			}.to "assays"
			on("refresh") { 
				handleAssays( flow.assay, params, flow ); 
				
				flow.assay?.template?.refresh()
				success() 
			}.to "assays"
		}

		returnFromAssays {
			action {
				flow.study.samples ? existingSamples() : samples();
			}
			on( "existingSamples" ).to "existingSamples"
			on( "samples" ).to "samples"
		}
		
		overview { 
			on( "save" ).to "saveStudy" 
			on( "previous" ).to "startAssays"
		}
		saveStudy {
			action {
				if( saveStudyToDatabase( flow ) ) {
					flash.message = "Your study is succesfully saved.";
					finish();
				} else {
					flash.error = "An error occurred while saving your study: <br />"
					flow.study.getErrors().each { flash.error += it.toString() + "<br />"}
					overview();
				}
			}
			on( "finish" ).to "finish"
			on( "overview" ).to "overview"
		}
		
		finish()
		
		handleError{
			redirect action: "errorPage"
		}
	}
	
	/**
	 * Saves the study with assay
	 * 
	 * @param flow
	 * @return true on success, false otherwise
	 */
	protected boolean saveStudyToDatabase( def flow ) {
		// Save the assay to the study
		if( flow.assay && flow.assay.template && !flow.study.assays?.contains( flow.assay ) ) {
			flow.study.addToAssays( flow.assay );
		}

		if( flow.study.save( flush: true ) ) {
			// Make sure all samples are attached to all assays
			flow.study.assays.each { assay ->
				def l = []+ assay.samples;
				l.each { sample ->
					if( sample )
						assay.removeFromSamples( sample );
				}
				assay.samples?.clear();

				flow.study.samples.each { sample ->
					assay.addToSamples( sample )
				}
			}
			
			return true;
	
		} else {
			// Remove the assay from the study again, since it is still available
			// in the session
			if( flow.assay ) {
				flow.study.removeFromAssays( flow.assay );
				flow.assay.parent = flow.study;
			}
			
			return false;
		}
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
   def handleExistingSamples( study, params, flow ) {
	   flash.validationErrors = [];

	   def errors = false;
	   
	   // iterate through objects; set field values and validate the object
	   def eventgroups = study.samples.parentEventGroup.findAll { it }
	   def events;
	   if( !eventgroups )
		   events = []
	   else
		   events = eventgroups.events?.getAt(0);
	   
	   def objects = [
		   'Subject': study.samples.parentSubject.findAll { it },
		   'SamplingEvent': study.samples.parentEvent.findAll { it },
		   'Event': events.flatten().findAll { it },
		   'Sample': study.samples
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
				   
				   def entityName = entity.class.name[ entity.class.name.lastIndexOf( "." ) + 1 .. -1 ]
				   getHumanReadableErrors( entity ).each {
				   		flash.validationErrors << [ key: it.key, value: "(" + entityName + ") " + it.value ];
				   }
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
	def handleSamples( study, params, flow ) {
		def filename = params.get( 'importfile' );

		// Handle 'existing*' in front of the filename. This is put in front to make a distinction between
		// an already uploaded file test.txt (maybe moved to some other directory) and a newly uploaded file test.txt
		// still being in the temporary directory.
		// This import step doesn't have to make that distinction, since all files remain in the temporary directory.
		if( filename == 'existing*' )
			filename = '';
		else if( filename[0..8] == 'existing*' )
			filename = filename[9..-1]

		handleSampleForm( study, params, flow );

		// Check whether the template exists
		if (!flow.sampleForm.template.Sample ){
			log.error ".simple study wizard not all fields are filled in (sample template) "
			flash.error = "No template was chosen. Please choose a template for the samples you provided."
			return false
		}
		
		// These fields have been removed from the form, so will always contain
		// their default value. The code however remains like this for future use.
		int sheetIndex = (params.int( 'sheetindex' ) ?: 1 )
		int dataMatrixStart = (params.int( 'datamatrix_start' ) ?: 2 )
		int headerRow = (params.int( 'headerrow' ) ?: 1 )
		
		flow.sampleForm.sheetIndex = sheetIndex;
		flow.sampleForm.dataMatrixStart = dataMatrixStart
		flow.sampleForm.headerRow = headerRow
		flow.sampleForm.importFile = filename
		
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

		// Match excel columns with template fields
		def fieldNames = [];
		flow.sampleForm.template.each { template ->
			if( template.value ) {
				def fields = template.value.entity.giveDomainFields() + template.value.getFields();
				fields.each { field ->
					if( !field.entity )
						field.entity = template.value.entity
						
					fieldNames << field
				}
			}
		}
		importerHeader.each { mc ->
			def bestfit = importerService.mostSimilar( mc.name, fieldNames, 0.8);
			if( bestfit ) {
				// Remove this fit from the list
				fieldNames.remove( bestfit );
				
				mc.entityclass = bestfit.entity
				mc.property = bestfit.name
			}
		}
		
		// Save read excel data into session
		def dataMatrix = [];
		def df = new DataFormatter();
		importerDataMatrix.each {
			dataMatrix << it.collect{ it ? df.formatCellValue(it) : "" }
		}
		
		flow.excel = [
					filename: filename,
					sheetIndex: sheetIndex,
					dataMatrixStart: dataMatrixStart,
					headerRow: headerRow,
					data: [
						header: importerHeader,
						dataMatrix: dataMatrix
					]
				]

		return true
	}
	
	/**
	 * Copies data from the submitted sample form to the flow
	 * @param study
	 * @param params
	 * @param flow
	 * @return
	 */
	protected def handleSampleForm( study, params, flow ) {
		def sampleTemplateId  = params.long( 'sample_template_id' )
		def subjectTemplateId  = params.long( 'subject_template_id' )
		def eventTemplateId  = params.long( 'event_template_id' )
		def samplingEventTemplateId  = params.long( 'samplingEvent_template_id' )

		// Save form data in session
		if( !flow.sampleForm )
			flow.sampleForm = [:]
			
		flow.sampleForm.templateId = [
						'Subject': subjectTemplateId,
						'Event': eventTemplateId,
						'SamplingEvent': samplingEventTemplateId,
						'Sample': sampleTemplateId
		];
		flow.sampleForm.template = [
						'Subject': subjectTemplateId ? Template.get( subjectTemplateId ) : null,
						'Event': eventTemplateId ? Template.get( eventTemplateId ) : null,
						'SamplingEvent': samplingEventTemplateId ? Template.get( samplingEventTemplateId ) : null,
						'Sample': sampleTemplateId ? Template.get( sampleTemplateId ) : null
		];
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
	def handleColumns( study, params, flow ) {
		// Find actual Template object from the chosen template name
		def templates = [:];
		flow.sampleForm.templateId.each {
			templates[ it.key ] = it.value ? Template.get( it.value ) : null;
		}
		
		def headers = flow.excel.data.header;

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
		//println "Importing samples for study " + study + " (" + study.id + ")";
		
		def importedfile = fileService.get( flow.excel.filename )
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
			
		def imported = importerService.importOrUpdateDataBySampleIdentifier(templates,
				workbook,
				flow.excel.sheetIndex - 1,
				flow.excel.dataMatrixStart - 1,
				flow.excel.data.header,
				flow.study,
				true			// Also create entities for which no data is imported but where templates were chosen
		);

		def table = imported.table
		def failedcells = imported.failedCells

		flow.imported = [
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
					// Determine entity class and add a parent. Add the entity to the study
					def preferredIdentifier = importerService.givePreferredIdentifier( entity.class );
					def equalClosure = { it.getIdentifier() == entity.getIdentifier() }
					def entityName = entity.class.name[ entity.class.name.lastIndexOf( "." ) + 1 .. -1 ]

					entity.parent = study
					
					switch( entity.class ) {
						case Sample:
							if( !study.samples?.find( equalClosure ) ) {
								study.addToSamples( entity );
							}
							
							// If an eventgroup is created, add it to the study
							// The eventgroup must have a unique name, but the user shouldn't be bothered with it
							// Add 'group ' + samplename and it that is not unique, add a number to it
							if( entity.parentEventGroup ) {
								study.addToEventGroups( entity.parentEventGroup )

								entity.parentEventGroup.name = "Group " + entity.name
								while( !entity.parentEventGroup.validate() ) {
									//entity.parentEventGroup.getErrors().each { println it }
									entity.parentEventGroup.name += "" + Math.floor( Math.random() * 100 )
								}
							}
							
							break;
						case Subject:
							if( !study.samples?.find( equalClosure ) ) {
								
								if( preferredIdentifier ) {
									// Subjects without a name should just be called 'subject'
									if( !entity.getFieldValue( preferredIdentifier.name ) )
										entity.setFieldValue( preferredIdentifier.name, "Subject" );
								
									// Subjects should have unique names; if the user has entered the same name multiple times,
									// the subject will be renamed
									def baseName = entity.getFieldValue( preferredIdentifier.name )
									def counter = 2;
									
									while( study.subjects?.find { it.getFieldValue( preferredIdentifier.name ) == entity.getFieldValue( preferredIdentifier.name ) } ) {
										entity.setFieldValue( preferredIdentifier.name, baseName + " (" + counter++ + ")" )
									}
								}
								
								study.addToSubjects( entity );
							
							}
							
							break;
						case Event:
							if( !study.events?.find( equalClosure ) ) {
								study.addToEvents( entity );
							}
							break;
						case SamplingEvent:
							// Sampling events have a 'sampleTemplate' value, which should be filled by the
							// template that is chosen for samples.
							if( !entity.getFieldValue( 'sampleTemplate' ) ) {
								entity.setFieldValue( 'sampleTemplate', flow.sampleForm.template.Sample.name )
							} 
						
							if( !study.samplingEvents?.find( equalClosure ) ) {
								study.addToSamplingEvents( entity );
							}
							break;
					}
					
					if (!entity.validate()) {
						numInvalidEntities++;
						
						// Add this field to the list of failed cells, in order to give the user feedback
						failedcells = addNonValidatingCells( failedcells, entity, flow )
	
						// Also create a full list of errors
						def currentErrors = getHumanReadableErrors( entity )
						if( currentErrors ) {
							currentErrors.each {
								errors += "(" + entityName + ") " + it.value;
							}
						}
					}
				}
			}
		}

		flow.imported.numInvalidEntities = numInvalidEntities + failedcells?.size();
		flow.imported.errors = errors;

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
	def handleMissingFields( study, params, flow ) {
		def numInvalidEntities = 0;
		def errors = [];

		// Check which fields failed previously
		def failedCells = flow.imported.failedCells
		def newFailedCells = [];

		flow.imported.data.each { table ->
			table.each { entity ->
				def invalidFields = 0
				def failed = new ImportRecord();
				def entityName = entity.class.name[ entity.class.name.lastIndexOf( "." ) + 1 .. -1 ]
				

				// Set the fields for this entity by retrieving values from the params
				entity.giveFields().each { field ->
					def fieldName = importerService.getFieldNameInTableEditor( entity, field );

					if( params[ fieldName ] == "#invalidterm" ) {
						// If the value '#invalidterm' is chosen, the user hasn't fixed anything, so this field is still incorrect
						invalidFields++;
						
						// store the mapping column and value which failed
						def identifier = entityName.toLowerCase() + "_" + entity.getIdentifier() + "_" + fieldName
						def mcInstance = new MappingColumn()
						failed.addToImportcells(new ImportCell(mappingcolumn: mcInstance, value: params[ fieldName ], entityidentifier: identifier))
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
				
				// Try to validate the entity now all fields have been set. If it fails, return an error
				if (!entity.validate() || invalidFields) {
					numInvalidEntities++;

					// Add this field to the list of failed cells, in order to give the user feedback
					failedCells = addNonValidatingCellsToImportRecord( failed, entity, flow )

					// Also create a full list of errors
					def currentErrors = getHumanReadableErrors( entity )
					if( currentErrors ) {
						currentErrors.each {
							errors += "(" + entityName + ") " + it.value;
						}
					}
					
					newFailedCells << failed;
				} else {
					importerService.removeFailedCell( failedCells, entity )
				}
			} // end of record
		} // end of table

		flow.imported.failedCells = newFailedCells
		flow.imported.numInvalidEntities = numInvalidEntities;
		flow.imported.errors = errors;

		return numInvalidEntities == 0
	}
	
	/**
	* Handles assay input
	* @param study		Study to update
	* @param params		Request parameter map
	* @return			True if everything went OK, false otherwise. An error message is put in flash.error
	*/
   def handleAssays( assay, params, flow ) {
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
						if( subject.id && eventGroup.subjects && eventGroup.subjects.toList()[0]?.id == subject.id )
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
						if( !sample.parentEventGroup.subjects || sample.parentEventGroup.subjects.toList()[0]?.id != sample.parentSubject.id ) {
							flash.message = "The structure of the eventgroups of one or more samples is too complex"
							simplicity = false;
						}
					}

					// If no id is given for the sampling event, it has been entered in this wizard, but
					// not yet saved. In that case, it is always OK
					if( sample.parentEvent && sample.parentEvent.id) {
						if( !sample.parentEventGroup.samplingEvents || sample.parentEventGroup.samplingEvents.toList()[0]?.id != sample.parentEvent.id ) {
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

	
	/**
	 * Adds all fields of this entity that have given an error when validating to the failedcells list
	 * @param failedcells	Current list of ImportRecords
	 * @param entity		Entity to check. The entity must have been validated before
	 * @return				Updated list of ImportRecords
	 */
	protected def addNonValidatingCells( failedcells, entity, flow ) {
		// Add this entity and the fields with an error to the failedCells list
		ImportRecord failedRecord = addNonValidatingCellsToImportRecord( new ImportRecord(), entity, flow );

		failedcells.add( failedRecord );

		return failedcells
	}
	
	/**
	* Adds all fields of this entity that have given an error when validating to the failedcells list
	* @param failedcells	Current list of ImportRecords
	* @param entity		Entity to check. The entity must have been validated before
	* @return				Updated list of ImportRecords
	*/
   protected def addNonValidatingCellsToImportRecord( failedRecord, entity, flow ) {
	   entity.getErrors().getFieldErrors().each { error ->
		   String field = error.getField();
		   
		   def mc = importerService.findMappingColumn( flow.excel.data.header, field );
		   def mcInstance = new MappingColumn( name: field, entityClass: Sample.class, index: -1, property: field.toLowerCase(), templateFieldType: entity.giveFieldType( field ) );

		   // Create a clone of the mapping column
		   if( mc ) {
			   mcInstance.properties = mc.properties
		   }

		   failedRecord.addToImportcells( new ImportCell(mappingcolumn: mcInstance, value: error.getRejectedValue(), entityidentifier: importerService.getFieldNameInTableEditor( entity, field ) ) )
	   }
	   
	   return failedRecord
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

    // TEMPORARY ACTION TO TEST CORRECT STUDY DESIGN INFERRING: SHOULD BE REMOVED WHEN DONE
    def testMethod = {

        def data = [//['sample name',         'subject',         'timepoint'],
                    [ '97___N_151_HAKA_1',    'N_151_HAKA',    '0w'],
                    [ '98___N_163_QUJO_3',    'N_163_QUJO',    '2w'],
                    [ '99___N_151_HAKA_2',    'N_151_HAKA',    '1w'],
                    ['100___N_163_QUJO_4',    'N_163_QUJO',    '3w'],
                    ['101___N_151_HAKA_3',    'N_151_HAKA',    '2w'],
                    ['102___N_163_QUJO_2',    'N_163_QUJO',    '1w'],
                    ['103___U_031_SMGI_1',    'U_031_SMGI',    '0w'],
                    ['104___U_031_SMGI_3',    'U_031_SMGI',    '2w'],
                    ['105___N_163_QUJO_1',    'N_163_QUJO',    '0w'],
                    ['106___U_031_SMGI_4',    'U_031_SMGI',    '3w'],
                    ['107___N_151_HAKA_4',    'N_151_HAKA',    '3w'],
                    ['108___U_031_SMGI_2',    'U_031_SMGI',    '1w'],
                    ['109___N_021_THAA_2',    'N_021_THAA',    '1w'],
                    ['110___U_029_DUJA_2',    'U_029_DUJA',    '1w'],
                    ['111___U_029_DUJA_3',    'U_029_DUJA',    '2w'],
                    ['112___N_021_THAA_3',    'N_021_THAA',    '2w'],
                    ['113___U_029_DUJA_4',    'U_029_DUJA',    '3w'],
                    ['114___N_045_SNSU_1',    'N_045_SNSU',    '0w'],
                    ['115___N_021_THAA_1',    'N_021_THAA',    '0w'],
                    ['116___N_045_SNSU_2',    'N_045_SNSU',    '1w'],
                    ['117___N_045_SNSU_3',    'N_045_SNSU',    '2w'],
                    ['118___N_045_SNSU_4',    'N_045_SNSU',    '3w'],
                    ['119___N_021_THAA_4',    'N_021_THAA',    '3w'],
                    ['120___U_029_DUJA_1',    'U_029_DUJA',    '0w'],
                    ['121___U_060_BRGE_3',    'U_060_BRGE',    '2w'],
                    ['122___N_018_WIHA_1',    'N_018_WIHA',    '0w'],
                    ['123___N_022_HUCA_3',    'N_022_HUCA',    '2w'],
                    ['124___N_022_HUCA_2',    'N_022_HUCA',    '1w']]

        def sampleTemplate = Template.findByName 'Human blood sample'
        def subjectTemplate = Template.findByName 'Human'
        def samplingEventTemplate = Template.findByName 'Blood extraction'
        def eventTemplate = Template.findByName 'Diet treatment'


        // Table is a collection of records. A records contains entities of type
        // Sample, Subject, and SamplingEvent. This mimics the output of
        // importerService.importOrUpdateDataBySampleIdentifier
        def table = data.collect { row ->

            [       new Sample( name: row[0], template: sampleTemplate),
                    new Subject(name: row[1], template: subjectTemplate),
                    new SamplingEvent(template: samplingEventTemplate).setFieldValue('startTime', row[2])
//                    new Event(template: eventTemplate)
            ]

        }

        // collect unique subjects and sampling events from table
        def uniqueSubjects = table.collect{it[1]}.unique()
        def uniqueSamplingEvents = table.collect{it[2]}.unique()

        // create an event group for each unique sampling event (not much of a group, is it ...)
        def eventGroups = uniqueSamplingEvents.collect{
            def eventGroup = new EventGroup(name: "Sampling_${it.name}_${it.startTime}").addToSamplingEvents(it)
            //study.addToEventGroups eventGroup
            eventGroup
        }
        //TODO: add event groups to study

//        uniqueSubjects.each {
//            eventGroup.addToSubjects it
//            // study.addToSubject it
//        }

        table.each{ record ->

            Sample sample = record[0]

            // gather all sample related entities
            def correspondingSamplingEvent  = uniqueSamplingEvents.findByStartTime(record[2].startTime)
            def correspondingSubject        = uniqueSubjects.findByName(record[1].name)
            def correspondingEventGroup     = correspondingSamplingEvent.eventGroup

            correspondingSamplingEvent.addToSamples sample
            correspondingEventGroup.addToSamples    sample
            correspondingEventGroup.addToSubjects   correspondingSubject
            sample.parentSubject =                  correspondingSubject




            // study.addToSamples sample
            uniqueSamplingEvents.findByStartTime(record[2].startTime).addToSamples(sample)
            sample.addToSubjects(uniqueSubjects.findByName(record[1].name))

        }


        println 'hoi'
        render 'bla'

    }
}
