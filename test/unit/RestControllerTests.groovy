import grails.test.*
import dbnp.authentication.*

class RestControllerTests extends ControllerUnitTestCase {
	def AuthenticationService
	
    protected void setUp() {
        super.setUp()

		controller.AuthenticationService = AuthenticationService
    }

    protected void tearDown() {
        super.tearDown()
    }

	void testResponse() {
	}
}
