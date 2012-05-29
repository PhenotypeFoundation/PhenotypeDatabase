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
import org.dbnp.gdt.AssayModule

class AjaxController {
	def authenticationService
	def ajaxService

	/**
	 * Get all unique species in the system
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

	/**
	 * Get all unique event template names for the studies the user can read
	 */
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

	/**
	 * Get all unique sampling event template names for the studies the user can read
	 */
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

    /**
     * Get all modules
     */
    def modules = {
        def result = AssayModule.list()

        // set output header to json
        response.contentType = 'application/json'

        // render result
        if (params.callback) {
            render "${params.callback}(${result as JSON})"
        } else {
            render result as JSON
        }
    }

	/**
	 * return the number of studies the user can read based on criteria
	 */
	def studyCount = {
		def user	= authenticationService.getLoggedInUser()
		def total	= Study.giveReadableStudies(user).size()
		def studies = ajaxService.getStudiesByCriteriaForCurrentUser(params)

		// define json result
		def result = ['total':total,'matched':studies.size()]

		// set output header to json
		response.contentType = 'application/json'

		// render result
		if (params.callback) {
			render "${params.callback}(${result as JSON})"
		} else {
			render result as JSON
		}
	}

	/**
	 * return the studies the user can read based on criteria
	 */
	def studies = {
        def user	= authenticationService.getLoggedInUser()
        def total	= Study.giveReadableStudies(user).size()
        def studies = ajaxService.getStudiesByCriteriaForCurrentUser(params)

        // find unique properties for these studies
        def species = []
        def uniqueSpecies = [:]
        def eventTemplates = []
        def uniqueEventTemplateNames = [:]
        def samplingEventTemplates = []
        def uniqueSamplingEventTemplateNames = [:]
        def modules = []
        def uniqueModules = [:]

        // iterate through studies to temporarily remember properties
        studies.each {
            species                 = species + it.uniqueSpecies()
            eventTemplates          = eventTemplates + it.uniqueEventTemplates()
            samplingEventTemplates  = samplingEventTemplates + it.uniqueSamplingEventTemplates()
            modules                 = modules + it.uniqueAssayModules()
        }

        // fetch unique properties
        species.unique().each { uniqueSpecies[ it.id ] = it.name }
        eventTemplates.unique().each { uniqueEventTemplateNames[ it.id ] = it.name }
        samplingEventTemplates.unique().each { uniqueSamplingEventTemplateNames[ it.id ] = it.name }
        modules.unique().each { uniqueModules[ it.id ] = it.name }

        // define json result
		def result = [
                'studies':studies.collect{ it.title },
                'total':total,
                'matched':studies.size(),
                'uniqueSpecies':uniqueSpecies,
                'uniqueEventTemplateNames':uniqueEventTemplateNames,
                'uniqueSamplingEventTemplateNames':uniqueSamplingEventTemplateNames,
                'modules':uniqueModules
        ]

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