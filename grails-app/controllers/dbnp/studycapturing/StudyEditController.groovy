package dbnp.studycapturing

import org.dbnp.gdt.*
import grails.plugins.springsecurity.Secured
import dbnp.authentication.SecUser
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import grails.converters.JSON
import org.hibernate.ObjectNotFoundException

/**
 * Controller to handle adding and editing studies
 * @author robert
 */
@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class StudyEditController {
	def authenticationService
	def datatablesService
	def studyEditService
	
	/**
	 * Instance of the validation tag library used to retrieve validation errors
	 * @see getHumanReadableErrors()
	 */
	def validationTagLib = new ValidationTagLib()
	
    def add() {
		render(view: "properties", model: [ study: new Study() ] )
	}
	
	def edit() {
		def study = getStudyFromRequest( params )
		render( view: "properties", model: [ study: study ] )
	}
	
	/***********************************************
	 * 
	 * Different parts of the editing process
	 * 
	 ***********************************************/
	
	/**
	 * Shows the properties page to edit study details
	 * @return
	 */
	def properties() {
		def study = getStudyFromRequest( params )
		
		// If this page is posted to, handle the input
		if( study && request.post ) {
			handleStudyProperties( study, params )
			
			// If the user wants to continue to another page, validate and save the object
			if( params._action == "save" ) {
				if( validateObject( study ) ) {
					study.save( flush: true )
					flash.message = "The study details have been saved."
					redirect controller: "study", action: "list"
				}
			}
			
			if( params._action == "next" ) {
				if( validateObject( study ) ) {
					study.save()
					redirect action: "subjects", id: study.id
				}
			} 
		}
		
		[ study: study ]
	}
	
	/**
	 * Shows the overview page to edit subject details. 
	 * @return
	 */
	def subjects() {
		def study = getStudyFromRequest( params )
		if( !study ) {
			redirect action: "add"
			return
		}
		
		// Check the number of subjects, and the specific templates, without loading all subjects 
		// for efficiency reasons
		def numSubjects = Subject.countByParent( study )
		def subjectTemplates = Subject.executeQuery("select distinct s.template from Subject s WHERE s.parent = ?", [ study ] )
		
		[ 
			study: study, 
			templates: Template.findAllByEntity( Subject.class ), 
			subjectTemplates: subjectTemplates, 
			numSubjects: numSubjects,
			domainFields: Subject.domainFields			
		]
	}
	
	/**
	 * Returns data for the subjects datatable
	 * @return
	 */
	def dataTableSubjects() {
		def template = Template.read( params.long( "template" ) )
		def study = Study.read( params.long( "id" ) )
		
		if( !study ) {
			render dataTableError( "Invalid study given: " + study ) as JSON
			return
		}
		
		if( !template ) {
			render dataTableError( "Invalid template given: " + template ) as JSON
			return
		}

		def searchParams = datatablesService.parseParams( params )
		def data = studyEditService.getEntitiesForTemplate( searchParams, study, template )
		render datatablesService.createDatatablesOutput( data, params ) as JSON
	}
	
	/**
	 * Stores changes in the subject details
	 * @return
	 */
	def editSubjects() {
		def study = getStudyFromRequest( params )
		if( !study || !study.id ) {
			response.status = 404
			render "Study not found"
			return
		}
		
		// Loop over all subjects
		def success = true
		def errors = [:]
		def subjectsToSave = []
		
		params.subject.each { key, newProperties ->
			// Key should be a subject ID
			if( !key.isLong() ) {
				return;
			}
			
			def subject = Subject.read( key.toLong() )
			
			// If no proper subject is found, (or it belongs to another study), return
			if( !subject || subject.parent != study ) {
				return
			}
			
			// Store the new values into each entity field
			subject.giveFields().each() { field ->
				if( newProperties.containsKey( field.escapedName() ) ) {
					// set field
					subject.setFieldValue(
						field.name,
						newProperties[ field.escapedName() ]
					)
				}
			}
			
			if( subject.validate() ) {
				subjectsToSave << subject
			} else {
				success = false
				subject.errors.allErrors.each { error ->
					errors[ error.getArguments()[0] ] = g.message(error: error)
				}
			}
		}
		
		def result
		if( success ) {
			// Save all subjects
			subjectsToSave.each {
				it.save()
			}
			
			result = ["OK"]
		} else {
			result = [
				message: "Validation errors occurred",
				errors: errors
			]
		}
		
		render result as JSON
	}

	/**
	 * Returns an error response for the datatable
	 * @param error
	 * @return
	 */
	protected def dataTableError( error ) {
		return [
			sEcho: 					params.sEcho,
			iTotalRecords: 			0,
			iTotalDisplayRecords: 	0,
			aaData:					[],
			errorMessage: 			error
		]
	}
	
	
	def design() {
		def study = getStudyFromRequest( params )
		if( !study ) {
			redirect action: "add"
			return
		}
		
		[ 
			study: study, 
			templates: [ 
				event: Template.findAllByEntity( Event.class ),
				samplingEvent:  Template.findAllByEntity( SamplingEvent.class )
			]
		]

	}
	
	def samples() {
		def study = getStudyFromRequest( params )
		if( !study ) {
			redirect action: "add"
			return
		}
		
		// Check the number of subjects, and the specific templates, without loading all subjects
		// for efficiency reasons
		def numSamples = Sample.countByParent( study )
		def templates = Sample.executeQuery("select distinct s.template from Sample s WHERE s.parent = ?", [ study ] )
		
		[
			study: study,
			sampleTemplates: templates,
			numSamples: numSamples,
			domainFields: Sample.domainFields
		]
	}
	
	def assays() {}
	
	/**
	 * Returns a page without layout with the prototypes of the given template
	 * @return
	 */
	def prototypes() {
		if( !params.id || !params.id.isLong() ) {
			response.status = 400
			render "Bad request"
			return;
		}
		
		def template = Template.read( params.long( 'id' ) )
		
		if( !template ) {
			response.status = 404
			render "Template not found"
			return
		}
		
		render( 
			template: 'prototypes', 
			model: [ template: template ]
		)
	}
	
	/**
	 * Retrieves the required study from the database or return an empty Study object if
	 * no id is given
	 *
	 * @param params	Request parameters with params.id being the ID of the study to be retrieved
	 * @return			A study from the database or an empty study if no id was given
	 */
	protected Study getStudyFromRequest(params) {
		SecUser user = authenticationService.getLoggedInUser();
		Study study  = (params.containsKey('id')) ? Study.findById(params.get('id')) : new Study(title: "New study", owner: user);

		// got a study?
		if (!study) {
			flash.error = "No study found with given id";
		} else if(!study.canWrite(user)) {
			flash.error = "No authorization to edit this study."
			study = null;
		}

		return study;
	}

	/**
	 * Handles study properties input
	 * @param study		Study to update
	 * @param params	Request parameter map
	 * @return			True if everything went OK, false otherwise. An error message is put in flash.error
	 */
	def handleStudyProperties( study, params ) {
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
		   if( study.publications ) {
			   study.publications.findAll { publication -> !publicationIDs.find { id -> id == publication.id } }.each {
				   study.removeFromPublications(it)
			   }
		   }

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
		   if( study.publications ) {
			   study.publications.each {
				   study.removeFromPublications(it)
			   }
		   }
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
			   study.persons.findAll {
				   studyperson -> !contactIDs.find { ids -> (ids.person == studyperson.person.id) && (ids.role == studyperson.role.id) }
			   }.each {
				   study.removeFromPersons(it)
				   it.delete()
			   }
		   }

		   // Add those contacts not yet present in the database
		   contactIDs.each { ids ->
			   if (!study.persons.find { studyperson -> (ids.person == studyperson.person.id) && (ids.role == studyperson.role.id) }) {
				   def person = Person.get(ids.person)
				   def role = PersonRole.get(ids.role)
				   if (person && role) {
						// Create a new StudyPerson object representing the relation, and attach it to the study
						// Note that because StudyPerson objects belong to a study, they can not and should not be re-used across studies
						def studyPerson = new StudyPerson(
						   person: person,
						   role: role
						)
						studyPerson.save(flush: true)
						study.addToPersons(studyPerson)
				   } else {
					   log.info('.person ' + ids.person + ' or Role ' + ids.role + ' not found in database.')
				   }
			   }
		   }
	   } else {
		   log.info('.no persons selected.')
		   if( study.persons ) {
			   // removing persons from study
			   // Create a clone of persons list in order to avoid
			   // concurrentModification exceptions. See http://blog.springsource.com/2010/07/02/gorm-gotchas-part-2/
			   def persons = [] + study.persons;
			   persons.each {
				   study.removeFromPersons(it)
				   it.delete()
			   }
		   }
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
		   if (study.readers) {
			   study.readers.clear();
		   }
			   
		   users.each { study.addToReaders(it) }
	   } else if (type == "writers") {
			   
		   if (study.writers) {
			   study.writers.clear();
		   }

		   users.each { study.addToWriters(it) }
		   
	   }
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
