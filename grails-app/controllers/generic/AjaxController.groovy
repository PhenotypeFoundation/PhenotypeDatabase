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



}