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
 * @author	Jahn 
 * @since	20100601
 *
 */

import data.*
import dbnp.studycapturing.Study
import dbnp.studycapturing.Assay
import grails.converters.*
import org.codehaus.groovy.grails.web.json.*



class RestController {


        /* REST resources for Simple Assay Module (SAM) */ 


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
 			Assay.find( "from Assay as a where externalAssayID=?",[id]).getSamples().each { sample ->
				def item = [ 
					'name'		: sample.name,
					'material'	: sample.material.name,
					'subject'	: sample.parentSubject.name,
					'event'		: sample.parentEvent.template.name,
					'startTime'	: sample.parentEvent.getDurationString()
				] 
				items.push item 
			}
 		}
		render items as JSON
	}

}
