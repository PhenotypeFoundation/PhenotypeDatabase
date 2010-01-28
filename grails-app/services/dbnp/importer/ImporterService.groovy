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

    def hello() { println "hello "}

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
    def getHeader(HSSFWorkbook wb){
	def sheet = wb.getSheetAt(0)
        def row = 0
	def header = []
        def cellvalue
        def df = new DataFormatter()

	for (HSSFCell c: sheet.getRow(row)) {
            switch (c.getCellType()) {
                    case HSSFCell.CELL_TYPE_STRING: header.add (df.formatCellValue(c)); break
                    case HSSFCell.CELL_TYPE_NUMERIC: header.add (df.formatCellValue(c)); break
                    case HSSFCell.CELL_TYPE_BLANK: header.add(""); break
                    default: header.add("")
            }	    
	}
        return header
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
