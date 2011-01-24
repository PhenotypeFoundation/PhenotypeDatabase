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
class BaseFilters {
	// define filters
	def filters = {
		defineStyle(controller: '*', action: '*') {
			// before every execution
			before = {
				// set the default style in the session
				if (!session.style) {
					session.style = 'default_style'
				}

				// set session lifetime to 1 week
				session.setMaxInactiveInterval(604800)
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
				def viewDuration = System.currentTimeMillis() - request._timeAfterRequest
				log.info("Timer: ${controllerName}(${actionDuration}ms)::${actionName}(${viewDuration}ms)")
			}
		}

	}
}

