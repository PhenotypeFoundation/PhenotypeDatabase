package dbnp.studycapturing
import grails.test.*

class StudyControllerTests extends ControllerUnitTestCase {
    protected void setUp() {

        super.setUp()
        mockDomain(Study, [ new Study(id: 1, assays: [[id:1, name:'assay1'], [id:2, name:'assay2']]),
                            new Study(id: 2, assays: [])])

    }

    protected void tearDown() {
        super.tearDown()
    }

    void testAjaxGetAssays() {

        mockParams.id = 1
        controller.ajaxGetAssays()
        assertEquals '[{"name":"assay1","id":1},{"name":"assay2","id":2}]', mockResponse.contentAsString
    }


    void testAjaxGetAssaysEmptyList() {

        mockParams.id = 2
        controller.ajaxGetAssays()
        assertEquals '[]', mockResponse.contentAsString
    }
}
