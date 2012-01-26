/**
 * AjaxController Controller
 *
 * A collection of application wide Ajax resources
 *
 * @author  Jeroen Wesbeek work@osx.eu
 * @since	20120123
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package generic

import dbnp.studycapturing.*
import grails.converters.JSON

class AjaxController {
	def authenticationService

	/**
	 * Get all unique species (for accessible studies)
	 */
	def uniqueSpecies = {
		def result = Subject.executeQuery("SELECT DISTINCT a.species FROM Subject a")

		// set output header to json
		response.contentType = 'application/json'

		// render result
		if (params.callback) {
			render "${params.callback}(${result as JSON})"
		} else {
			render result as JSON
		}
	}

	def uniqueEventTemplateNames = {
		def user	= authenticationService.getLoggedInUser()
		def studies	= Study.giveReadableStudies(user)
		def uniqueEventTemplates = []

		// iterate through studies
		studies.each { study ->
			study.events.each { event ->
				if (!uniqueEventTemplates.contains(event.template)) {
					uniqueEventTemplates.add(event.template)
				}
			}
		}

		def result	= uniqueEventTemplates

		// set output header to json
		response.contentType = 'application/json'

		// render result
		if (params.callback) {
			render "${params.callback}(${result as JSON})"
		} else {
			render result as JSON
		}
	}

	def uniqueSamplingEventTemplateNames = {
		def user	= authenticationService.getLoggedInUser()
		def studies	= Study.giveReadableStudies(user)
		def uniqueSamplingEventTemplates = []

		// iterate through studies
		studies.each { study ->
			study.samplingEvents.each { event ->
				if (!uniqueSamplingEventTemplates.contains(event.template)) {
					uniqueSamplingEventTemplates.add(event.template)
				}
			}
		}

		def result	= uniqueSamplingEventTemplates

		// set output header to json
		response.contentType = 'application/json'

		// render result
		if (params.callback) {
			render "${params.callback}(${result as JSON})"
		} else {
			render result as JSON
		}
	}

	def studyCount = {
		def matched	= true
		def user	= authenticationService.getLoggedInUser()
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

			// if criteria are met, add this study to the matchedStudies array
			if (matched) matchedStudies.add(study)
		}

		// define json result
		def result = ['total':studies.size(),'matched':matchedStudies.size()];

		// set output header to json
		response.contentType = 'application/json'

		// render result
		if (params.callback) {
			render "${params.callback}(${result as JSON})"
		} else {
			render result as JSON
		}
	}
}