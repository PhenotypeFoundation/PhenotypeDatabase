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
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
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
					toPage( "samples" );
					return;
				}
			}
		}

		// Give the study to the user
		[ study: study ]
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
				toPage( "study" );
				return;
			} else if( event == "skip" ) {
				// The user may skip the complete samples page
				toPage( "assays" );
				return;
			}
		}

		// Give the study and other data to the user
		[ study: study, sampleTemplates: Template.findAllByEntity( Sample.class ), encodedEntity: gdtService.encryptEntity( Sample.class.name ), sampleForm: session.simpleWizard.sampleForm ]
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

					toPage( "assays" );
				}
				return;
			} else if( event == "previous" ) {
				// THe user may go to the previous page, even if the data is not correct
				toPage( "samples" );
				return;
			}
		}

		// Give the study and other data to the user
		[ study: study,
					filename: session.simpleWizard.sampleForm.importFile,
					template: Template.get( session.simpleWizard.sampleForm.templateId ),
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
					if( session.simpleWizard.sampleForm.importFile ) 
						fileService.delete( session.simpleWizard.sampleForm.importFile );
						
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

		// Give the study and other data to the user
		[ study: study, imported: session.simpleWizard.imported, rules: rules ]
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
		
		Assay assay
		if( study.assays?.size() ) {
			assay = study.assays[0];
			study.removeFromAssays( assay );
		} else {
			assay = new Assay();
		}
			
		def event = getEvent(params);

		// If any event on this page is triggered, we should save the entered data.
		// If no event is triggered, the user gets here from another page. In that case,
		// we don't set the values
		if( event ) {
			// Only continue to the next or previous page if the information entered is correct
			if( event == "skip" ) {
				// The user may skip the complete assay page
				toPage( "overview" );
				return;
			} else if( handleAssays( assay, params ) ) {
				study.addToAssays( assay );
				
				// Now determine what action to perform
				if( event == "next" && validateObject( study ) ) {
					toPage( "overview" );
					return;
				} else if( event == "previous" ) {
					toPage( "samples" )
					return;
				}
			}
		}

		// Give the study to the user
		[ study: study, assay: assay ]
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
				toPage( "assay" )
				return;
			}
		}

		// Give the study to the user
		[ study: study ]
	}
	
	def save = {
		// Retrieve the correct study
		Study study = getStudyInWizard( params );
		if( !study ) {
			redirect( controller: 'simpleWizard', action: 'study' );
			return;
		}
		
		// Make sure all samples are attached to all assays
		study.assays.each { assay ->
			assay.samples?.clear();
			study.samples.each { sample ->
				assay.addToSamples( sample )
			}
		}
		
		// Save the study
		if( study.save( flush: true ) ) {
			// Clear session
			session.simpleWizard = null;
			
			flash.message = "Your study is succesfully saved.";
		} else {
			flash.error = "An error occurred while saving your study";
			//validateObject( study );
		}

		// Give the study to the user
		[ study: study ]
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
		if( filename[0..8] == 'existing*' )
			filename = filename[9..-1]

		def templateId  = params.long( 'template_id' )
		int sheetIndex = (params.int( 'sheetindex' ) ?: 1 )
		int dataMatrixStart = (params.int( 'datamatrix_start' ) ?: 2 )
		int headerRow = (params.int( 'headerrow' ) ?: 1 )

		// Save form data in session
		session.simpleWizard.sampleForm = [
					importFile: filename,
					templateId: templateId,
					sheetIndex: sheetIndex,
					dataMatrixStart: dataMatrixStart,
					headerRow: headerRow
				];

		// Check whether the template exists
		if (!templateId || !Template.get( templateId ) ){
			log.error ".simple study wizard not all fields are filled in"
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
		def template = Template.get(session.simpleWizard.sampleForm.templateId)
		def headers = session.simpleWizard.excel.data.header;

		if( !params.matches ) {
			log.error( ".simple study wizard no column matches given" );
			flash.error = "No column matches given";
			return false;
		}

		// Retrieve the chosen matches from the request parameters and put them into
		// the headers-structure, for later reference
		params.matches.index.each { columnindex, property ->
			// Create an actual class instance of the selected entity with the selected template
			// This should be inside the closure because in some cases in the advanced importer, the fields can have different target entities
			def entityClass = Class.forName( headers[columnindex.toInteger()].entityclass.getName(), true, this.getClass().getClassLoader())
			def entityObj = entityClass.newInstance(template: template)

			// Store the selected property for this column into the column map for the ImporterService
			headers[columnindex.toInteger()].property = property

			// Look up the template field type of the target TemplateField and store it also in the map
			headers[columnindex.toInteger()].templatefieldtype = entityObj.giveFieldType(property)

			// Is a "Don't import" property assigned to the column?
			headers[columnindex.toInteger()].dontimport = (property == "dontimport") ? true : false

			//if it's an identifier set the mapping column true or false
			entityObj.giveFields().each {
				headers[columnindex.toInteger()].identifier = ( it.preferredIdentifier && (it.name == property) )
			}
		}

		// Import the workbook and store the table with entity records and store the failed cells
		def (table, failedcells) = importerService.importData(session.simpleWizard.sampleForm.templateId,
				session.simpleWizard.excel.workbook,
				session.simpleWizard.excel.sheetIndex - 1,
				session.simpleWizard.excel.dataMatrixStart - 1,
				session.simpleWizard.excel.data.header)

		session.simpleWizard.imported = [
					data: table,
					failedCells: failedcells
				];

		// loop through all entities to validate them and add them to failedcells if an error occurs
		def numInvalidEntities = 0;
		def errors = [];

		// Remove all samples
		study.samples?.clear();

		table.each { record ->
			record.each { entity ->
				// Determine entity class and add a parent
				entity.parent = study
				study.addToSamples( entity );

				if (!entity.validate()) {
					numInvalidEntities++;

					// Add this field to the list of failed cells, in order to give the user feedback
					failedcells = addNonValidatingCells( failedcells, entity )

					// Also create a full list of errors
					errors += getHumanReadableErrors( entity );
				}
			}
		}

		session.simpleWizard.imported.numInvalidEntities = numInvalidEntities;
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

	   // Remove all samples before adding them again
	   study.samples?.clear();

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
			   study.addToSamples( entity );

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

	   return true
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
			flash.error = "The sheet number you provided is too high. The provided excel sheet has only " + workbook.getNumberOfSheets() + " sheet(s).";
			return false
		}

		def sheet = workbook.getSheetAt(sheetIndex - 1);
		def firstRowNum = sheet.getFirstRowNum();
		def lastRowNum = sheet.getLastRowNum();
		def numRows = lastRowNum - firstRowNum + 1;

		if( headerRow > numRows  ) {
			log.error ".simple study wizard Header row number is incorrect: " + headerRow + " / " + numRows;
			flash.error = "The header row number you provided is too high. Please provide a number equal to or below " + numRows;
			return false
		}

		if( dataMatrixStart > numRows  ) {
			log.error ".simple study wizard Data row number is incorrect: " + dataMatrixStart + " / " + numRows;
			flash.error = "The data row number you provided is too high. Please provide a number equal to or below " + numRows;
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

		if( !s.canWrite( authenticationService.getLoggedInUser() ) ) {
			flash.error = "No authorization to edit this study."
			return null;
		}

		return s
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
			return session.simpleWizard.study;
		} else {
			// The user didn't get here from the wizard or no study is found
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
