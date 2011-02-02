package dbnp.studycapturing

import grails.test.*

class StudyWizardControllerTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testAjaxParseRelTimeNonExistent() {
        // Without parameters, the method should give an error
        controller.ajaxParseRelTime();
	    assert controller.response.status == 400;
    }

    void testAjaxParseRelTimeEmpty() {
        // With empty parameter, the method should work
        controller.params.reltime = '';
        controller.ajaxParseRelTime();
	    assert controller.response.status == 200
        assert controller.response.contentAsString == "0 seconds"
    }

    void testAjaxParseRelTimeCorrect() {
        // With simple parameter, the method should work
        controller.params.reltime = '3d';
        controller.ajaxParseRelTime();
	    assert controller.response.status == 200
        assert controller.response.contentAsString == "3 days"
    }

    void testAjaxParseRelTimeIllegal() {
        // With illegal parameter, the method should give status code 400
        controller.params.reltime = 'no valid reltime';
        controller.ajaxParseRelTime();
    	assert controller.response.status == 400;
    }

}
