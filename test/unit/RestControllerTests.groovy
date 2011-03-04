import grails.test.*
import dbnp.authentication.*

class RestControllerTests extends ControllerUnitTestCase {
	def authenticationService
	
    protected void setUp() {
        super.setUp()

		controller.authenticationService = authenticationService
    }

    protected void tearDown() {
        super.tearDown()
    }

	void testResponse() {
	}
}
