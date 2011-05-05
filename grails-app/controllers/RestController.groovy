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
import dbnp.rest.common.CommunicationManager
import org.springframework.security.core.context.SecurityContextHolder;

class RestController {

	/**************************************************/
	/** Rest resources for Simple Assay Module (SAM) **/
	/**************************************************/

	def authenticationService
	def beforeInterceptor = [action:this.&auth,except:["isUser"]]
	def credentials
	def requestUser

	/**
	 * Authorization closure, which is run before executing any of the REST resource actions
	 * It fetches a consumer/token combination from the url and checks whether
	 * that is a correct and known combination
	 *
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return  true if the user is remotely logged in, false otherwise
	 */
	private def auth() {
		if( !authenticationService.isRemotelyLoggedIn( params.consumer, params.token ) ) {
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
		boolean isUser = authenticationService.isRemotelyLoggedIn( params.consumer, params.token )
		def reply = ['authenticated':isUser]

		// set output header to json
		response.contentType = 'application/json'

		render reply as JSON
	}

	/**
	 * REST resource for data modules.
	 * Consumer and token should be supplied via URL parameters.
	 * Provides the details of the user that has logged in
	 *
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return bool {"username": "...", "id": ... } when user/password is logged in.
	 */
	def getUser = {
		SecUser user = authenticationService.getRemotelyLoggedInUser( params.consumer, params.token )
		def reply = [username: user.username, id: user.id, isAdministrator: user.hasAdminRights() ]

		// set output header to json
		response.contentType = 'application/json'

		render reply as JSON
	}

	/**
	 * REST resource for data modules.
	 * Consumer and token should be supplied via URL parameters.
	 * Provide a list of all studies owned by the supplied user.
	 *
	 * @param	studyToken  optional parameter. If no studyToken is given, all studies available to user are returned.
	 *                      Otherwise, the studies for which the studyTokens are given are be returned. 
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return  JSON object list containing 'studyToken', and 'name' (title) for each study
	 *
	 * If one study is requested, a 404 error might occur if the study doesn't exist, and a 401 error if the user is not
	 * authorized to access this study. If multiple studies are requrested, non-existing studies or studies for which the 
	 * user is not authorized are not returned in the list (so the list might be empty).
	 *
	 * Example 1. REST call without studyToken. 
	 * 
	 * Call: http://localhost:8080/gscf/rest/getStudies/query 
	 *
	 * Result: [{"title":"NuGO PPS3 mouse study leptin module","studyToken":"PPS3_leptin_module",
	 * 			"startDate":"2008-01-01T23:00:00Z","published":false,"Description":"C57Bl/6 mice were fed a high fat (45 en%) 
	 * 			or low fat (10 en%) diet after a four week run-in on low fat diet.","Objectives":null,"Consortium":null,
	 *			"Cohort name":null,"Lab id":null,"Institute":null,"Study protocol":null},
	 *			{"title":"NuGO PPS human study","studyToken":"PPSH","startDate":"2008-01-13T23:00:00Z","published":false,
	 *			"Description":"Human study performed at RRI; centres involved: RRI, IFR, TUM, Maastricht U.","Objectives":null,
	 *			"Consortium":null,"Cohort name":null,"Lab id":null,"Institute":null,"Study protocol":null}]
	 *
	 *
	 * Example 2. REST call with one studyToken. 
	 * 
	 * Call: http://localhost:8080/gscf/rest/getStudies/query?studyToken=PPSH
	 *
	 * Result: [{"title":"NuGO PPS human study","studyToken":"PPSH","startDate":"2008-01-13T23:00:00Z",
	 * 		"published":false,"Description":"Human study performed at RRI; centres involved: RRI, IFR, TUM, Maastricht U.",
	 * 		"Objectives":null,"Consortium":null,"Cohort name":null,"Lab id":null,"Institute":null,"Study protocol":null}]
	 *
	 *
	 *
	 * Example 2. REST call with two studyTokens. 
	 *
	 * http://localhost:8080/gscf/rest/getStudies/query?studyToken=PPSH&studyToken=PPS3_leptin_module
	 *
	 * Result: same as result of Example 1. 
	 */
	def getStudies = {

		List returnStudies = []
		List studies = []

		if( !params.studyToken ) {
			studies = Study.findAll()
		}
		else if( params.studyToken instanceof String ) {
			def study = Study.findByStudyUUID( params.studyToken )
			if( study ) {
				if( !study.canRead(authenticationService.getRemotelyLoggedInUser( params.consumer, params.token )) ) {
					response.sendError(401)
					return false
				}

				studies.push study
			} else {
				response.sendError(404)
				return false
			}

		}
		else {
			params.studyToken.each{ studyToken ->
				def study = Study.findByStudyUUID( studyToken );
				if( study )
					studies.push study
			}
		}


		studies.each { study ->
			if(study) {
				def user = authenticationService.getRemotelyLoggedInUser( params.consumer, params.token )
				// Check whether the person is allowed to read the data of this study
				if( study.canRead(authenticationService.getRemotelyLoggedInUser( params.consumer, params.token ))) {

					def items = [studyToken:study.giveUUID()]
					study.giveFields().each { field ->
						def name = field.name
						def value = study.getFieldValue( name )
						items[name] = value
					}

					// Add study version number
					items['version'] = study.version;

					returnStudies.push items
				}
			}
		}

		// set output header to json
		response.contentType = 'application/json'

		render returnStudies as JSON
	}

	/**
	 * REST resource for data modules.
	 * Consumer and token should be supplied via URL parameters.
	 * Provides the version number of the specified study
	 *
	 * @param	studyToken  optional parameter. If no studyToken is given, a 400 error is given
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return  JSON object list containing 'studyToken', and 'version'
	 *
	 * A 404 error might occur if the study doesn't exist, and a 401 error if the user is not
	 * authorized to access this study. 
	 *
	 * Example. REST call with one studyToken.
	 *
	 * Call: http://localhost:8080/gscf/rest/getStudyVersion?studyToken=PPSH
	 *
	 * Result: {"studyToken":"PPSH","version":31}
	 */
	def getStudyVersion = {

		def versionInfo = [:];
		def study

		if( !params.studyToken || !(params.studyToken instanceof String)) {
			response.sendError(400)
			return false
		} else {
			study = Study.findByStudyUUID( params.studyToken )
			if( study ) {
				if( !study.canRead(authenticationService.getRemotelyLoggedInUser( params.consumer, params.token )) ) {
					response.sendError(401)
					return false
				}
			} else {
				response.sendError(404)
				return false
			}
		}

		versionInfo[ 'studyToken' ] = params.studyToken;
		versionInfo[ 'version' ] = study.version;

		// set output header to json
		response.contentType = 'application/json'

		render versionInfo as JSON
	}

	/**
	 * REST resource for data modules.
	 * Consumer and token should be supplied via URL parameters.
	 * Provide a list of all subjects belonging to a study.
	 *
	 * If the user is not allowed to read the study contents, a 401 error is given. If the study doesn't exist, a 404 error is given
	 *
	 * @param	studyToken	String The external study id (code) of the target GSCF Study object
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return JSON object list of subject names
	 */
	def getSubjects = {
		List subjects = []
		if( params.studyToken ) {
			def study = Study.findByStudyUUID( params.studyToken)

			if(study) {
				// Check whether the person is allowed to read the data of this study
				if( !study.canRead(authenticationService.getRemotelyLoggedInUser( params.consumer, params.token ))) {
					response.sendError(401)
					return false
				}

				study.subjects.each { subjects.push it.name }
			} else {
				response.sendError(404)
				return false
			}
		}

		// set output header to json
		response.contentType = 'application/json'

		render subjects as JSON
	}


	/**
	 * REST resource for data modules.
	 * Consumer and token should be supplied via URL parameters.
	 * Provide a list of all assays for a given study.
	 *
	 * If the user is not allowed to read the study contents, a 401 error is given. If the study doesn't exist, a 404 error is given
	 *
	 * @param	studyToken	String The external study id (code) of the target GSCF Study object
	 * @param	consumer	consumer name of the calling module
	 * @return list of assays in the study as JSON object list, filtered to only contain assays
	 *         for the specified module, with 'assayToken' and 'name' for each assay
	 *
	 *
	 * Example 1. REST call without assayToken 
	 *            http://localhost:8080/gscf/rest/getAssays/aas?studyToken=PPSH
	 *				&consumer=http://localhost:8182/sam
	 *
	 * Result: [{"name":"Glucose assay after",
	 *		        "module":{"class":"dbnp.studycapturing.AssayModule","id":1,"name":"SAM module for clinical data",
	 *				"platform":"clinical measurements","url":"http://localhost:8182/sam"},
	 *			"externalAssayID":"PPSH-Glu-A", "Description":null,"parentStudyToken":"PPSH"},
	 *			{"name":"Glucose assay before",
	 *				"module":{"class":"dbnp.studycapturing.AssayModule","id":1,"name":"SAM module for clinical data",
	 *				"platform":"clinical measurements","url":"http://localhost:8182/sam"},
	 *				"externalAssayID":"PPSH-Glu-B","Description":null,"parentStudyToken":"PPSH"}]
	 *
	 *
	 * Example 2. REST call with one assayToken 
	 * 			  http://localhost:8080/gscf/rest/getAssays/queryOneTokenz?studyToken=PPSH
	 *				&consumer=http://localhost:8182/sam&assayToken=PPSH-Glu-A
	 *
	 * Result: [{"name":"Glucose assay after","module":{"class":"dbnp.studycapturing.AssayModule","id":1,
	 *			"name":"SAM module for clinical data","platform":"clinical measurements","url":"http://localhost:8182/sam"},
	 *			"externalAssayID":"PPSH-Glu-A","Description":null,"parentStudyToken":"PPSH"}]
	 *
	 *
	 * Example 3. REST call with two assayTokens.
	 *
	 * Result: Same as result in Example 1.
	 */
	def getAssays = {
		// set output header to json
		response.contentType = 'application/json'

		List returnList = []    // return list of hashes each containing fields and values belonging to an assay

		// Check if required parameters are present
		def validCall = CommunicationManager.hasValidParams( params, "consumer" )
		if( !validCall ) {
			render "Error. Wrong or insufficient parameters." as JSON
			return
		}
		
		def assays = []
		
		if( params.studyToken ) {

			def study = Study.findByStudyUUID(params.studyToken)

			if(study) {
				// Check whether the person is allowed to read the data of this study
				if( !study.canRead(authenticationService.getRemotelyLoggedInUser( params.consumer, params.token ))) {
					response.sendError(401)
					return false
				}

				if(params.assayToken==null) {
					assays = study.assays
				}
				else if( params.assayToken instanceof String ) {
					def assay = study.assays.find{ it.giveUUID() == params.assayToken }
					if( assay ) {
						assays.push assay
					}
				}
				else { 													// there are multiple assayTokens instances
					params.assayToken.each { assayToken ->
						def assay = study.assays.find{ it.giveUUID() == assayToken }
						if(assay) {
							assays.push assay
						}
					}
				}

			} else {
				response.sendError(404)
				return false
			}

		} else {
			// Return all assays for the given module
			assays = Assay.list().findAll{ it.parent.canRead(authenticationService.getRemotelyLoggedInUser( params.consumer, params.token ) ) }
		}

		// Create data for all assays
		assays.each{ assay ->
			if (assay.module?.url && assay.module.url.equals(params.moduleURL)) {
				if(assay) {
					def map = [assayToken : assay.giveUUID()]
					assay.giveFields().each { field ->
						def name = field.name
						def value = assay.getFieldValue( name )
						map[name] = value
					}
					map["parentStudyToken"] = assay.parent.giveUUID()
					returnList.push( map )
				}
			}
		}

		render returnList as JSON
	}

	/**
	 * REST resource for data modules.
	 * Provide all samples of a given Assay. The result is an enriched list with additional information for each sample.
	 *
	 * If the user is not allowed to read the study contents, a 401 error is given. If the assay doesn't exist, a 404 error is given
	 * 
	 * @param	assayToken	String (assayToken of some Assay in GSCF)
	 * @param	sampleToken Optional parameter. One or more sampleTokens to specify what sample to give exectly. 
	 * 			If not given, return all samples for specified assay.
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return As a JSON object list, for each sample in that assay:
	 * @return 'name' (Sample name, which is unique)
	 * @return 'material' (Sample material)
	 * @return 'subject' (The name of the subject from which the sample was taken)
	 * @return 'event' (the name of the template of the SamplingEvent describing the sampling)
	 * @return 'startTime' (the time the sample was taken relative to the start of the study, as a string)
	 * @return additional template fields are returned
	 * 
	 * 
	 * 
	 * Example 1: no sampleTokens given.
	 * Query: 
	 * http://localhost:8080/gscf/rest/getSamples/query?assayToken=PPSH-Glu-A
	 * 
	 * Result: 
	 * [{"sampleToken":"5_A","material":"blood plasma","subject":"5","event":"Blood extraction","startTime":"4 days, 6 hours"},
	 * {"sampleToken":"6_A","material":"blood plasma","subject":"6","event":"Blood extraction","startTime":"4 days, 6 hours"},
	 * {"sampleToken":"10_A","material":"blood plasma","subject":"10","event":"Blood extraction","startTime":"4 days, 6 hours"},
	 * {"sampleToken":"2_A","material":"blood plasma","subject":"2","event":"Blood extraction","startTime":"4 days, 6 hours"},
	 * {"sampleToken":"11_A","material":"blood plasma","subject":"11","event":"Blood extraction","startTime":"4 days, 6 hours"},
	 * {"sampleToken":"1_A","material":"blood plasma","subject":"1","event":"Blood extraction","startTime":"4 days, 6 hours"},
	 * {"sampleToken":"9_A","material":"blood plasma","subject":"9","event":"Blood extraction","startTime":"4 days, 6 hours"},
	 * {"sampleToken":"4_A","material":"blood plasma","subject":"4","event":"Blood extraction","startTime":"4 days, 6 hours"},
	 * {"sampleToken":"8_A","material":"blood plasma","subject":"8","event":"Blood extraction","startTime":"4 days, 6 hours"},
	 * {"sampleToken":"7_A","material":"blood plasma","subject":"7","event":"Blood extraction","startTime":"4 days, 6 hours"},
	 * {"sampleToken":"3_A","material":"blood plasma","subject":"3","event":"Blood extraction","startTime":"4 days, 6 hours"}]
	 * 
	 * 
	 * 
	 * Example 2: one sampleToken given.
	 * Query: 
	 * http://localhost:8080/gscf/rest/getSamples/query?assayToken=PPSH-Glu-A&sampleToken=5_A
	 * 
	 * Result: 
	 * [{"sampleToken":"5_A","material":"blood plasma","subject":"5","event":"Blood extraction","startTime":"4 days, 6 hours"}]
	 * 
	 * 
	 * 
	 * Example 3: two sampleTokens given.
	 * Query: 
	 * http://localhost:8080/gscf/rest/getSamples/query?assayToken=PPSH-Glu-A&sampleToken=5_A&sampleToken=6_A
	 * 
	 * Result: 
	 * [{"sampleToken":"5_A","material":"blood plasma","subject":"5","event":"Blood extraction","startTime":"4 days, 6 hours"},
	 *  {"sampleToken":"6_A","material":"blood plasma","subject":"6","event":"Blood extraction","startTime":"4 days, 6 hours"}]
	 *
	 *
	 * Example 4: no assaytoken given
	 * Query: 
	 * http://localhost:8080/gscf/rest/getSamples/query?sampleToken=5_A&sampleToken=6_A
	 * 
	 * Result: 
	 * [{"sampleToken":"5_A","material":"blood plasma","subject":"5","event":"Blood extraction","startTime":"4 days, 6 hours"},
	 *  {"sampleToken":"6_A","material":"blood plasma","subject":"6","event":"Blood extraction","startTime":"4 days, 6 hours"}]
	 *
	 */
	def getSamples = {
		def items = []
		def samples
		if( params.assayToken ) {
			def assay = Assay.findByAssayUUID( params.assayToken );

			if( assay )  {
				// Check whether the person is allowed to read the data of this study
				if( !assay.parent.canRead(authenticationService.getRemotelyLoggedInUser( params.consumer, params.token ))) {
					response.sendError(401)
					return false
				}

				samples = assay.getSamples() // on all samples
			} else {
				// Assay not found
				response.sendError(404)
				return false
			}
		} else {
			// Find all samples from studies the user can read
			def studies = Study.list().findAll { it.canRead( authenticationService.getRemotelyLoggedInUser( params.consumer, params.token ) ) };
			samples = studies*.getSamples().flatten();
		}

		// Check whether only a subset of samples should be returned
		if( params.sampleToken ) {
			def sampleTokens = params.list( "sampleToken" );
			samples = samples.findAll { sampleTokens.contains( it.giveUUID() ) }
		}

		samples.each { sample ->

			def item = [
						'sampleToken' : sample.giveUUID(),
						'material'	  : sample.material?.name,
						'subject'	  : sample.parentSubject?.name,
						'event'		  : sample.parentEvent?.template?.name,
						'startTime'	  : sample.parentEvent?.getStartTimeString()
					]

			sample.giveFields().each { field ->
				def name = field.name
				def value = sample.getFieldValue( name )
				if(name!='material')
				{
					item[name]=value
				}
			}

			if(sample.parentEvent) {
				def parentEvent = sample.parentEvent
				def eventHash = [:]
				parentEvent.giveFields().each { field ->
					def name = field.name
					if( name !='sampleTemplate' && name != 'fields') {
						def value = parentEvent.getFieldValue( name )
						eventHash[name]=value
					}
				}
				item['eventObject'] = eventHash
			}

			if(sample.parentSubject) {
				def parentSubject = sample.parentSubject
				def subject = [:]
				parentSubject.giveFields().each { field ->
					def name = field.name
					if( name!='fields') {
						def value = parentSubject.getFieldValue( name )
						subject[name]=value
					}
				}
				item['subjectObject'] = subject
			}

			items.push item
		}

		// set output header to json
		response.contentType = 'application/json'

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
			def study = Study.findByStudyUUID(params.studyToken)

			if( !study ) {
				response.sendError(404)
				return false
			}

			def user = authenticationService.getRemotelyLoggedInUser( params.consumer, params.token );
			def auth = ['isOwner': study.isOwner(user), 'canRead': study.canRead(user), 'canWrite': study.canWrite(user)];
			log.trace "Authorization for study " + study.title + " and user " + user.username + ": " + auth

			// set output header to json
			response.contentType = 'application/json'

			render auth as JSON;
		} else {
			response.sendError(400)
			return false
		}
	}
}