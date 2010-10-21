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
import org.nmcdsp.plugins.aaaa.SecUser
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
	 * It fetches a username/password combination from basic HTTP authentication and checks whether
	 * that is an active (SecuritySpring) account
	 * @return
	 */
	private def auth() {
	    credentials = BasicAuthentication.credentialsFromRequest(request)		
        requestUser = AuthenticationService.authenticateUser(credentials.u, credentials.p)
                
		if(!requestUser) {
		    response.sendError(403)
	        return false
	    }
		else {
			return true
		}
	}

	/**
	* REST resource for data modules.
	* Username and password should be supplied via HTTP Basic Authentication.
	* Determines whether the given user/password combination is a valid GSCF account.
	*
	* @return bool {"authenticated":true} when user/password is a valid GSCF account, {"authenticated":false} otherwise.
	*/
	def isUser= {
		boolean isUser
		credentials = BasicAuthentication.credentialsFromRequest(request)
		def reqUser = AuthenticationService.authenticateUser(credentials.u, credentials.p)
		isUser = reqUser ? true : false
		def reply = ['authenticated':isUser]
		render reply as JSON
	}


	/**
	* REST resource for data modules.
	* Username and password should be supplied via HTTP Basic Authentication.
	* Provide a list of all studies owned by the supplied user.
	*
	* @return JSON object list containing 'studyToken', and 'name' (title) for each study
	*/
	def getStudies = {
		List studies = [] 
		def user = params.user
		Study.findAllByOwner(requestUser).each { study ->
			studies.push( [ 'title':study.title, 'studyToken':study.getToken()] )
		}
 		render studies as JSON 
	}


	/**
	* REST resource for data modules.
	* Username and password should be supplied via HTTP Basic Authentication.
	* Provide a list of all subjects belonging to a study.
	*
	* @param studyToken String The external study id (code) of the target GSCF Study object
	* @return JSON object list of subject names
	*/
	def getSubjects = {
		List subjects = [] 
		if( params.studyToken ) {
			def id = params.studyToken
 			def study = Study.find( "from Study as s where s.code=?", [id])
			if(study) study.subjects.each { subjects.push it.name }
		}
		render subjects as JSON 
	}


	/**
	* REST resource for data modules.
	* Username and password should be supplied via HTTP Basic Authentication.
	* Provide a list of all assays for a given study
	*
	* Example call of the getAssays REST resource: 
	* http://localhost:8080/gscf/rest/getAssays?studyToken=PPSH&moduleURL=http://localhost:8182/sam
	*
	* @param stuyToken String The external study id (code) of the target GSCF Study object
	* @param moduleURL String The base URL of the calling dbNP module
	* @return list of assays in the study as JSON object list, filtered to only contain assays 
	*         for the specified module, with 'assayToken' and 'name' for each assay
	*/
	def getAssays = {
		List assays = [] 
		if( params.studyToken ) {
			def id = params.studyToken
 			def study = Study.find( "from Study as s where s.code=?", [id] )
			if(study && study.owner == requestUser) study.assays.each{ assay ->
				if (assay.module.url.equals(params.moduleURL)) {
			        def map = ['name':assay.name, 'assayToken':assay.getToken()]
					assays.push( map )
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
	* @param assayToken String (assayToken of some Assay in GSCF)
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
	* @param studyToken String, the external identifier of the study
	* @return List of all fields of this study
	* @return 
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
	* @param assayToken String, the external identifier of the study
	* @return List of all fields of this assay 
	*
	* Example REST call (without authentication): 
    * http://localhost:8080/gscf/rest/getAssay/assay?assayToken=PPS3_SAM
    *
	* Returns the JSON object: {"name":"Lipid profiling","module":{"class":"dbnp.studycapturing.AssayModule","id":1,
	* "name":"SAM module for clinical data","platform":"clinical measurements","url":"http://sam.nmcdsp.org"},
	* "assayToken":"PPS3_SAM","Description":null}
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
			}
        }
		render items as JSON
	}



	/**
	* REST resource for data modules.
	* Username and password should be supplied via HTTP Basic Authentication.
	* One specific sample of a given Assay.
	*
	* @param assayToken String (id of some Assay in GSCF)
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

	def getAuthorizationLevel = {
		// in future the users authorization level will be based on authorization model		
		if( params.studyToken ) {
			def id = params.studyToken
 			def study = Study.find( "from Study as s where s.code=?", [id])
			if(study) study.subjects.each { subjects.push it.name }
		}

		def perm = study.getPermissions(requestUser)
                
		render ('isOwner': study.isOwner(requestUser),
                        'create': perm.create, 'read':perm.read,
                        'update': perm.update, 'delete':perm.delete
                        ) as JSON
    }
}
