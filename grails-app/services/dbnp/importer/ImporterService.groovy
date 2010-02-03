/**
 * Importer service
 *
 * The importer service handles the import of tabular, comma delimited and Excel format
 * based files.
 *
 * @package	importer
 * @author	t.w.abma@umcutrecht.nl
 * @since	20100126
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

package dbnp.importer
import org.apache.poi.hssf.usermodel.*
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.DataFormatter


class ImporterService {

    boolean transactional = true

    /**
    * @param is input stream representing the (workbook) resource
    * @return high level representation of the workbook
    */
    HSSFWorkbook getWorkbook(InputStream is) {
	POIFSFileSystem	fs = new POIFSFileSystem(is)
	HSSFWorkbook	wb = new HSSFWorkbook(fs);
	return wb;
    }

    /**
     * @param wb high level representation of the workbook
     * @return header representation as a string array
     */
    def getHeader(HSSFWorkbook wb, int sheetindex){

	def sheet = wb.getSheetAt(sheetindex)
	def datamatrix_start = sheet.getFirstRowNum() + 1
	def header = []        
        def df = new DataFormatter()

	for (HSSFCell c: sheet.getRow(sheet.getFirstRowNum())) {
	    def datamatrix_celltype = sheet.getRow(datamatrix_start).getCell(c.getColumnIndex()).getCellType()

            // Check for every celltype, currently redundant code, but possibly this will be 
	    // a piece of custom code for every cell type like specific formatting
	    
	    switch (c.getCellType()) {
                    case HSSFCell.CELL_TYPE_STRING: 			
			header.add (columnindex:c.getColumnIndex(), value:df.formatCellValue(c), celltype:datamatrix_celltype);
			break
                    case HSSFCell.CELL_TYPE_NUMERIC:
			header.add (columnindex:c.getColumnIndex(), value:df.formatCellValue(c), celltype:datamatrix_celltype);
			break
		    case HSSFCell.CELL_TYPE_BLANK:
			header.add (columnindex:c.getColumnIndex(), value:"-", celltype:datamatrix_celltype);
			break
                    default:
			header.add (columnindex:c.getColumnIndex(), value:df.formatCellValue(c), celltype:datamatrix_celltype);
			break
            }
	}
        return header
    }

    /**
     * This method is meant to return a matrix of the rows and columns
     * used in the preview
     *
     * @param wb workbook object
     * @param sheetindex sheet index used
     * @param rows amount of rows returned
     * @return two dimensional array (matrix) of HSSFCell objects
     */

    HSSFCell[][] getDatamatrix(HSSFWorkbook wb, int sheetindex, int count) {
	def sheet = wb.getSheetAt(sheetindex)
	def rows  = []
	def df = new DataFormatter()

	(count <= sheet.getLastRowNum()) ?
	((1+sheet.getFirstRowNum())..count).each { rowindex ->

	    def row = []
	    for (HSSFCell c: sheet.getRow(rowindex))
		row.add(c)
		//row.add(df.formatCellValue(c))
	    rows.add(row)
	} : 0

	return rows
    }

    /**
    * This method will move a file to a new location.
    *
    * @param file File object to move
    * @param folderpath folder to move the file to
    * @param filename (new) filename to give
    * @return if file has been moved succesful, the new path and filename will be returned, otherwise an empty string will be returned
    */
    def moveFile(File file, String folderpath, String filename) {
        try {
		def rnd = ""; //System.currentTimeMillis()
		file.transferTo(new File(folderpath, rnd+filename))
		return folderpath + filename
	    } catch(Exception exception) {
		log.error "File move error, ${exception}"
		return ""
		}
    }

    /**
    * @return random numeric value
    */
    def random = {
	    return System.currentTimeMillis() + Runtime.runtime.freeMemory()
	}
}
