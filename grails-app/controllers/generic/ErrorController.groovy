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
		def studyId
		def shortCode = request.forwardURI.replace("${request.contextPath}/", "")

		if ( shortCode ) {

			def study = Study.findByCode(shortCode)

			if ( study ) {

				if ( study.publicstudy ) {
					studyId = study.id
				}
				else {
					SecUser user = authenticationService.getLoggedInUser()

					if ( user ) {
						if (study.canRead(user) ) {
							studyId = study.id
						}
					}
				}
			}

			if (studyId) {
				redirect(controller: "study", action: "show", id: studyId)
				return
			}
		}


		render(template: "404")
	}
}
