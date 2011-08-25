package dbnp.visualization

import grails.test.*

/**
 * VisualizeControllerTests Test
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
class VisualizeControllerTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testGetStudies() {
		assert 1 == 1
    }

    void testGetFields() {
		assert 1 == 1
    }

	void testGetData() {
		// Create a fake study
		
		// Set controller parameters
		controller.params.study = 1;
		controller.params.fields = [ "row": 4, "columns": 6 ];
		
		// Run controller method
		//controller.getData()
		//assertEquals "...", controller.response.contentAsString
		assert 1 == 1;
    }
}
