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
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testWrongAssayID() {
        mockParams.assayId = 1

        controller.exportAssayAsExcel()

        assertEquals 'Redirected action should match', [action: 'selectAssay'], redirectArgs
        assertEquals 'Error message', 'No assay found with id: 3.', mockFlash.errorMessage
    }

    void testExceptionHandling() {
        controller.assayService = [
                collectAssayData:{a, b -> throw new Exception('msg1')},
                exportColumnWiseDataToExcelFile:{a, b -> throw new Exception('msg2')}
        ]
        controller.exportAssayAsExcel()

        assertEquals 'Redirected action should match', [action: 'selectAssay'], redirectArgs
        assertEquals 'Error message', 'java.lang.Exception: msg1', mockFlash.errorMessage

        controller.assayService.collectAssayData = {a, b -> true}
        controller.exportAssayAsExcel()

        assertEquals 'Redirected action should match', [action: 'selectAssay'], redirectArgs
        assertEquals 'Error message', 'java.lang.Exception: msg2', mockFlash.errorMessage

    }

}
