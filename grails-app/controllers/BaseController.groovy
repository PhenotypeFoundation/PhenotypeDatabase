import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.GrailsUtil

/**
 * Base Controller
 * @Author  Jeroen Wesbeek
 * @Since   20091014
 * @see	    Authorization.groovy
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
    public def authorizationService;

    /**
     * @var boolean scaffolding default
     * @visibility public
     */
    def scaffold = false;

    /**
     * class constructor
     * @visibility protected
     * @void
     */
     BaseController() {
	// debug line for now
	printf("instantiated %s\n",this.class.name);

	// instantiate Authorization service
	this.authorizationService = new AuthorizationService();

	// dynamically set scaffolding
	this.scaffold = (GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT && this.class.name != 'BaseController');
    }


    /**
     * intercept any method calls in extended classes
     * @visibility public
     * @see http://www.grails.org/Controllers+-+Interceptors
     */
    def beforeInterceptor = {
	def controller = params.controller;
	def action = params.action;

	// check if the user is Authorized to call this method
	if (this.authorizationService.isAuthorized(controller,action)) {
	    // user is not authorized to use this functionality
	    printf("authorized call to action: %s->%s(...)\n",this.class.name,action);
	} else {
	    // user is not authorized to use this controller + method
	    printf("!! unauthorized call to action: %s-->%s(...)\n",this.class.name,action);

	    // redirect to error page
	    flash['error'] = sprintf("unauthorized call to action: %s::%s\n",controller,action);
	    redirect(controller:'error',action:'index');
	}
    }
}