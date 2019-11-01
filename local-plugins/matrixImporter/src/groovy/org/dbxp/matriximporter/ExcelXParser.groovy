package org.dbxp.matriximporter

import org.apache.commons.logging.LogFactory
import org.apache.poi.util.SAXHelper
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler
import org.apache.poi.xssf.model.StylesTable
import org.apache.poi.xssf.usermodel.XSSFComment
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader
import org.xml.sax.ContentHandler


/**
 * This class is capable of importing large Excel (.xlsx) files
 */
public class ExcelXParser extends MatrixParser{

    private static def log = LogFactory.getLog(this)

    /**
     * Returns true if this class is able to parse files with a given name. This
     * is done by checking if the extension equals '.xls' or '.xlsx'. Also
     * returns true if fileName is null or ''.
     *
     * @param file	File object to read
     * @return true if the parser can parse the file, false otherwise
     */
    public boolean canParse( Map hints = [:] ) {
        def fileName = hints.fileName
        return fileName ? fileName.matches(/.+\.xlsx$/) : true
    }

    /**
     * Parses the given file and returns the matrix in that file
     * @param file	File object to read
     * @param hints	Hints for reading the excel file. Possible keys are:
     * 			startRow	0-based row number of the first row to read. (1 means start reading from the second row)
     * 						Defaults to the first row in the file
     * 			endRow		0-based row number of the last row to read.	 (2 means the 3rd row is the last to read)
     * 						Defaults to the last row in the file
     * 			sheetIndex	0-based index of the excel sheet to be read
     * 						Defaults to 0
     * @return      An Arraylist with:
     *              a. Two-dimensional data matrix of structure:
     * 				[
     * 					[ 1, 3, 5 ] // First line
     * 					[ 9, 1, 2 ] // Second line
     * 				]
     *
     * 			    b. A map with parse info containing:
     * 			    - sheetIndex, the sheet which was read
     * 			    - numberOfSheets, the number of sheets contained in the excel file
     */
    public ArrayList parse( InputStream inputStream, Map hints ) {
        def sheetIndex = hints.sheetIndex ?: 0

        XSSFWorkbook workbook = new XSSFWorkbook(inputStream) {
            /** Avoid DOM parse of large sheet */
            @Override
            public void parseSheet(java.util.Map<String, XSSFSheet> shIdMap, CTSheet ctSheet) {
            }
        };

        // Having avoided a DOM-based parse of the sheet, we can stream it instead.
        ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(workbook.getPackage())
        XSSFReader xssfReader = new XSSFReader(workbook.getPackage())
        StylesTable styles = workbook.getStylesSource()

        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        // Iterate to the required sheet
        int x = sheetIndex
        while (x > 0 && iter.hasNext()) {
            iter.next()
            x--
        }
        if (!iter.hasNext()) {
            return;
        }

        // Now we are at the specified sheet
        InputStream sheetInputStream = iter.next();
        String sheetName = iter.getSheetName();

        InputSource sheetSource = new InputSource(sheetInputStream);
        XSSFSheetXMLHandler.SheetContentsHandler sheetContentHandler = createSheetContentsHandler(
                hints.startRow, hints.endRow)

        try {
            XMLReader sheetParser = SAXHelper.newXMLReader();
            ContentHandler handler = new XSSFSheetXMLHandler(styles, strings, sheetContentHandler, false);
            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
        } catch (SAXException e) {
            // Check if we interrupted the parser on purpose.
            // If we did, we'll just ignore the exception.
            if (!e.cause instanceof PastEndRowException) {
                throw e;
            }
        }

        def dataMatrix = sheetContentHandler.dataMatrix

        sheetInputStream.close();
        workbook.close();
        inputStream.close();

        [dataMatrix, [sheetIndex: sheetIndex, numberOfSheets: workbook.numberOfSheets]]
    }

    /**
     * Returns custom SheetContentsHandler that saves all formatted values
     * between startRow to endRow in a data matrix
     * @param startRow
     * @param endRow
     * @return SheetContentsHandler
     */
    private static XSSFSheetXMLHandler.SheetContentsHandler createSheetContentsHandler(startRow, endRow) {
        def dataMatrix = []
        def dataMatrixRow = []
        def index = 0

        return new XSSFSheetXMLHandler.SheetContentsHandler() {

            @Override
            public void startRow(int rowNum) {
                dataMatrixRow = []
            }

            @Override
            public void headerFooter(String text, boolean isHeader, String tagName) {
            }

            @Override
            public void endRow(int rowNum) {
                if (index >= startRow) {
                    dataMatrix << dataMatrixRow
                }
                index++
                if (endRow && index > endRow) {
                    throw new PastEndRowException("End row reached");
                }
            }

            @Override
            public void cell(String cellReference, String formattedValue, XSSFComment comment) {
                if (index >= startRow) {
                    dataMatrixRow << formattedValue
                }
            }
        };
    }

    private static class PastEndRowException extends Exception {
        PastEndRowException(String message) {
            super(message);
        }
    }

    /**
     * Returns a description for this parser
     * @return	Human readable description
     */
    public String getDescription() {
        return "Matrix importer for reading XLSX files"
    }

}
