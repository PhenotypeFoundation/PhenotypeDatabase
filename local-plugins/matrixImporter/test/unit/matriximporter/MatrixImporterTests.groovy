package matriximporter

import grails.test.GrailsUnitTestCase
import org.dbxp.matriximporter.CsvParser
import org.dbxp.matriximporter.ExcelParser
import org.dbxp.matriximporter.MatrixImporter

class MatrixImporterTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testMockPPSHXLSX() {
        def excelReader = new ExcelParser()

        def (matrix, parseInfo) = excelReader.parse(new File('test_data/mock_PPSH.xlsx'))

        assert matrix.size() == 14
        assert matrix[0].size == 154
    }

    void testMockDiogenesXLSX() {
        def excelReader = new ExcelParser()

        def (matrix, parseInfo) = excelReader.parse(new File('test_data/DiogenesMockData.xlsx'))

        assert matrix.size() == 1986
        assert matrix[0].size == 163
    }

    void testMockDiogenesXLS() {
        def excelReader = new ExcelParser()

        def (matrix, parseInfo) = excelReader.parse(new File('test_data/DiogenesMockData.xls'))

        assert matrix.size() == 1986
        assert matrix[0].size == 163
    }

    void testMockDiogenesTABDELIMITED() {
        def csvReader = new CsvParser()

        def (matrix, parseInfo) = csvReader.parse(new File('test_data/DiogenesMockData.txt'))

        assert matrix.size() == 1986
        assert matrix[0].size == 163
    }

    void testMatrixImporterInputStream() {

        def matrixImporterInstance = MatrixImporter.instance

        def fileInputStream = new FileInputStream('test_data/DiogenesMockData_mini.txt')

        def (matrix, parseInfo) = matrixImporterInstance.importInputStream(fileInputStream)

        fileInputStream.close()

        assert matrix
        assert parseInfo

    }

    void testMatrixImporterString() {

        def matrixImporterInstance = MatrixImporter.instance

        def s = "1,2,3,2,311,\n,3,53,23"

        def matrix = matrixImporterInstance.importString(s, [delimiter: ','])

        assert matrix == [['1','2','3','2','311'],['','3','53','23']]

    }
}
