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
		println params
		println params.containsKey('data')
//		println params['data']['uniqueSpecies']

		def matched	= false
		def user	= authenticationService.getLoggedInUser()
		def studies	= Study.giveReadableStudies(user)
		def matchedStudies = []

		// parameters
/*
		def uniqueSpecies =	(params['data']['uniqueSpecies']) ? params['data']['uniqueSpecies'] : []
		println uniqueSpecies.class
		println uniqueSpecies.size()

		// iterate through studies
		studies.each { study ->
matched = false
//			matched = (study.subjects.find{uniqueSpecies.contains(it.species.id)}) ? true : false;
println "study: ${study} -> matched: ${matched}"

			if (matched) matchedStudies.add(study)
		}
*/
println "------"

		def result = ['count':matchedStudies.size()];

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