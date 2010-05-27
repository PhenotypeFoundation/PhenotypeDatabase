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

import dbnp.studycapturing.TemplateFieldType
import dbnp.studycapturing.Template
import dbnp.studycapturing.Study
import dbnp.studycapturing.Subject
import dbnp.studycapturing.Event

import dbnp.studycapturing.Sample

import dbnp.data.Term

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
     * @param sheetindex sheet to use within the workbook
     * @return header representation as a MappingColumn hashmap
     */
    def getHeader(HSSFWorkbook wb, int sheetindex, theEntity=0){

	def sheet = wb.getSheetAt(sheetindex)
	def datamatrix_start = sheet.getFirstRowNum() + 1
	//def header = []
	def header = [:]
        def df = new DataFormatter()
	def property = new dbnp.studycapturing.TemplateField()

	for (HSSFCell c: sheet.getRow(datamatrix_start)) {
	    def index	=   c.getColumnIndex()
	    def datamatrix_celltype = sheet.getRow(datamatrix_start).getCell(index).getCellType()
	    def headercell = sheet.getRow(sheet.getFirstRowNum()).getCell(index)

            // Check for every celltype, currently redundant code, but possibly this will be 
	    // a piece of custom code for every cell type like specific formatting
	        
	    switch (datamatrix_celltype) {
                    case HSSFCell.CELL_TYPE_STRING:
			    header[index] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell),
									    templatefieldtype:TemplateFieldType.STRING,
									    index:index,
									    entity:theEntity,
									    property:property);
			    break
                    case HSSFCell.CELL_TYPE_NUMERIC:			
			    if (HSSFDateUtil.isCellDateFormatted(c)) {
				header[index] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell),
										templatefieldtype:TemplateFieldType.DATE,
										index:index,
										entity:theEntity,
										property:property)
			    }
			    else
				header[index] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell),
										templatefieldtype:TemplateFieldType.INTEGER,
										index:index,
										entity:theEntity,
										property:property);
			    break
		    case HSSFCell.CELL_TYPE_BLANK:
			    header[index] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell),
									    templatefieldtype:TemplateFieldType.STRING,
									    index:index,
									    entity:theEntity,
									    property:property);
			    break
                    default:
			    header[index] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell),
									    templatefieldtype:TemplateFieldType.STRING,
									    index:index,
									    entity:theEntity,
									    property:property);
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
    * @param template_id template identifier to use fields from
    * @param wb POI horrible spreadsheet formatted workbook object
    * @param mcmap linked hashmap (preserved order) of MappingColumns
    * @param sheetindex sheet to use when using multiple sheets
    * @param rowindex first row to start with reading the actual data (NOT the header)
    * @return two dimensional array containing records (with entities)
    * 
    * @see dbnp.importer.MappingColumn
    */
    def importdata(template_id, HSSFWorkbook wb, int sheetindex, int rowindex, mcmap) {
	def sheet = wb.getSheetAt(sheetindex)
	def table = []

	
	// walk through all rows	
	(rowindex..sheet.getLastRowNum()).each { i ->
	    table.add(createRecord(template_id, sheet.getRow(i), mcmap))
	}

	/*table.each {
	    it.each { entity ->
		entity.giveFields().each { field ->
		    print field.name + ":" + entity.getFieldValue(field.name) + "/"
		}
		println
	    }
	}*/

	return table	
    }
    
     /**
                     // start transaction
                        def transaction = sessionFactory.getCurrentSession().beginTransaction()
                              // persist data to the database
                                try {
                                   // commit transaction
                                        println "commit"
                                        transaction.commit()
                                        success()
                                } catch (Exception e) {
                                        // rollback
                                        // stacktrace in flash scope
                                        flash.debug = e.getStackTrace()

                                        println "rollback"
                                        transaction.rollback()
                                        error()
                                }
      */

    /**
     * Method to store a matrix containing the entities in a record like structure. Every row in the table
     * contains one or more entity objects (which contain fields with values). So actually a row represents
     * a record with fields from one or more different entities.
     *
     * @param study entity Study
     * @param datamatrix two dimensional array containing entities with values read from Excel file     *
     */    
    def saveDatamatrix(Study study, datamatrix) {
	study.refresh()
	
	datamatrix.each { record ->
	    record.each { entity ->
		switch (entity.getClass()) {
		    case Study	 :  print "Persisting Study `" + entity.title + "`: "
				    persistEntity(entity)
				    break
		    case Subject :  print "Persisting Subject `" + entity.name + "`: "
				    persistEntity(entity)
				    study.addToSubjects(entity)
				    break
		    case Event	 :  print "Persisting Event `" + entity.eventdescription + "`: "
				    persistEntity(entity)
				    break
		    case Sample  :  print "Persisting Sample `" + entity.name +"`: "
				    persistEntity(entity)
				    break
		    default	 :  println "Skipping persisting of `" + entity.getclass() +"`"
				    break
		}
	    }
	}
    }

    /**
     * Method to persist entities into the database
     * Checks whether entity already exists (based on identifier column 'name')
     * 
     * @param entity entity object like Study, Subject, Protocol et cetera
     * 
     */
    def persistEntity(entity) {	
	if (entity.save(flush:true)) {  //.merge?	    
        } else entity.errors.allErrors.each {
		println it
	}
    }

    /**
     * This method creates a record (array) containing entities with values
     *
     * @param template_id template identifier
     * @param excelrow POI based Excel row containing the cells
     * @param mcmap map containing MappingColumn objects
     */
    def createRecord(template_id, HSSFRow excelrow, mcmap) {
	def df = new DataFormatter()
	def template = Template.get(template_id)
	def record = []	
	
	def study = new Study(title:"New study", template:template)
	def subject = new Subject(name:"New subject", species:Term.findByName("Homo sapiens"), template:template)
	def event = new Event(eventdescription:"New event", template:template)
	def sample = new Sample(name:"New sample", template:template)

	for (HSSFCell cell: excelrow) {	    
	    def mc = mcmap[cell.getColumnIndex()]
	    def value = formatValue(df.formatCellValue(cell), mc.templatefieldtype)

	    switch(mc.entity) {
		case Study	:   (record.any {it.getClass()==mc.entity}) ? 0 : record.add(study)
				    if (mc.identifier) { study.title = value; break }
				    study.setFieldValue(mc.property.name, value)
				    break
	        case Subject	:   (record.any {it.getClass()==mc.entity}) ? 0 : record.add(subject)
				    if (mc.identifier) { subject.name = value; break }
				    subject.setFieldValue(mc.property.name, value)				    
				    break
		case Event	:   (record.any {it.getClass()==mc.entity}) ? 0 : record.add(event)
				    if (mc.identifier) { event.eventdescription = value; break }
				    event.setFieldValue(mc.property.name, value)
				    break
		case Sample	:   (record.any {it.getClass()==mc.entity}) ? 0 : record.add(sample)
				    if (mc.identifier) { sample.name = value; break }
				    sample.setFieldValue(mc.property.name, value)
				    break
		case Object	:   // don't import
				    break
	    } // end switch
	} // end for

	return record
    }

    /**
    * Method to parse a value conform a specific type
    * @param value string containing the value
    * @return object corresponding to the TemplateFieldType
    */
    def formatValue(String value, TemplateFieldType type) {	
	switch (type) {
	    case TemplateFieldType.STRING	:   return value.trim()
	    case TemplateFieldType.TEXT		:   return value.trim()
	    case TemplateFieldType.INTEGER	:   return Integer.valueOf(value.replaceAll("[^0-9]",""))
	    case TemplateFieldType.FLOAT	:   return Float.valueOf(value.replace(",","."));
	    case TemplateFieldType.DOUBLE	:   return Double.valueOf(value.replace(",","."));
	    case TemplateFieldType.STRINGLIST	:   return value.trim()
	    case TemplateFieldType.ONTOLOGYTERM	:   return value.trim()
	    case TemplateFieldType.DATE		:   return value
	    default				:   return value
	}
    }
}
