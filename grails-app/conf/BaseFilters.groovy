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
import org.dbnp.gdt.AssayModule

class BaseFilters {
    def authenticationService

    // define filters
    def filters = {
        defineStyle(controller: '*', action: '*') {
            // before every execution
            before = {
                // set session lifetime to 1 week
                session.setMaxInactiveInterval(604800)
            }
        }

        // Save a reference to the logged in user in the session,
        // in order to use it later on. This is needed, because webflows are not capable of retrieving
        // the logged in user from the authenticationService, since that service (more specific: spring security)
        // is not serializable.
        saveUser(controller: '*', action: '*' ) {
            before = {
                // set the secUser in the session
                def secUser = authenticationService.getLoggedInUser()
                if (secUser) {
                    session.gscfUser = secUser
                } else {
                    // remove session variable
                    if( session?.gscfUser )
                        session.removeAttribute('gscfUser')
                }
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
        
        // Add assay modules to the Layout
        modulesInLayout(controller: '*', action: '*') {
            before = {
                // We need the assay modules in every request, so we store
                // a (simplified) copy of the object in the session
                if( session.assayModules == null ) {
                    AssayModule.withTransaction {
                        def assayModules = AssayModule.list()
                        session.assayModules = assayModules.collect { 
                            [ name: it.name, url: it.url ]
                        }
                    }
                }
                
                request.setAttribute('assayModules', session.assayModules)
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

