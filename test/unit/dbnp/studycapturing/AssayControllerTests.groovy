package dbnp.studycapturing

import grails.test.*

/**
 * AssayControllerTests Test
 *
 * @author  your email (+name?)
 * @since	20101208
 * @package	dbnp.studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class AssayControllerTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
        mockDomain(Assay, [new Assay(id:1)])
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testWrongAssayID() {
        mockParams.assayId = 3

        controller.exportAssayAsExcel()

        assertEquals 'Redirected action should match', [action: 'selectAssay'], redirectArgs
        assertEquals 'Error message', 'No assay found with id: 3.', mockFlash.errorMessage
    }

    void testExceptionHandling() {
        mockParams.assayId = 1

        controller.metaClass.'grailsApplication' = [
                config: [modules: [metabolomics: [url: 'www.ab.com']]]
        ]

        controller.assayService = [

                collectAssayData:                   {a, b -> def e = new Exception('msga'); e.metaClass.cause = new Exception('msg1'); throw e },
                exportColumnWiseDataToExcelFile:    {a, b -> def e = new Exception('msgb'); e.metaClass.cause = new Exception('msg2'); throw e }

        ]

        controller.exportAssayAsExcel()

        assertEquals 'Redirected action should match', [action: 'selectAssay'], redirectArgs
        assertEquals 'Error message', 'msg1', mockFlash.errorMessage

        controller.assayService.collectAssayData = {a, b -> true}
        controller.exportAssayAsExcel()

        assertEquals 'Redirected action should match', [action: 'selectAssay'], redirectArgs
        assertEquals 'Error message', 'msg2', mockFlash.errorMessage

    }

}
