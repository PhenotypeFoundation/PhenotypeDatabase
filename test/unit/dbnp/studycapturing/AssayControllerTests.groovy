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


// Disabled testing of controller since I made a webflow out of the excel export
// testing might prove necessary later but tests need to be rewritten

//    void testWrongAssayID() {
//        mockFlash.assayId = 3
//
//        controller.compileExportData()
//
//        assertEquals 'Redirected action should match', [action: 'selectAssay'], redirectArgs
//        assertEquals 'Error message', 'No assay found with id: 3', mockFlash.errorMessage
//    }

//    void testExceptionHandling() {
//        mockFlash.assayId = 1
//
//        controller.metaClass.'grailsApplication' = [
//                config: [modules: [metabolomics: [url: 'www.ab.com']]]
//        ]
//
//        controller.assayService = [
//
//                collectAssayData:               {a, b, c -> throw new Exception('msg1') },
//                convertColumnToRowStructure:    {a -> throw new Exception('msg2')},
//                exportRowWiseDataToExcelFile:   {a, b -> throw new Exception('msg3') }
//
//        ]
//
//        controller.compileExportData()
//
//        assertEquals 'Redirected action should match', [action: 'selectAssay'], redirectArgs
//        assertEquals 'Error message', 'msg1', mockFlash.errorMessage
//
//        controller.assayService.collectAssayData = {a, b, c -> true}
//        controller.compileExportData()
//
//        assertEquals 'Redirected action should match', [action: 'selectAssay'], redirectArgs
//        assertEquals 'Error message', 'msg2', mockFlash.errorMessage
//
//        controller.doExport()
//
//        assertEquals 'Redirected action should match', [action: 'selectAssay'], redirectArgs
//        assertEquals 'Error message', 'msg3', mockFlash.errorMessage
//
//    }

}
