import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.GrailsUtil
//import org.apache.log4j.*

/**
 * Base Controller
 * @Author  Jeroen Wesbeek
 * @Since   20091014
 * @Description
 *
 * Base Controller which provides general functionality. Should always be
 * extended in all controllers
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class BaseController {
    /**
     * @var object authorization object
     * @visibility public
     */
    public def Authorization;
    public def scaffold = false;

    /**
     * class constructor
     * @void
     */
    protected BaseController() {
	// instantiate Authorization class
	this.Authorization = new Authorization();

	// dynamically set scaffolding
	this.scaffold = (GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT && this.class.name != 'BaseController');
    }

    /**
     * Render default output to the browser, overload this in extended classes
     * @void
     */
    def index = {
	render(sprintf("default index for %s @ %s environment :: nothing to see here! :)",this.class.name,GrailsUtil.environment));
    }

    /**
     * intercept any method calls in extended classes
     * @see http://www.grails.org/Controllers+-+Interceptors
     */
    def beforeInterceptor = {
	def controller = params.controller;
	def action = params.action;
	
	// check if the user is Authorized to call this method
	if (Authorization.isAuthorized(controller,action)) {
	    // user is not authorized to use this functionality
	    printf("authorized call to action: %s->%s(...)\n",controller,action);
	} else {
	    // user is not authorized to use this functionality
	    printf("!! unauthorized call to action: %s-->%s(...)\n",controller,action);

	    // redirect to error page
	    flash['error'] = sprintf("unauthorized call to action: %s::%s\n",controller,action);
	    redirect(controller:'error',action:'index');
	}
    }

    /**
     * after interception
     * @param object model
     * @param object modelAndView
     * @see http://www.grails.org/Controllers+-+Interceptors
     */
    def afterInterceptor = {
	// nothing here yet
    }
}