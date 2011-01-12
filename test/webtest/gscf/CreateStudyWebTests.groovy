package gscf



class CreateStudyWebTests extends grails.util.WebTest {

    // Unlike unit tests, functional tests are sometimes sequence dependent.
    // Methods starting with 'test' will be run automatically in alphabetical order.
    // If you require a specific sequence, prefix the method name (following 'test') with a sequence
    // e.g. test001XclassNameXListNewDelete

   void testCreateStudy() {

	   // make sure Canoo waits for AJAX calls
	   config(easyajax:true)

	   invoke "http://localhost:8080/gscf/"
	   clickLink(description: "Click link: Log In | Register", htmlId: "open")
	   setInputField(name: "j_username", value: "user")
	   setInputField(description: "Set password field j_password: useR123!", name: "j_password", value: "useR123!")
	   clickButton "Login"
	   clickLink "Studies"

	   clickLink "Create a new study"
	   setSelectField(name: "template", text: "Academic study")
	   setInputField(name: "title", value: "Test create study")
	   setInputField(name: "description", value: "Test study from CreateStudyWebTests.testCreateStudy()")
	   setInputField(name: "code", value: "TESTCREATE")
	   setInputField(name: "startdate", value: "01/01/2011")
	   clickButton "next »"

	   // Add 10 human subjects
	   setSelectField(name: "species", text: "Homo sapiens")
	   setSelectField(name: "template", text: "Human")
	   setInputField(name: "addNumber", value: "10")
	   clickButton "Add"

	   /* Stub code for testing of the template editor
	   setSelectField(name: "template", text: "add / modify..")
	   clickLink "Create new field"
	   setInputField(name: "name", value: "Second Species")
	   setSelectField(name: "type", text: "ONTOLOGYTERM")
	   setSelectField(name: "ontologies", text: "NCBI organismal classification")
	   clickButton "Save"
	   clickButton "Close"
	   setSelectField(name: "subject_793_second_species", text: "add more...")
	   setInputField(name: "term", value: "rat")
	   clickLink "Rattus norvegicus (Synonym) from: Rattus norvegicus"
	   clickButton "Add term"
	   clickButton "Close"   */
	   clickButton "next »"

	   // Add some events, sampling events, and groups
	   setSelectField(name: "eventTemplate", text: "Compound challenge")
	   setSelectField(name: "compound", text: "glucose")
	   clickButton "Add"
	   clickButton "add a new eventgroup"
	   setCheckbox(name: "event_15_group_13")
	   setCheckbox(name: "event_15_group_16")
	   setRadioButton(description: "Check radio button eventType: sample", name: "eventType", value: "sample")
	   setSelectField(name: "sampleTemplate", text: "Blood extraction")
	   setSelectField(name: "sampletemplate", text: "Human blood sample")
	   clickButton "Add"
	   setSelectField(name: "sampleTemplate", text: "Blood extraction")
	   setSelectField(name: "sampletemplate", text: "Human blood sample")
	   setInputField(name: "starttime", value: "1w")
	   clickButton "Add"
	   setCheckbox(name: "event_18_group_13")
	   setCheckbox(name: "event_19_group_16")
	   setRadioButton(description: "Check radio button eventType: event", name: "eventType", value: "event")
	   setSelectField(name: "eventTemplate", text: "Compound challenge")
	   setSelectField(name: "compound", text: "glucose")
	   clickButton "Add"
	   setCheckbox(name: "event_20_group_13")
	   setCheckbox(name: "event_20_group_16")

	   /* stub to test ontology term widget
	   setSelectField(name: "event_20_compound", text: "add more...")
	   setInputField(name: "term", value: "glu")
	   clickLink "L-glutamic acid (Synonym) from: L-glutamic acid"
	   clickButton "Add term"
	   clickButton "Close"
	   setSelectField(name: "event_1648_compound", text: "L-glutamic acid")
	   */
	   clickButton "next »"


	   // assign subjects to event groups
	   setCheckbox(name: "subject_3_group_13")
	   setCheckbox(name: "subject_4_group_13")
	   setCheckbox(name: "subject_5_group_13")
	   setCheckbox(name: "subject_6_group_16")
	   setCheckbox(name: "subject_7_group_16")
	   setCheckbox(name: "subject_8_group_16")
	   clickButton "next »"


	   // accept the generated samples, check if the right names are in place
	   /* For some reason enabling this causes the whole webtest to blow. TODO: find out why...
	   verifyInputField(name: "sample_21_name", value="Subject1_BloodExtraction_Group1_0s")
	   verifyInputField(name: "sample_22_name", value="Subject2_BloodExtraction_Group1_0s")
	   verifyInputField(name: "sample_23_name", value="Subject3_BloodExtraction_Group1_0s")
	   verifyInputField(name: "sample_24_name", value="Subject4_BloodExtraction_Group2_1w")
	   verifyInputField(name: "sample_25_name", value="Subject5_BloodExtraction_Group2_1w")
	   verifyInputField(name: "sample_26_name", value="Subject6_BloodExtraction_Group2_1w")

	       and this doesnt work apparently inside textfields: verifyText(text: "Subject1_BloodExtraction_Group1_0s")

		*/
	   clickButton "next »"


	   setSelectField(name: "template", text: "Clinical chemistry assay")
	   setInputField(name: "name", value: "test assay")
	   clickButton "Add"
	   clickButton "next »"

	   setCheckbox(name: "sample_21_assay_27")
	   clickButton "next »"
	   clickButton "next »"
	   clickLink "view the study"
    }

}