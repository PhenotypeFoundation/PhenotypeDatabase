package gscf

import grails.test.*
import org.springframework.core.io.*
import grails.converters.*
import dbnp.authentication.*
import org.dbnp.gdt.*

/**
 * ImporterTests Test
 *
 * Description of my test
 *
 * @author  Tjeerd
 * @since	20110314
 * @package	gscf
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class ImporterTests extends GroovyTestCase {
    def ImporterService

    static final String testStudyExcelFile = "testfiles/NTC_Experiment_test4.xls"
    static final String testStudyXMLTemplateFile = "testfiles/ntc_tudy_template.xml"
    static final String testSheetIndex = "0"
    static final String testHeaderRow = "0"
    static final String testDatamatrixStart = "0"
    
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    /**
    * Try to import an example Excel study and an XML study template
    */
    void testImportExcelAndXMLTemplateStudy() {
        def xmltemplate
        def user = SecUser.findByUsername( "user" );
        
        // Load the files from a subfolder in the integration test folder and setup input streams
        Resource resourceExcel = new ClassPathResource(testStudyExcelFile, getClass().classLoader)
        Resource resourceXMLTemplate = new ClassPathResource(testStudyXMLTemplateFile, getClass().classLoader)

        // Create input streams from the resources
        def fisExcel = new FileInputStream(resourceExcel.getFile())
        assert fisExcel

        def fisXML = new FileInputStream(resourceXMLTemplate.getFile())
        assert fisXML

        // Retrieve the input stream as an Excel workbook
        def wb = importerService.getWorkbook(fisExcel)
        assert wb

        // Parse XML
		try {
			xmltemplate = XML.parse(fisXML, "UTF-8")
		} catch( Exception e ) {
			println "Parsing failed" + e
		}

        assert xmltemplate

        xmltemplate.template.each {
            println it
        }

        // Parse the XML template
        def t = Template.parse(xmltemplate.template, user);

        assert t
    }
}
