package dbnp.studycapturing

import org.dbnp.gdt.*
import grails.plugins.springsecurity.Secured
import dbnp.authentication.SecUser
import grails.converters.JSON

/**
 * Controller to handle adding and editing studies
 * @author robert
 */
@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class StudyViewController {
	def authenticationService
	def datatablesService
	def studyEditService

	def index() {
		redirect controller: "study", action: "list"
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

		[ study: study, loggedInUser: authenticationService.getLoggedInUser() ]
	}

	/**
	 * Shows the overview page to edit subject details. 
	 * @return
	 */
	def subjects() {
		prepareDataForDatatableView( Subject )
	}

	def design() {
		def study = getStudyFromRequest( params )
		[study: study, loggedInUser: authenticationService.getLoggedInUser()]
	}
	
	/**
	 * Shows the overview page to edit subject details. 
	 * @return
	 */
	def samples() {
		prepareDataForDatatableView( Sample )
	}

	def assays() {
		prepareDataForDatatableView( Assay )
	}

	/**
	 * Returns data for a templated datatable. The type of entities is based on the template given.
	 * @return
	 */
	def dataTableEntities() {
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
		
		// We have to remove the id from each data item
		def datatableData = datatablesService.createDatatablesOutputForEntities( data, params )
		datatableData.aaData = datatableData.aaData.collect { it.tail() }
		
		render datatableData as JSON
	}

	/**
	 * Prepares the data for the datatable view
	 * @param entityClass	Class for the type of entities to show. E.g. Subject
	 * @return	a list of data to return to the view
	 */
	protected def prepareDataForDatatableView( entityClass ) {
		def study = getStudyFromRequest( params )
		if( !study ) {
			redirect action: "add"
			return
		}

		// Check the distinct templates for these entities, without loading all
		// entities for efficiency reasons
		def templates = entityClass.executeQuery("select distinct s.template from " + entityClass.simpleName + " s WHERE s.parent = ?", [ study ] )

		[
			study: study,
			templates: templates,
			domainFields: entityClass.domainFields,
			loggedInUser: authenticationService.getLoggedInUser()
		]

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
			redirect controller: "study", action: "list"
		} else if(!study.canRead(user)) {
			flash.error = "No authorization to view this study."
			study = null;
			redirect controller: "study", action: "list"
		}

		return study;
	}
}
