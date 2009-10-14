import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.GrailsUtil

/**
 * Base Controller
 * @Author  Jeroen Wesbeek
 * @Since   20091014
 * @Description
 *
 * Base Controller which provides general functionality
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class BaseController {
    /**
     * Turn scaffolding on or off
     */
    def scaffold = (GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT && this.class.name != 'DebugController');

    /**
     * Render default output to the browser, overload this method to suit your needs
     */
    def index = { 
	render(sprintf("%s @ %s environment :: nothing to see here! :)",this.class.name,GrailsUtil.environment));
    }

    /**
     * intercept any method calls in extended classes
     * @see http://www.grails.org/Controllers+-+Interceptors
     */
    def beforeInterceptor = [action:this.&auth,except:'login']

    /**
     * after interception
     * @see http://www.grails.org/Controllers+-+Interceptors
     */
    def afterInterceptor = { model, modelAndView ->
	println "Current view is ${modelAndView.viewName}"
	if(model.someVar) modelAndView.viewName = "/mycontroller/someotherview"
	println "View is now ${modelAndView.viewName}"
    }

    /**
     * authentication method
     */
    def auth() {
	if(!session.user) {
	    redirect(action:'login')
	    return false
	}
    }

    /**
     * login method
     */
    def login = {
	// display login page
	println "render login...";
    }
}
