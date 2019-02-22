package org.dbxp.matriximporter

import org.apache.commons.logging.LogFactory
import org.apache.poi.ss.usermodel.*

/**
 * This class is capable of importing Excel (.xls and .xlsx) files
 * 
 * @author robert
 *
 */
public class ExcelParser extends MatrixParser{

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
		return fileName ? fileName.matches(/.+\.xls$/) : true
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

		// Read the file with Apache POI 
		def workbook = WorkbookFactory.create( inputStream )
		def sheet = workbook.getSheetAt(sheetIndex)
		
		def dataFormatter = new DataFormatter()
		def dataMatrix = []
		FormulaEvaluator formulaEvaluator = workbook.creationHelper.createFormulaEvaluator()

        if (hints.endRow == null) hints.endRow = sheet.lastRowNum

        def startRow =  forceValueInRange(hints.startRow ?: 0, sheet.firstRowNum, sheet.lastRowNum)
        def endRow =    forceValueInRange(hints.endRow, startRow, sheet.lastRowNum)

		// Determine amount of columns: the number of columns in the first row
		def columnCount = sheet.getRow(startRow)?.lastCellNum

        // A counter to keep track of the amount of formulas that could not be interpreted by POI
        def failedFormulas = 0

		// Walk through all rows
		(startRow..endRow).each { rowIndex ->

			def dataMatrixRow = []

			// Get the current row
			def excelRow = sheet.getRow(rowIndex)

			// Excel contains some data?
			if (excelRow)
				columnCount.times { columnIndex ->

					def cell = excelRow.getCell(columnIndex)

                    String stringValue = ''

					switch (cell?.cellType) {
						case Cell.CELL_TYPE_NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell))
                                stringValue = dataFormatter.formatCellValue(cell)
                            else {
                                // Set the cell type to string, this prevents any kind of formatting
                                cell.cellType = Cell.CELL_TYPE_STRING
                                stringValue = cell.stringCellValue
                            }
							break
                        case Cell.CELL_TYPE_STRING:
                            stringValue = cell.stringCellValue
                            break
						case Cell.CELL_TYPE_FORMULA:
                            try {
                                CellValue cellValue = formulaEvaluator.evaluate(cell)

                                if (cellValue.cellType == Cell.CELL_TYPE_STRING) stringValue = cellValue.stringValue
                                else if (cellValue.cellType == Cell.CELL_TYPE_NUMERIC) stringValue = cellValue.numberValue.toString()

                            } catch (e) {
                                failedFormulas++
                                if (failedFormulas>9) {
                                    throw new RuntimeException("There are at least 10 formulas in this sheet that could not be interpreted (e.g. \"RAND\"). Consider changing formulas to values. Aborting import now.")
                                }
                                log.error("Unable to parse formula of cell at row: $rowIndex, column: $columnIndex.", e)
                            }
							break
						default:
                            break
					}

                    dataMatrixRow.add(stringValue)
				}

			if ( dataMatrixRow.any {it} ) // is at least 1 of the cells non empty?
				dataMatrix << dataMatrixRow
		}
		[dataMatrix, [sheetIndex: sheetIndex, numberOfSheets: workbook.numberOfSheets]]
	}

	/**
	* Returns a description for this parser
	* @return	Human readable description
	*/
   public String getDescription() {
	   return "Matrix importer for reading XLS files"
   }

}
