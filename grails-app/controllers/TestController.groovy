/**
 * Test Controller
 * @Author  Jeroen Wesbeek
 * @Since   20091019
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
public class TestController extends BaseController {
    /**
     * class constructor
     * @void
     */
    public def TestController() {
	// nothing yet
    }

    /**
     * render dummy text when executed
     * @void
     */
    def index = {
	render(sprintf("this is %s",this.class.name));
    }

    /**
     * dummy method
     */
    def sayHello = {
	render("Hello World!");
    }

    /**
     * another dummy method
     */
    def sayWeather = {
	render("The weather is pretty good!");
    }
}