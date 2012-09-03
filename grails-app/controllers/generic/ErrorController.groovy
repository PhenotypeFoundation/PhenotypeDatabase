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

class ErrorController {
	def authenticationService

	/**
	 * 404 closure
	 */
	def notFound = {
		// substract shortCode from original request uri
		def shortCode = request.forwardURI.replace("${request.contextPath}/", "")

		// got a shortcode?
		if (shortCode) {
			// yeah, see if we've got a study with this
			// shortcode
			def study       = Study.findByCode(shortCode)
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
