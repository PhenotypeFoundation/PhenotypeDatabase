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

import data.*
import dbnp.studycapturing.Study
import dbnp.studycapturing.Assay
import grails.converters.*
import org.codehaus.groovy.grails.web.json.*



class RestController {



       /**************************************************/
      /** Rest resources for Simple Assay Module (SAM) **/
     /**************************************************/
    


	/**
	* REST resource for the Simple Assay Module.
	* Provide a list of all studies. 
	*
	*
	* Examlpe call of the getAssays REST resource: http://localhost:8080/gscf/rest/getAssays/json?externalStudyID=1
	*
	* @return as JSON object list of members externalStudyID, and title for all studies
	*/
	def getStudies = {
		List studies = [] 
		Study.list().each { study ->
			studies.push( [ 'externalStudyID': study.code, 'name':study.title ] )
		}
 		render studies as JSON 
	}


	/**
	* REST resource for the Simple Assay Module.
	* Provide a list of all subjects belonging to a study. 
	*
	* @param  externalStudyID
	* @return as JSON object list of subject names 
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
	* REST resource for the Simple Assay Module.
	* Provide a list of all assays for a given study
	*
	* @param  externalStudyID 
	* @return list of assays as JSON object 
	*/
	def getAssays = {
		List assays = [] 
		if( params.externalStudyID ) {
 			def study = Study.find( "from Study as s where s.code=?", [params.externalStudyID])
			if(study) study.assays.each{ assay -> assays.push assay.externalAssayID }
 		}
		render assays as JSON 
	}


	/**
	* REST resource for the Simple Assay Module.
	* Provide all samples of a given Assay. The result is an enriched list with additional informatin on a sample. 
	*
	* @param  assayID (externalAssayID of some Assay in GSCF)
	* @return list of element of  Sample.name x Sample.material x Sample.subject.name x Sample.Event.name x Sample.Event.time
	*/
	def getSamples = {
		def items = []
		if( params.externalAssayID ) {
			def id = Long.parseLong(params.externalAssayID)
			Assay.findAll().each{ println it }
 			def assay = Assay.find( "from Assay as a where externalAssayID=?",[id])
			println "Assay: " + assay
			assay.getSamples().each { sample ->
				def item = [ 
					'name'		      : sample.name,
					'material'	      : sample.material.name,
					'subject'	      : sample.parentSubject.name,
					'event'		      : sample.parentEvent.template.name,
					'startTime'	      : sample.parentEvent.getDurationString(),
					'externalSampleId': sample.externalSampleId
				] 
				items.push item 
			}
 		}
		println "done"
		render items as JSON
	}





       /****************************/
      /** Rest resources for DSP **/
     /****************************/
    
	
	/* still not complete!! */

	/**
	* REST resource for DSP.
	* call: gscf/rest/isUser/?username=username&password=password
	*
	* @param  String username   
	* @param  String password
	* @return bool
	*/
	def isUser= {
		def isUser = isVerifiedUser( params )
		render isUser as JSON
	}


	/* still not complete!! */
	/**
	* REST resource for DSP.
	* call: gscf/rest/listStudies/?username=username&password=password 
	*
	* @param  String username
	* @param  String password
	* @return list of studies 
	*/
	def listStudies = {

		if( !isVerifiedUser( params ) ) {
			render [:] as JSON
			return
		}

		List studies = [] 

		// add code for filtering studies that belong to given user 
		// (use Study.findAll( ... )  
		// ... 
		Study.list().each { 
			def map = ["study_token":it.code, "name":it.name]
			studies.add( map )
		}
		
		render studies as JSON
	}



	/* still not complete!! */
	/**
	* REST resource for DSP.
	* call: gscf/rest/getStudy/?username=username&password=password&study_token=studytoken
	*
	* @param  String username
	* @param  String password
	* @param  String study_token 
	* @return list of studies 
	*/
	def getStudy = {

		if( !isVerifiedUser( params ) ) {
			render [:] as JSON
			return
		}

		List studyResult = [:] 
		def code   = params.study_token

		def query = "from Study as s where s.code = ?"
		def study = Study.find( query, code )
		studyResult = [ 'study_token' : study.code, 'name' : study.name ]
			/*  still not complete!! 
				Add features
					... study_token:”GHyJeR#g”, 
					... created: “20/06/2010 22:34:52”,
					... meta: [
					... greenhouse_id: “GH010938.AB.5”,
					... greenhouse_type: “lean-to, detached, and ridge and gutter connected” ]
			*/
		
		render studyResult as JSON
	}




	/* still not complete!! */
	/**
	* REST resource for DSP.
	* call: gscf/rest/listStudySamples/?username=username&password=password&study_token=studytoken
	*
	* @param  String user name
	* @param  String password 
	* @param  String a valid GSCF Study.code
	* @return List of pairs; each pair is a map with keys sample_token and name and values Study.code and Sample.name.
	*/
	def listStudySamples = {

		if( !isVerifiedUser( params ) ) {
			render [:] as JSON
			return
		}

		List samples = [:] 
		def code = params.study_token
		def query = "from Samples as s where s.study = ?"
		def study = Study.find( query, code )
		if(study) study.samples.each { sample ->
			def map = [ sample_token:code, name:sample.name ]
			samples.add( map )
		}
		
		render samples as JSON
	}



	/* still not complete!! */
	/**
	* REST resource for DSP.
	* call: getStudySample/?username=me&password=123&study_token=GHyJeR#g&sample_name=”AHVJwR”)
	*
	* @param  String username
	* @param  String password
	* @param  String study_token 
	* @param  String sample_name 
	* @return list of studies 
	*/
	def getStudySample = {

		if( !isVerifiedUser( params ) ) {
			render [:] as JSON
			return
		}

		List sample = [:] 
		def code = params.study_token
		def name = params.sample_name

		def query = "from Sample as s where s.name = ? AND s.parentStudy "
		def study = Sample.find( query, name )
		sample = [ 'study_token' : sample.code, 'name' : sample.name ]
		// samples will have unique identifier strings
		/*  still not complete!! 
				Add features
				[ 
					study_token:”GHyJeR#g”, 
					sample_token:”AHVJwR”, 
					name: “Sample SMPL002”,
					created: “25/06/2010 09:14:32”,
					meta: [ subject: “SUB000294-34942.A”, subject_bmi: “29.3”, ... study_token:”GHyJeR#g”, 
					... created: “20/06/2010 22:34:52”, greenhouse_id: “GH010938.AB.5”,
					greenhouse_type: “lean-to, detached, and ridge and gutter connected” 
				]
			*/
		render sample as JSON
	}




	/* still not complete!! */
	/** Convenience method for isUser and listStudies.
	*   Verify user and password.
	*   @param  params object with two map keys: (1) 'username', (2) 'password'
	*   @param  String password
	*   @return bool
	*/
	private isVerifiedUser( params ) {
		def isVerified = false 
		def user = params?.username
		def pass = params?.password

		if( user && pass ) {
			// insert code for verification of user and 
			// ... 
			isVerified = true
 		}
		return isVerified
	}



    /* this is just for testing! */
    def test = {
		def result = dbnp.rest.common.CommunicationManager.getQueryResult("Insulin")
		render result 
		render result["studies"]
		render result["studies"].get(0).class
    }
}
