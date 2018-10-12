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

		if ( shortCode ) {

			def study = Study.findByCode(shortCode)

			if ( study ) {

				def studyId

				if ( study.publicstudy ) {
					studyId = study.id
				}
				else {
					SecUser user = authenticationService.getLoggedInUser()

					if ( user ) {
						// User will see study or get 'unauthorized' message
						studyId = study.id
					}
					else {
						// User will be asked to sign in
						redirect(controller: "home", action: "gotoStudy", id: study.id )
						return
					}
				}

				if ( studyId ) {
					redirect(controller: "study", action: "show", id: studyId)
					return
				}
			}
		}


		render(template: "404")
	}
}
