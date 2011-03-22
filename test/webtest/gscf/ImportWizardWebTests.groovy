package gscf

import org.dbnp.gdt.*

class ImportWizardWebTests extends grails.util.WebTest {

	// Unlike unit tests, functional tests are sometimes sequence dependent.
	// Methods starting with 'test' will be run automatically in alphabetical order.
	// If you require a specific sequence, prefix the method name (following 'test') with a sequence
	// e.g. test001XclassNameXListNewDelete

	void testImportWizard() {
		// reset Identity to be able to predict identifiers
		Identity.resetIdentifier()

		// make sure Canoo waits for AJAX calls
		config(easyajax: true)

		invoke "http://localhost:8080/gscf/?nostats=true"
		clickLink(description: "Click link: Log In | Register", htmlId: "open")
		setInputField(name: "j_username", value: "user")
		setInputField(description: "Set password field j_password: useR123!", name: "j_password", value: "useR123!")
		clickButton "Login"
        
        clickLink "Studies"
        clickLink "Import study data"
	
        setSelectField(name: "entity", text: "Study")        
        clickButton "next »"
        
        // Set column properties (TemplateFields)
        setSelectField(name: "columnproperty.index.0", text: "startDate")
        setSelectField(name: "columnproperty.index.1", text: "code (IDENTIFIER)")
        setSelectField(name: "columnproperty.index.15", text: "title")
        setSelectField(name: "columnproperty.index.16", text: "description")
        clickButton "next »"
        
        // Validate and store
        clickButton "next »"
        
	
	}

}