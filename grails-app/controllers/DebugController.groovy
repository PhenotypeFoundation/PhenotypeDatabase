import grails.util.GrailsUtil

/**
 * Debug Controller
 * @Author  Jeroen Wesbeek
 * @Since   20091014
 * @Description
 *
 * If all controllers extend this debug controller in one piece of code the
 * behaviour of the other controllers can be -to some extent- manipulated.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class DebugController {
    /**
     * Turn scaffolding on or off
     */
    def scaffold = (GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT && this.class.name != 'DebugController');

    /**
     * Render output to the browser, overload this method to suit your needs
     */
    def index = { 
	render(sprintf("%s @ %s environment :: nothing to see here! :)",this.class.name,GrailsUtil.environment));
    }
}
