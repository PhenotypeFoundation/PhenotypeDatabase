/**
 * SimpleService Service
 * 
 * Description of my service
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

import org.dbnp.gdt.Template
import org.apache.poi.ss.usermodel.DataFormatter
import dbnp.importer.ImportRecord
import dbnp.importer.MappingColumn
import dbnp.importer.ImportCell
import dbnp.authentication.SecUser

class SimpleService {

    static transactional = true
    def authenticationService
    def importerService
    def fileService

    def validationTagLib = new ValidationTagLib()


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
    * re-usable code for handling publications form data
	* @param study	Study object to update
	* @param params GrailsParameterMap (the flow parameters = form data)
	* @returns boolean
	*/
   def handleStudyPublications(Study study,  params) {
	   if (study.publications) study.publications = []

	   // Check the ids of the pubblications that should be attached
	   // to this study. If they are already attached, keep 'm. If
	   // studies are attached that are not in the selected (i.e. the
	   // user deleted them), remove them
	   def publicationIDs = params.get('publication_ids')
	   if (publicationIDs) {
		   // Find the individual IDs and make integers
		   publicationIDs = publicationIDs.split(',').collect { Integer.parseInt(it, 10) }

		   // First remove the publication that are not present in the array
		   if( study.publications )
			   study.publications.removeAll { publication -> !publicationIDs.find { id -> id == publication.id } }

		   // Add those publications not yet present in the database
		   publicationIDs.each { id ->
			   if (!study.publications.find { publication -> id == publication.id }) {
				   def publication = Publication.get(id)
				   if (publication) {
					   study.addToPublications(publication)
				   } else {
					   log.info('.publication with ID ' + id + ' not found in database.')
				   }
			   }
		   }

	   } else {
		   log.info('.no publications selected.')
		   if( study.publications )
			   study.publications.clear()
	   }
   }


    /**
     * re-usable code for handling contacts form data
     * @param study	Study object to update
     * @param Map GrailsParameterMap (the flow parameters = form data)
     * @return boolean
     */
    def handleStudyContacts(Study study, params) {
        if (!study.persons) study.persons = []

        // Check the ids of the contacts that should be attached
        // to this study. If they are already attached, keep 'm. If
        // studies are attached that are not in the selected (i.e. the
        // user deleted them), remove them

        // Contacts are saved as [person_id]-[role_id]
        def contactIDs = params.get('contacts_ids')
        if (contactIDs) {
            // Find the individual IDs and make integers
            contactIDs = contactIDs.split(',').collect {
                def parts = it.split('-')
                return [person: Integer.parseInt(parts[0]), role: Integer.parseInt(parts[1])]
            }

            // First remove the contacts that are not present in the array
            if( study.persons ) {
                study.persons.removeAll {
                    studyperson -> !contactIDs.find { ids -> (ids.person == studyperson.person.id) && (ids.role == studyperson.role.id) }
                }
            }

            // Add those contacts not yet present in the database
            contactIDs.each { ids ->
                if (!study.persons.find { studyperson -> (ids.person == studyperson.person.id) && (ids.role == studyperson.role.id) }) {
                    def person = Person.get(ids.person)
                    def role = PersonRole.get(ids.role)
                    if (person && role) {
                        // Find a studyperson object with these parameters
                        def studyPerson = StudyPerson.findAll().find { studyperson -> studyperson.person.id == person.id && studyperson.role.id == role.id }

                        // If if does not yet exist, save the example
                        if (!studyPerson) {
                            studyPerson = new StudyPerson(
                                    person: person,
                                    role: role
                                    )
                            studyPerson.save(flush: true)
                        }

                        study.addToPersons(studyPerson)
                    } else {
                        log.info('.person ' + ids.person + ' or Role ' + ids.role + ' not found in database.')
                    }
                }
            }
        } else {
            log.info('.no persons selected.')
            if( study.persons )
                study.persons.clear()
        }
    }

    /**
     * re-usable code for handling contacts form data
     * @param study	Study object to update
     * @param Map GrailsParameterMap (the flow parameters = form data)
     * @param String    'readers' or 'writers'
     * @return boolean
     */
    def handleStudyUsers(Study study, params, type) {
        def users = []

        if (type == "readers" && study.readers ) {
            users += study.readers
        } else if (type == "writers" && study.writers ) {
            users += study.writers
        }

        // Check the ids of the contacts that should be attached
        // to this study. If they are already attached, keep 'm. If
        // studies are attached that are not in the selected (i.e. the
        // user deleted them), remove them

        // Users are saved as user_id
        def userIDs = params.get(type + '_ids')

        if (userIDs) {
            // Find the individual IDs and make integers
            userIDs = userIDs.split(',').collect { Long.valueOf(it, 10) }

            // First remove the publication that are not present in the array
            users.removeAll { user -> !userIDs.find { id -> id == user.id } }

            // Add those publications not yet present in the database
            userIDs.each { id ->
                if (!users.find { user -> id == user.id }) {
                    def user = SecUser.get(id)
                    if (user) {
                        users.add(user)
                    } else {
                        log.info('.user with ID ' + id + ' not found in database.')
                    }
                }
            }

        } else {
            log.info('.no users selected.')
            users.clear()
        }

        if (type == "readers") {
            if (study.readers)
                study.readers.clear()

            users.each { study.addToReaders(it) }
        } else if (type == "writers") {
            if (study.writers)
                study.writers.clear()

            users.each { study.addToWriters(it) }

        }
    }



	/**
	* Handles the editing of existing samples
	* @param study		Study to update
	* @param params		Request parameter map
	* @return			True if everything went OK, false otherwise. An error message is put in flash.error
	*/
   def handleExistingSamples( study, params, flow ) {
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
		flow.sampleForm = [
					importFile: filename,
					templateId: [
						'Sample': sampleTemplateId,
						'Subject': subjectTemplateId,
						'Event': eventTemplateId,
						'SampingEvent': samplingEventTemplateId
					],
					template: [
						'Sample': sampleTemplateId ? Template.get( sampleTemplateId ) : null,
						'Subject': subjectTemplateId ? Template.get( subjectTemplateId ) : null,
						'Event': eventTemplateId ? Template.get( eventTemplateId ) : null,
						'SampingEvent': samplingEventTemplateId ? Template.get( samplingEventTemplateId ) : null
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
		println "Importing samples for study " + study + " (" + study.id + ")";

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

		flow.imported.data.each { table ->
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

		flow.imported.numInvalidEntities = numInvalidEntities;
		flow.imported.errors = errors;

		return true
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

	   // Save the assay in session
	   flow.assay = assay;

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
}
