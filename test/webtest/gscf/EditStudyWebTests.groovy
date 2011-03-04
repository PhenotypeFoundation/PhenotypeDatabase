package gscf

/**
 * Test editting the study that was created in CreateStudyWebTests.groovy
 * @Author Jeroen Wesbeek
 * @Since 20110113
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class EditStudyWebTests extends grails.util.WebTest {
	void testEditStudy001() {
		// make sure Canoo waits for AJAX calls
		config(easyajax: true)

		// login
		invoke "http://localhost:8080/gscf/?nostats=true"
		clickLink(description: "Click link: Log In | Register", htmlId: "open")
		setInputField(name: "j_username", value: "user")
		setInputField(description: "Set password field j_password: useR123!", name: "j_password", value: "useR123!")
		clickButton "Login"

		// navigate to the edit study wizard
		clickLink "Studies"
		clickLink "Edit a study"

		// select the study which was created in CreateStudyWebTests.groovy
		// and load it
		setSelectField(name: "study", text: "Test create study")
		clickButton "next »"

		// change some study fields
		setInputField(name: "title", value: "Test edit study")
		setInputField(name: "description", value: "Test study from EditStudyWebTests.testEditStudy001()")
		setInputField(name: "code", value: "TESTEDIT 001")

		// and now try to quicksave it
		clickButton "quick save"

		// and we should be done, without exceptions
		// verifyText(text:'Done')
		// does not seem to work, also see --> http://grails.1312388.n4.nabble.com/webtest-verifyText-doesn-t-see-AJAX-update-with-easyAjax-td2225674.html

		// and view the study
		clickLink "view the study"
	}
}
