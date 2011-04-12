/**
 * Base Filters
 * @Author Jeroen Wesbeek
 * @Since 20091026
 * @see main.gsp
 * @see http://grails.org/Filters
 * @Description
 *
 * These filters contain generic logic for -every- page request.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
import org.codehaus.groovy.grails.commons.GrailsApplication

class BaseFilters {
	def authenticationService

	// define filters
	def filters = {
		defineStyle(controller: '*', action: '*') {
			// before every execution
			before = {
				// set the default style in the session
				if (!session.style) {
					def hostname = InetAddress.getLocalHost().getHostName()
					if (hostname =~ 'nmcdsp.org') {
						session.style = 'nmcdsp_style'
					} else if (hostname =~ 'nbx14') {
						session.style = 'dbnp_style'
					} else {
						session.style = 'default_style'
					}
				}

				// set session lifetime to 1 week
				session.setMaxInactiveInterval(604800)
			}
		}

		// we need secUser in GDT::Template*, but we do not want GDT
		// to rely on authentication. Therefore we handle it through
		// a filter and store the loggedInUser in the session instead
		templateEditor(controller: 'templateEditor', action: '*') {
			// before every execution
			before = {
				// set the secUser in the session
				def secUser = authenticationService.getLoggedInUser()
				if (secUser) {
					session.loggedInUser = secUser
				} else {
					// remove session variable
					session.removeAttribute('loggedInUser')

					def returnURI = request.requestURL.toString().replace(".dispatch","").replace("/grails/","/") + '?' + request.queryString

					// and redirect to login page
					redirect(controller: 'login', action: 'auth', params: [returnURI: returnURI, referer: request.getHeader('referer')] )
				}
			}
		}

		// disable all access to the query controller as this allows
		// full access to the database
		query(controller: 'query', action: '*') {
			// before every execution
			before = {
				// only allow development
				if (grails.util.GrailsUtil.environment != GrailsApplication.ENV_DEVELOPMENT) {
					redirect(controller: 'home')
				}
			}
		}
		
		profiler(controller: '*', action: '*') {
			before = {
				request._timeBeforeRequest = System.currentTimeMillis()
			}

			after = {
				request._timeAfterRequest = System.currentTimeMillis()
			}

			afterView = {
				def actionDuration = request._timeAfterRequest ? request._timeAfterRequest - request._timeBeforeRequest : 0
				def viewDuration = request._timeAfterRequest ? System.currentTimeMillis() - request._timeAfterRequest : 0
				log.info("Timer: ${controllerName}(${actionDuration}ms)::${actionName}(${viewDuration}ms)")
			}
		}

	}
}

