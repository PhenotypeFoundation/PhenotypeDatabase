/**
 * NotFoundController Controler
 *
 * Description of my controller
 *
 * @author  Jeroen Wesbeek <work@osx.eu>
 * @since	20120816
 */
package generic

import dbnp.studycapturing.Study
import dbnp.authentication.SecUser

class NotFoundController {
	def authenticationService

	/**
	 * index closure
	 */
    def index = {
	    render(template: "404")
    }

	/**
	 * we probably got redirected here from the UrlMappings with
	 * a shortcode (e.g. studies.dbnp.org/study_code)
	 * redirect to a study, or to the index page
	 */
	def find = {
		// got a shortcode?
		if (params.containsKey('shortCode')) {
			// yeah, see if we've got a study with this
			// shortcode
			def study       = Study.findByCode(params.get('shortCode'))
			SecUser user    = authenticationService.getLoggedInUser()

			// got a study and is it readable?
			if (study && study.canRead(user)) {
				redirect(controller: "study", action: "show", id: study.id)
			} else {
				render(template: "404")
			}
		} else {
			render(template: "404")
		}
	}
}
