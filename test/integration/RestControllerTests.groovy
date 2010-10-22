import grails.test.*
import dbnp.authentication.*

/**
 * RestControllerTests Test
 *
 * Description of my test
 *
 * @author  your email (+name?)
 * @since	2010mmdd
 * @package	???
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class RestControllerTests extends ControllerUnitTestCase {
	def AuthenticationService

	String consumer = "TEST"
	String token = "abcdef"

	String studyToken = "PPS3_leptin_module"
	String assayToken = "PPS3_Lipidomics"
	String sampleToken = "A51_B"

    protected void setUp() {
        super.setUp()

		controller.AuthenticationService = AuthenticationService
    }

    protected void tearDown() {
        super.tearDown()
    }

	protected void login() {
		println "Logging in to GSCF"

		// Enable remote login
		long now = new Date().getTime()
		Date expiryDate = new Date( now + 24 * 60 * 60 * 1000 )
		def user = new SessionAuthenticatedUser( consumer: consumer, token: token, secUser: SecUser.findByUsername( 'user' ), expiryDate: expiryDate )
		user.save(flush: true)

		// Check whether the user is really logged in
		assert AuthenticationService.isRemotelyLoggedIn( consumer, token )

		// Set authentication parameters
		controller.params.consumer = consumer
		controller.params.token = token
	}

    void testIsUser() {
		login();
		controller.isUser()

		println controller.response.contentAsString
		assert controller.response.contentAsString.equals( '{"authenticated":true}' )
    }

    void testGetStudies() {
		login();

		controller.getStudies()
		println controller.response.contentAsString
    }

    void testGetStudy() {
		login();

		controller.params.studyToken = studyToken
		controller.getStudy()
		println controller.response.contentAsString
    }

	void testGetAuthorizationLevel() {
		login();

		controller.params.studyToken = studyToken
		controller.getAuthorizationLevel()
		println controller.response.contentAsString
    }

	void testGetAssays() {
		login();

		controller.params.studyToken = studyToken
		controller.getAssays()
		println controller.response.contentAsString
    }

	void testGetAssay() {
		login();

		controller.params.assayToken = assayToken
		controller.getAssay()
		println controller.response.contentAsString
    }

	void testGetSamples() {
		login();

		controller.params.assayToken = assayToken
		controller.getSamples()
		println controller.response.contentAsString
    }

	void testGetSample() {
		login();

		controller.params.assayToken = assayToken
		controller.params.sampleToken = sampleToken
		controller.getSample()
		println controller.response.contentAsString
    }

}
