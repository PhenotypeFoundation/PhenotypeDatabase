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
    //def scaffold = false;

    /**
     * class constructor
     * @visibility protected
     * @void
     */
     BaseController() {
	// debug line for now
	printf("instantiated %s\n",this.class.name);

	// instantiate Authorization service
	//this.authorizationService = new AuthorizationService();

	// dynamically set scaffolding
	//this.scaffold = (GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT && this.class.name != 'BaseController');
    }


    /**
     * intercept any method calls in extended classes
     * @visibility public
     * @see http://www.grails.org/Controllers+-+Interceptors
     */
    def beforeInterceptor = {
	def controller = params.controller;
	def action = params.action;

	printf("calling %s->%s(...)\n",this.class.name,action);
    }
}