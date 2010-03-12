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
import org.apache.poi.hssf.usermodel.HSSFDateUtil
import dbnp.studycapturing.TemplateFieldType
import dbnp.studycapturing.Study
import dbnp.studycapturing.Subject
import dbnp.studycapturing.Event
import dbnp.studycapturing.Protocol
import dbnp.studycapturing.Sample


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
     * @return header representation as a MappingColumn array
     */
    def getHeader(HSSFWorkbook wb, int sheetindex){

	def sheet = wb.getSheetAt(sheetindex)
	def datamatrix_start = sheet.getFirstRowNum() + 1
	//def header = []
	def header = [:]
        def df = new DataFormatter()


	for (HSSFCell c: sheet.getRow(datamatrix_start)) {
	    def datamatrix_celltype = sheet.getRow(datamatrix_start).getCell(c.getColumnIndex()).getCellType()
	    def headercell = sheet.getRow(sheet.getFirstRowNum()).getCell(c.getColumnIndex())

            // Check for every celltype, currently redundant code, but possibly this will be 
	    // a piece of custom code for every cell type like specific formatting
	        
	    switch (datamatrix_celltype) {
                    case HSSFCell.CELL_TYPE_STRING:
			    header[c.getColumnIndex()] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell), templatefieldtype:TemplateFieldType.STRING);
			    break
                    case HSSFCell.CELL_TYPE_NUMERIC:			
			    if (HSSFDateUtil.isCellDateFormatted(c)) {
				println("DATE")
				header[c.getColumnIndex()] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell), templatefieldtype:TemplateFieldType.DATE)
			    }
			    else
				header[c.getColumnIndex()] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell), templatefieldtype:TemplateFieldType.INTEGER);
			    break
		    case HSSFCell.CELL_TYPE_BLANK:
			    header[c.getColumnIndex()] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell), templatefieldtype:TemplateFieldType.STRING);
			    break
                    default:
			    header[c.getColumnIndex()] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell), templatefieldtype:TemplateFieldType.STRING);
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
	def datamatrix_start = 1

	// walk through all rows
	(count <= sheet.getLastRowNum()) ?
	((datamatrix_start+sheet.getFirstRowNum())..count).each { rowindex ->
	    def row = []

	    // walk through every cell
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

    /**
    * Method to read data from a workbook and to import data into the database
    * by using mapping information
    *
    * @param wb POI horrible spreadsheet formatted workbook object
    * @param mc array of MappingColumns
    * @param sheetindex sheet to use when using multiple sheets
    * @param rowindex first row to start with reading the actual data (NOT the header)
    * 
    * @see dbnp.importer.MappingColumn
    */
    def importdata(HSSFWorkbook wb, int sheetindex, int rowindex, MappingColumn[] mcarray) {
	def sheet = wb.getSheetAt(sheetindex)
	def rows  = []
	def df = new DataFormatter()

	// walk through all rows	
	rowindex..sheet.getLastRowNum().each { i ->
	    def record = [:]

	    // get the value of the cells in the row
	    for (HSSFCell c: sheet.getRow(i))		
		mc = mcarray[c.getColumnIndex()]		
		record.add(createColumn(c, mc))
	}
    }

    /**
    * This function creates a column based on the current cell and mapping
    *
    * @param cell POI cell read from Excel
    * @param mc mapping column
    * @return entity object
    * 
    */
    def createColumn(HSSFCell cell, MappingColumn mc) {
	def df = new DataFormatter()

	// check the templatefield type of the cell
	switch(mc.entity) {
	    case Study	:   def st = new Study()
			    st.setFieldValue(mc.name, df.formatCellValue(c))
			    return st
			    break
	    case Subject:   def su = new Subject()
			    su.setFieldValue(mc.name, df.formatCellValue(c))
			    return su
			    break
	    case Event  :   def ev = new Event()
			    ev.setFieldValue(mc.name, df.formatCellValue(c))
			    return ev
			    break
	    case Protocol:  def pr = new Protocol()
			    pr.setFieldValue(mc.name, df.formatCellValue(c))
			    return pr
			    break
	    case Sample	:   def sa = new Sample()
			    sa.setFieldValue(mc.name, df.formatCellValue(c))
			    return sa
			    break
	    case Object :   break
	    
	}

    }
}
