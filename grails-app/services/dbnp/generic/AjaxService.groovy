/**
 * AjaxService Service
 * 
 * Description of my service
 *
 * @author  your email (+name?)
 * @since	2010mmdd
 * @package	???
 *
 * Revision information:
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
 */
package dbnp.generic

import dbnp.studycapturing.Study

class AjaxService {
    static transactional = true
	def authenticationService

	def getStudiesByCriteriaForCurrentUser(params) {
		def user	= authenticationService.getLoggedInUser()

		return getStudiesByCriteriaForUser(params, user);
	}

	def getStudiesByCriteriaForUser(params, user) {
		def matched	= true
		def studies	= Study.giveReadableStudies(user)
		def matchedStudies = []

		// closure to transform criteria arguments to an array of Longs
		def getAsArray = { paramText ->
			def result = []

			if (paramText == null) {
				result = []
			} else if (paramText.toString().contains(",")) {
				result = paramText.collect { it as Long }
			} else {
				result = [paramText as Long]
			}

			return result
		}

		// define criteria arrays based on JSON arguments
		def uniqueSpecies = (params.containsKey('uniqueSpecies[]')) ? getAsArray(params['uniqueSpecies[]']) : []
		def uniqueEventTemplateNames = (params.containsKey('uniqueEventTemplateNames[]')) ? getAsArray(params['uniqueEventTemplateNames[]']) : []
		def uniqueSamplingEventTemplateNames = (params.containsKey('uniqueSamplingEventTemplateNames[]')) ? getAsArray(params['uniqueSamplingEventTemplateNames[]']) : []
        def modules = (params.containsKey('modules[]')) ? getAsArray(params['modules[]']) : []

		// iterate through readable studies for this user
		studies.each { study ->
			matched = true

			// match to selection criteria
			// 1. if any species were selected, see if this study contains any subject of this species
			matched = (matched && (!uniqueSpecies.size() || study.subjects.find{uniqueSpecies.contains(it.species.id)})) ? true : false;
			// 2. if any eventTemplateNames were selected, see if this study contains any of these
			matched = (matched && (!uniqueEventTemplateNames.size() || study.events.find{uniqueEventTemplateNames.contains(it.template.id)})) ? true : false;
			// 3. if any samplingEventTemplateNames were selected, see if this study contains any of these
			matched = (matched && (!uniqueSamplingEventTemplateNames.size() || study.samplingEvents.find{uniqueSamplingEventTemplateNames.contains(it.template.id)})) ? true : false;
            // 4. if any modules were selected, see if this study contains any of these
            matched = (matched && (!modules.size() || study.assays.find{modules.contains(it.module.id)})) ? true : false;

			// if criteria are met, add this study to the matchedStudies array
			if (matched) matchedStudies.add(study)
		}

		return matchedStudies
	}
}
