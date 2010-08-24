/**
 * RestControler
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
import grails.converters.*
import nl.metabolomicscentre.dsp.http.BasicAuthentication


class RestController {



       /**************************************************/
      /** Rest resources for Simple Assay Module (SAM) **/
     /**************************************************/

	def authService
	def beforeInterceptor = [action:this.&auth,except:["isUser"]]
	def credentials
	def requestUser

	/**
	 * Authorization closure, which is run before executing any of the REST resource actions
	 * It fetches a username/password combination from basic HTTP authentication and checks whether
	 * that is an active (nimble) account
	 * @return
	 */
	private def auth() {
	    credentials = BasicAuthentication.credentialsFromRequest(request)
		requestUser = authService.authUser(credentials.u,credentials.p)
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
	* @return bool True when user/password is a valid GSCF account, false otherwise.
	*/
	def isUser= {
		boolean isUser
		def reqUser = authService.authUser(credentials.u,credentials.p)
		if (reqUser) {
			isUser = true
		}
		else {
			isUser = false
		}
		render isUser as JSON
	}

	/**
	* REST resource for data modules.
	* Username and password should be supplied via HTTP Basic Authentication.
	* Provide a list of all studies owned by the supplied user.
	*
	* @return JSON object list containing 'externalStudyID', and 'name' (title) for each study
	*/
	def getStudies = {
		List studies = [] 
		Study.findAllByOwner(requestUser).each { study ->
			studies.push( [ 'externalStudyID': study.code, 'name':study.title ] )
		}
 		render studies as JSON 
	}


	/**
	* REST resource for data modules.
	* Username and password should be supplied via HTTP Basic Authentication.
	* Provide a list of all subjects belonging to a study.
	*
	* @param externalStudyID String The external study id (code) of the target GSCF Study object
	* @return JSON object list of subject names
	*/
	def getSubjects = {
		List subjects = [] 
		if( params.externalStudyID ) {
			def id = params.externalStudyID
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
	* Example call of the getAssays REST resource: http://localhost:8080/gscf/rest/getAssays?externalStudyID=PPSH&moduleURL=http://localhost:8182/sam
	*
	* @param externalStudyID String The external study id (code) of the target GSCF Study object
	* @param moduleURL String The base URL of the calling dbNP module
	* @return list of assays in the study as JSON object list, filtered to only contain assays for the specified module, with 'externalAssayID' and 'name' for each assay
	*/
	def getAssays = {
		List assays = [] 
		if( params.externalStudyID ) {
 			def study = Study.find( "from Study as s where s.code=?", [params.externalStudyID])
			if(study && study.owner == requestUser) study.assays.each{ assay ->
				if (assay.module.url.equals(params.moduleURL)) {
			        def map = ['name':assay.name, 'externalAssayID':assay.externalAssayID]
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
	* @param externalAssayID String (externalAssayID of some Assay in GSCF)
	* @return As a JSON object list, for each sample in that assay:
	* @return 'name' (Sample name, which is unique)
	* @return 'material' (Sample material)
	* @return 'subject' (The name of the subject from which the sample was taken)
	* @return 'event' (the name of the template of the SamplingEvent describing the sampling)
	* @return 'startTime' (the time the sample was taken relative to the start of the study, as a string)
	*/
	def getSamples = {
		def items = []
		if( params.externalAssayID ) {
 			def assay = Assay.find( "from Assay as a where externalAssayID=?",[params.externalAssayID])
			assay.getSamples().each { sample ->
				def item = [ 
					'name'		      : sample.name,
					'material'	      : sample.material.name,
					'subject'	      : sample.parentSubject.name,
					'event'		      : sample.parentEvent.template.name,
					'startTime'	      : sample.parentEvent.getStartTimeString()
				]
				items.push item 
			}
 		}
		render items as JSON
	}


    /* this is just for testing! */
    /*def test = {
		render( dbnp.rest.common.CommunicationManager.getQueryResultWithOperator("Insulin",">",200) )
    }*/
}
