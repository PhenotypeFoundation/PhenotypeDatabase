/**
 * RestController
 *
 * This controler provides a REST service.
 * The names of the RESET resources are the same as the names of this
 * controller's actions. E.g., the resources called getStudies simply
 * corresponds to the action getStudies. Some of the resources are parameterized. 
 * The parameters are passed as parameters in the url and are available in the
 * params respecting Grails' conventions. In this file, we adher to the javadoc  
 * convention for describing parameters ("@param"), but actually we mean
 * key-value pairs in the params object of each Grails action we comment on.
 * 
 * @author	Jahn-Takeshi Saito
 * @since	20100601
 *
 */

import dbnp.studycapturing.Study
import dbnp.studycapturing.Assay
import dbnp.authentication.SecUser
import grails.converters.*
import nl.metabolomicscentre.dsp.http.BasicAuthentication


class RestController {

       /**************************************************/
      /** Rest resources for Simple Assay Module (SAM) **/
     /**************************************************/

	def AuthenticationService        
	def beforeInterceptor = [action:this.&auth,except:["isUser"]]
	def credentials
	def requestUser // = SecUser.findByName( "user" )

	/**
	 * Authorization closure, which is run before executing any of the REST resource actions
	 * It fetches a consumer/token combination from the url and checks whether
	 * that is a correct and known combination
	 *
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return true if the user is remotely logged in, false otherwise
	 */
	private def auth() {
		if( !AuthenticationService.isRemotelyLoggedIn( params.consumer, params.token ) ) {
			response.sendError(403)
			return false
		} else {
			return true
		}
	}

	/**
	 * REST resource for data modules.
	 * Consumer and token should be supplied via URL parameters.
	 * Determines whether the given user/password combination is a valid GSCF account.
	 *
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return bool {"authenticated":true} when user/password is a valid GSCF account, {"authenticated":false} otherwise.
	 */
	def isUser = {
		boolean isUser = AuthenticationService.isRemotelyLoggedIn( params.consumer, params.token )
		def reply = ['authenticated':isUser]
		render reply as JSON
	}


	/**
 	 * REST resource for data modules.
 	 * Consumer and token should be supplied via URL parameters.
 	 * Provide a list of all studies owned by the supplied user.
	 *
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return JSON object list containing 'studyToken', and 'name' (title) for each study
	 */
	def getStudies = {
		List studies = [] 
		Study.findAllByOwner(AuthenticationService.getRemotelyLoggedInUser( params.consumer, params.token )).each { study ->
			studies.push( [ 'title':study.title, 'studyToken':study.getToken()] )
		}
 		render studies as JSON 
	}


	/**
	 * REST resource for data modules.
	 * Consumer and token should be supplied via URL parameters.
	 * Provide a list of all subjects belonging to a study.
	 *
	 * If the user is not allowed to read the study contents, a 401 error is given
	 *
	 * @param	studyToken	String The external study id (code) of the target GSCF Study object
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return JSON object list of subject names
	 */
	def getSubjects = {
		List subjects = [] 
		if( params.studyToken ) {
			def id = params.studyToken
 			def study = Study.find( "from Study as s where s.code=?", [id])

			if(study) {
				// Check whether the person is allowed to read the data of this study
				if( !study.canRead(AuthenticationService.getRemotelyLoggedInUser( params.consumer, params.token ))) {
					response.sendError(401)
					return false
				}

				study.subjects.each { subjects.push it.name }
			}
		}
		render subjects as JSON 
	}


	/**
	 * REST resource for data modules.
	 * Consumer and token should be supplied via URL parameters.
	 * Provide a list of all assays for a given study.
	 *
	 * If the user is not allowed to read the study contents, a 401 error is given
	 *
	 * Example call of the getAssays REST resource:
	 * http://localhost:8080/gscf/rest/getAssays?studyToken=PPSH&moduleURL=http://localhost:8182/sam
	 *
	 * @param	studyToken	String The external study id (code) of the target GSCF Study object
	 * @param	moduleURL	String The base URL of the calling dbNP module
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return list of assays in the study as JSON object list, filtered to only contain assays
	 *         for the specified module, with 'assayToken' and 'name' for each assay
	 */
	def getAssays = {
		List assays = [] 
		if( params.studyToken ) {
			def id = params.studyToken
 			def study = Study.find( "from Study as s where s.code=?", [id] )

			if(study) {
				// Check whether the person is allowed to read the data of this study
				if( !study.canRead(AuthenticationService.getRemotelyLoggedInUser( params.consumer, params.token ))) {
					response.sendError(401)
					return false
				}

				study.assays.each{ assay ->
					if (assay.module.url.equals(params.moduleURL)) {
						def map = ['name':assay.name, 'assayToken':assay.getToken()]
						assays.push( map )
					}
				}
			}
 		}
		render assays as JSON 
	}


	/**
	 * REST resource for data modules.
	 * Username and password should be supplied via HTTP Basic Authentication.
	 * Provide all samples of a given Assay. The result is an enriched list with additional information for each sample.
	 *
	 * @param	assayToken	String (assayToken of some Assay in GSCF)
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return As a JSON object list, for each sample in that assay:
	 * @return 'name' (Sample name, which is unique)
	 * @return 'material' (Sample material)
	 * @return 'subject' (The name of the subject from which the sample was taken)
	 * @return 'event' (the name of the template of the SamplingEvent describing the sampling)
	 * @return 'startTime' (the time the sample was taken relative to the start of the study, as a string)
	 */
	def getSamples = {
		def items = []
		if( params.assayToken ) {
 			def assay = Assay.find( "from Assay as a where externalAssayID=?",[params.assayToken])
			if( assay )  {
				assay.getSamples().each { sample ->
					def item = [ 
						'sampleToken' : sample.name,
						'material'	  : sample.material?.name,
						'subject'	  : sample.parentSubject?.name,
						'event'		  : sample.parentEvent?.template?.name,
						'startTime'	  : sample.parentEvent?.getStartTimeString()
					]
					items.push item 
				}
			}
 		}
		render items as JSON
	}


	/**
	 * REST resource for dbNP modules.
	 *
	 * @param	studyToken String, the external identifier of the study
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return List of all fields of this study
	 * @return
	 *
	 * If the user is not allowed to read this study, a 401 error is given
	 *
	 * Example REST call (without authentication):
     * http://localhost:8080/gscf/rest/getStudy/study?studyToken=PPSH
     *
	 * Returns the JSON object:
	 * {"title":"NuGO PPS human study","studyToken":"PPSH","startDate":"2008-01-13T23:00:00Z",
	 * "Description":"Human study performed at RRI; centres involved: RRI, IFR, TUM, Maastricht U.",
	 * "Objectives":null,"Consortium":null,"Cohort name":null,"Lab id":null,"Institute":null,
	 * "Study protocol":null}
	*/
	def getStudy = {
		def items = [:]
		if( params.studyToken ) {
 			def study = Study.find( "from Study as s where code=?",[params.studyToken])
			if(study) {
				// Check whether the person is allowed to read the data of this study
				if( !study.canRead(AuthenticationService.getRemotelyLoggedInUser( params.consumer, params.token ))) {
					response.sendError(401)
					return false
				}
				
				study.giveFields().each { field ->
					def name = field.name
					def value = study.getFieldValue( name )
					items[name] = value
				}
			}
        }
		render items as JSON
	}



	/**
	 * REST resource for dbNP modules.
	 *
	 * @param	assayToken String, the external identifier of the study
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return List of all fields of this assay
	 *
	 * Example REST call (without authentication):
     * http://localhost:8080/gscf/rest/getAssay/assay?assayToken=PPS3_SAM
     *
	 * Returns the JSON object: {"name":"Lipid profiling","module":{"class":"dbnp.studycapturing.AssayModule","id":1,
	 * "name":"SAM module for clinical data","platform":"clinical measurements","url":"http://sam.nmcdsp.org"},
	 * "assayToken":"PPS3_SAM","parentStudyToken":"PPS","Description":null}
	 */
	def getAssay = {
		def items = [:]
		if( params.assayToken ) {
 			def assay = Assay.find( "from Assay as a where externalAssayID=?",[params.assayToken])
			if(assay) {
				assay.giveFields().each { field ->
					def name = field.name
					def value = assay.getFieldValue( name )
					items[name] = value
				}
				items["parentStudyToken"] = assay.parent.getToken()
			}
        }
		render items as JSON
	}



	/**
	 * REST resource for data modules.
	 * Username and password should be supplied via HTTP Basic Authentication.
	 * One specific sample of a given Assay.
	 *
	 * @param	assayToken	String (id of some Assay in GSCF)
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return As a JSON object list, for each sample in that assay:
	 * @return 'name' (Sample name, which is unique)
	 * @return 'material' (Sample material)
	 * @return 'subject' (The name of the subject from which the sample was taken)
	 * @return 'event' (the name of the template of the SamplingEvent describing the sampling)
	 * @return 'startTime' (the time the sample was taken relative to the start of the study, as a string)
	 *
	 * Example REST call (without authentication):
     * http://localhost:8080/gscf/rest/getSample/sam?assayToken=PPS3_SAM&sampleToken=A30_B
     *
	 * Returns the JSON object:
	 * {"subject":"A30","event":"Liver extraction","startTime":"1 week, 1 hour",
	 * "sampleToken":"A30_B","material":{"class":"dbnp.data.Term","id":6,"accession":"BTO:0000131",
	 * "name":"blood plasma","ontology":{"class":"Ontology","id":2}},"Remarks":null,
	 * "Text on vial":"T70.91709057820039","Sample measured volume":null}
	 */
	def getSample = {
		def items = [:]
		if( params.assayToken && params.sampleToken ) {
 			def assay = Assay.find( "from Assay as a where externalAssayID=?",[params.assayToken])
			if(assay) {
				assay.getSamples().each { sample ->
					if( sample.name == params.sampleToken ) {
							items = [ 
							'subject'	      : sample.parentSubject.name,
							'event'		      : sample.parentEvent.template.name,
							'startTime'	      : sample.parentEvent.getStartTimeString()
							]
							sample.giveFields().each { field ->
							def name = field.name
							def value = sample.getFieldValue( name )
							items[name] = value
            			}
					}
				}
			}
 		}
		render items as JSON
	}

	/**
	 * Returns the authorization level the user has for a given study.
	 *
	 * If no studyToken is given, a 400 (Bad Request) error is given.
	 * If the given study doesn't exist, a 404 (Not found) error is given.
	 *
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return	JSON Object
	 * @return  { isOwner: true/false, 'canRead': true/false, 'canWrite': true/false }
	 */
	def getAuthorizationLevel = {
		if( params.studyToken ) {
			def id = params.studyToken
 			def study = Study.find( "from Study as s where s.code=?", [id])

			if( !study ) {
				response.sendError(404)
				return false
			}

			def user = AuthenticationService.getRemotelyLoggedInUser( params.consumer, params.token );
			render( 'isOwner': study.isOwner(user), 'canRead': study.canRead(user), 'canWrite': study.canWrite(user) )
		} else {
			response.sendError(400)
			return false
		}
    }
}
