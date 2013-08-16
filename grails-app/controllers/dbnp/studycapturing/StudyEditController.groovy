package dbnp.studycapturing

import org.dbnp.gdt.*
import grails.plugins.springsecurity.Secured
import dbnp.authentication.SecUser
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import grails.converters.JSON

/**
 * Controller to handle adding and editing studies
 * @author robert
 */
@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class StudyEditController {
	def authenticationService
	
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
	
	// Different parts of the editing process
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
	
	def dataTableSubjects() {
		/**
		 * Input:
		 * 
			int			iDisplayStart	Display start point in the current data set.
			int			iDisplayLength	Number of records that the table can display in the current draw. It is expected that the number of records returned will be equal to this number, unless the server has fewer records to return.
			
			string		sSearch			Global search field
			int			iSortingCols	Number of columns to sort on
			
			int			iSortCol_(int)	Column being sorted on (you will need to decode this number for your database)
			string		sSortDir_(int)	Direction to be sorted - "desc" or "asc".

			string		sEcho			Information for DataTables to use for rendering.
		 * 
		 * 
		 * Output:
		 * 
		 * {
			    "sEcho": 3,
			    "iTotalRecords": 57,
			    "iTotalDisplayRecords": 57,
			    "aaData": [
			        [
			            "Gecko",
			            "Firefox 1.0",
			            "Win 98+ / OSX.2+",
			            "1.7",
			            "A"
			        ],
			        [
			            "Gecko",
			            "Firefox 1.5",
			            "Win 98+ / OSX.2+",
			            "1.8",
			            "A"
			        ],
			        ...
			    ]
			}
		 */
		
		// Retrieve the right template
		def template = Template.read( params.long( "template" ) )
		def study = Study.read( params.long( "id" ) )
		
		if( !template || !study ) {
			render dataTableError( "Invalid study or template given: " + params.template ) as JSON
			return
		}
	
		def domainFields = Subject.domainFields
		def output = [ 
			sEcho: params.sEcho,
			iTotalRecords: Subject.countByParent( study ) 
		]
		
		// Make sure only subjects for this study are returned
		def limit = params.int( "iDisplayLength" ) ?: -1
		def offset = params.int( "iDisplayStart" ) ?: 0
		
		// Create an HQL query as it gives us the most flexibility in searching and ordering
		def from = "FROM Subject s "
		def joins = []
		def whereClause = []
		def hqlParams = [ study: study ]
		def orderBy = ""

		// First add searching
		if( params.sSearch ) {
			// With searching, retrieving the data requires joining all text and term fields
			def searchTerm = params.sSearch.toLowerCase()
			
			// Only allow for searching in textual fields
			def fieldTypesAllowed = [
				TemplateFieldType.STRING,
				TemplateFieldType.TEXT,

				TemplateFieldType.STRINGLIST,
				TemplateFieldType.EXTENDABLESTRINGLIST,
				
				TemplateFieldType.ONTOLOGYTERM,
				TemplateFieldType.TEMPLATE
			]
			
			def fieldTypesAsReference = [ 
				TemplateFieldType.STRINGLIST,
				TemplateFieldType.EXTENDABLESTRINGLIST,
				TemplateFieldType.ONTOLOGYTERM,
				TemplateFieldType.TEMPLATE
			]
			
			// Domain fields are handled differently from template fields
			domainFields.each { field ->
				// Continue if this type is not allowed
				if( !( field.type in fieldTypesAllowed ) )
					return true;
					
				if( field.type in fieldTypesAsReference )
					whereClause << "lower( s." + field + ".name ) LIKE :search"
				else
					whereClause << "lower( s." + field + " ) LIKE :search"
					
				hqlParams[ "search" ] = "%" + searchTerm + "%"
			}
			
			template.fields.each { field ->
				// Continue if this type is not allowed
				if( !( field.type in fieldTypesAllowed ) )
					return true;

				def store = "template${field.type.casedName}Fields"
				def joinName = "templateField" + field.id
				
				joins << "s." + store + " as " + joinName + " WITH index( " + joinName + " ) = :fieldName${joinName}"
				hqlParams[ "fieldName${joinName}" ] = field.name
										
				if( field.type in fieldTypesAsReference )
					whereClause << "lower( ${joinName}.name ) LIKE :search"
				else
					whereClause << "lower( ${joinName} ) LIKE :search"
					
				hqlParams[ "search" ] = "%" + searchTerm + "%"
			}
		}
		
		// Add ordering; to determine the column to sort on, we take into account that the
		// first column doesn't contain data, but only contains the checkbox
		def sortColumnIndex = Math.max( ( params.int( "iSortCol_0" ) ?: 1 ) - 1, 0 )
		def sortOrder = params[ "sSortDir_0"] ?: "ASC"
		
		if( sortColumnIndex != null || sortColumnIndex >= ( domainFields.size() + template.fields.size() ) ) {
			if( sortColumnIndex < domainFields.size() ) {
				def sortOn = domainFields[ sortColumnIndex ]?.name;
				orderBy = " ORDER BY s." + sortOn + " " + sortOrder
			} else {
				// Sort on template field: use a join in the sql
				// select * from subjects inner join template_fields sortField on ....
				def sortField = template.fields[ sortColumnIndex - domainFields.size() ]
				def store = "template${sortField.type.casedName}Fields"
				
				joins << "s." + store + " as orderJoin WITH index( orderJoin ) = :sortField"
				hqlParams[ "sortField" ] = sortField.name
				orderBy = " ORDER BY orderJoin " + sortOrder
			}
		}
			
		// Now build up the query, except for the SELECT part. 
		def hql = from 
		if( joins )
			hql += " LEFT JOIN " + joins.join( " LEFT JOIN " )
		
		hql +=  " WHERE s.parent = :study "
			
		if( whereClause )
			hql += " AND (" + whereClause.join( " OR " ) + ") "
			

		// First select the number of results
		def numResults = Subject.executeQuery( "SELECT COUNT(s) " + hql, hqlParams );
		output.iTotalDisplayRecords = numResults[ 0 ]
		
		// Now find the results themselves
		def query = "SELECT s " + hql + " " + orderBy
		def results = Subject.executeQuery( query, hqlParams, [ max: limit, offset: offset ] )

		output.aaData = results.collect { subject ->
			def data = [
				g.checkBox( name: "id", value: subject.id, checked: false, onClick: "updateCheckAll(this);" )
			]
			
			subject.giveFields().each { field ->
				def value = subject.getFieldValue( field.name )
				data << ( value ? value.toString() : "" ) 
			}
			data
		}
		
		
		render output as JSON
	}
	
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
				it.save();
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
	def assays() {}
	
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
