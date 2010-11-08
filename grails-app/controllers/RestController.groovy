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

	def AuthenticationService        
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
	 * Provides the details of the user that has logged in
	 *
	 * @param	consumer	consumer name of the calling module
	 * @param	token		token for the authenticated user (e.g. session_id)
	 * @return bool {"username": "...", "id": ... } when user/password is logged in.
	 */
	def getUser = {
		SecUser user = AuthenticationService.getRemotelyLoggedInUser( params.consumer, params.token )
		def reply = [username: user.username, id: user.id]
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
			studies.push Study.findByCode( params.studyToken ) 
		}
		else { 
			params.studyToken.each{ studyToken ->
				studies.push Study.findByCode( studyToken )
			}
		}

		studies.each { study ->
			if(study) {
				def user = AuthenticationService.getRemotelyLoggedInUser( params.consumer, params.token )
				// Check whether the person is allowed to read the data of this study
				if( study.canRead(AuthenticationService.getRemotelyLoggedInUser( params.consumer, params.token ))) {

                    def items = [:]
                    study.giveFields().each { field ->
                        def name = field.name
                        def value = study.getFieldValue( name )
                        if( name=='code' ) {
                            name = 'studyToken'
                        }
                        items[name] = value
                    }
                    returnStudies.push items
                }
			}
		}

 		render returnStudies as JSON 
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

		List returnList = []    // return list of hashes each containing fields and values belonging to an assay 

		// Check if required parameters are present 
		def validCall = CommunicationManager.hasValidParams( params, "consumer", "studyToken" )
		if( !validCall ) { 
			render "Error. Wrong or insufficient parameters." as JSON 
			return
		}

		if( params.studyToken ) {

			def id = params.studyToken
 			def study = Study.find( "from Study as s where s.code=?", [id] )

			if(study) {
				// Check whether the person is allowed to read the data of this study
				/*
				if( !study.canRead(AuthenticationService.getRemotelyLoggedInUser( params.consumer, params.token ))) {
					response.sendError(401)
					return false
				}
				*/

				def assays = []
				if(params.assayToken==null) {
					assays = study.assays
				}
				else if( params.assayToken instanceof String ) { 
					def assay = study.assays.find{ it.externalAssayID==params.assayToken }
					if( assay ) {
						 assays.push assay
					}
				}
				else { 													// there are multiple assayTokens instances
					params.assayToken.each { assayToken ->
						def assay = study.assays.find{ it.externalAssayID==assayToken }
						if(assay) {
							assays.push assay
						}
					}
				}

				assays.each{ assay ->
					if (assay.module.url.equals(params.consumer )) {
						if(assay) {
							def map = [:]
							assay.giveFields().each { field ->
								def name = field.name
								def value = assay.getFieldValue( name )
								if(field.name=='externalAssayID') {
									name = 'assayToken'
								}
								map[name] = value
							}
							map["parentStudyToken"] = assay.parent.getToken()
							returnList.push( map )
						}
					}
				}
        	}

 		}

		render returnList as JSON 
	}







	/**
	 * REST resource for data modules.
	 * Provide all samples of a given Assay. The result is an enriched list with additional information for each sample.
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
	 * http://localhost:8080/gscf/rest/getSamples/query?assayToken=PPSH-Glu-A&sampleToken=5_A
 	 * 
 	 * Result: 
	 * [{"sampleToken":"5_A","material":"blood plasma","subject":"5","event":"Blood extraction","startTime":"4 days, 6 hours"},
	 *  {"sampleToken":"6_A","material":"blood plasma","subject":"6","event":"Blood extraction","startTime":"4 days, 6 hours"}]
	 */
	def getSamples = {
		def items = []
		if( params.assayToken ) {
 			def assay = Assay.find( "from Assay as a where externalAssayID=?",[params.assayToken])
			if( assay )  {
				def samples = assay.getSamples() // on all samples

				if( params.sampleToken ) {       // or on a subset of samples?
					def sampleTokens = (params.sampleToken instanceof String) ? 
						[params.sampleToken] : params.sampleToken
					samples = []
					sampleTokens.each{ sampleToken ->
						assay.getSamples().find{ sample -> sampleToken == sample.name } 
					}
				}

				samples.each { sample ->

					def item = [ 
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
							if( name!='sampleTemplate' && name!='fields') {
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
			render( ['isOwner': study.isOwner(user), 'canRead': study.canRead(user), 'canWrite': study.canWrite(user)] as JSON )
		} else {
			response.sendError(400)
			return false
		}
    }






}
