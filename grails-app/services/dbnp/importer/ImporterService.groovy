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
import org.apache.poi.ss.usermodel.*

import dbnp.studycapturing.TemplateFieldType
import dbnp.studycapturing.Template
import dbnp.studycapturing.SamplingEvent
import dbnp.studycapturing.Study
import dbnp.studycapturing.Subject
import dbnp.studycapturing.Event
import dbnp.studycapturing.Sample

class ImporterService {
    def AuthenticationService

    boolean transactional = true

    /**
    * @param is input stream representing the (workbook) resource
    * @return high level representation of the workbook
    */
    Workbook getWorkbook(InputStream is) {
        WorkbookFactory.create(is)
    }

    /**
     * @param wb high level representation of the workbook
     * @param sheetindex sheet to use within the workbook
     * @return header representation as a MappingColumn hashmap
     */
    def getHeader(Workbook wb, int sheetindex, int headerrow, int datamatrix_start, theEntity=null){

	def sheet = wb.getSheetAt(sheetindex)	
	def sheetrow = sheet.getRow(datamatrix_start)
	//def header = []
	def header = [:]
        def df = new DataFormatter()
	def property = new String()

	//for (Cell c: sheet.getRow(datamatrix_start)) {

	(0..sheetrow.getLastCellNum() -1 ).each { columnindex ->

	    //def index	=   c.getColumnIndex()
	    def datamatrix_celltype = sheet.getRow(datamatrix_start).getCell(columnindex,Row.CREATE_NULL_AS_BLANK).getCellType()
	    def datamatrix_celldata = df.formatCellValue(sheet.getRow(datamatrix_start).getCell(columnindex))
	    def datamatrix_cell	    = sheet.getRow(datamatrix_start).getCell(columnindex)	    
            def headercell = sheet.getRow(headerrow-1+sheet.getFirstRowNum()).getCell(columnindex)
	    def tft = TemplateFieldType.STRING //default templatefield type

            // Check for every celltype, currently redundant code, but possibly this will be
	    // a piece of custom code for every cell type like specific formatting	    
	        
	    switch (datamatrix_celltype) {
                    case Cell.CELL_TYPE_STRING:
			    //parse cell value as double
			    def doubleBoolean = true
			    def fieldtype = TemplateFieldType.STRING

			    // is this string perhaps a double?
			    try {
				formatValue(datamatrix_celldata, TemplateFieldType.DOUBLE)
			    } catch (NumberFormatException nfe) { doubleBoolean = false }
			    finally {
				if (doubleBoolean) fieldtype = TemplateFieldType.DOUBLE
			    }

			    header[columnindex] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell),
									    templatefieldtype:fieldtype,
									    index:columnindex,
									    entity:theEntity,
									    property:property);

			    break
                    case Cell.CELL_TYPE_NUMERIC:
			    def fieldtype = TemplateFieldType.INTEGER
			    def doubleBoolean = true
			    def integerBoolean = true

			    // is this cell really an integer?
			    try {
				Integer.valueOf(datamatrix_celldata)
			    } catch (NumberFormatException nfe) { integerBoolean = false }
			    finally {
				if (integerBoolean) fieldtype = TemplateFieldType.INTEGER
			    }

			    // it's not an integer, perhaps a double?
			    if (!integerBoolean)
				try {
				    formatValue(datamatrix_celldata, TemplateFieldType.DOUBLE)
				} catch (NumberFormatException nfe) { doubleBoolean = false }
				finally {
				    if (doubleBoolean) fieldtype = TemplateFieldType.DOUBLE
				}

			    if (DateUtil.isCellDateFormatted(datamatrix_cell)) fieldtype = TemplateFieldType.DATE

			    header[columnindex] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell),
									    templatefieldtype:fieldtype,
									    index:columnindex,
									    entity:theEntity,
									    property:property);
			    break
		    case Cell.CELL_TYPE_BLANK:
			    header[columnindex] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell),
									    templatefieldtype:TemplateFieldType.STRING,
									    index:columnindex,
									    entity:theEntity,
									    property:property);
			    break
                    default:
			    header[columnindex] = new dbnp.importer.MappingColumn(name:df.formatCellValue(headercell),
									    templatefieldtype:TemplateFieldType.STRING,
									    index:columnindex,
									    entity:theEntity,
									    property:property);
			    break
            } // end of switch
	} // end of cell loop
        return header
    }

    /**
     * This method is meant to return a matrix of the rows and columns
     * used in the preview
     *
     * @param wb workbook object
     * @param sheetindex sheet index used
     * @param rows amount of rows returned
     * @return two dimensional array (matrix) of Cell objects
     */

    Cell[][] getDatamatrix(Workbook wb, header, int sheetindex, int datamatrix_start, int count) {
	def sheet = wb.getSheetAt(sheetindex)
	def rows  = []
	def df = new DataFormatter()	

	// walk through all rows
	(count <= sheet.getLastRowNum()) ?
	((datamatrix_start+sheet.getFirstRowNum())..count).each { rowindex ->
	    def row = []

	    // walk through every cell
	    /*for (Cell c: sheet.getRow(rowindex)) {
		row.add(c)
		println c.getColumnIndex() + "=" +c
	    }*/
	    
	    (0..header.size()-1).each { columnindex ->
		def c = sheet.getRow(rowindex).getCell(columnindex, Row.CREATE_NULL_AS_BLANK)
		//row.add(df.formatCellValue(c))
		row.add(c)
		//if (c.getCellType() == c.CELL_TYPE_STRING) println "STR"+c.getStringCellValue()
		//if (c.getCellType() == c.CELL_TYPE_NUMERIC) println "INT" +c.getNumericCellValue()
	    }
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
    * Method to read data from a workbook and to import data into a two dimensional
    * array
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
    def importData(template_id, Workbook wb, int sheetindex, int rowindex, mcmap) {
	def sheet = wb.getSheetAt(sheetindex)
	def table = []
        def failedcells = [:] // map [recordhash, [mappingcolumnlist]] values
	
	// walk through all rows and fill the table with records
	(rowindex..sheet.getLastRowNum()).each { i ->
	    //table.add(createRecord(template_id, sheet.getRow(i), mcmap))
            // Create an entity record based on a row read from Excel and store the cells which failed to be mapped
            def (record, failed) = createRecord(template_id, sheet.getRow(i), mcmap)

            // Add record with entity and its values to the table
            table.add(record)

            // If failed cells have been found, add them to the failed cells map
            // the record hashcode is later on used to put the failed data back
            // in the data matrix
            if (failed.size()!=0) failedcells.put(record.hashCode(), failed)
	}

        failedcells.each { record ->
           record.each { cell ->
               cell.each {
                   println it.value.dump()
               }
            }
        }


	return [table,failedcells]
    }

    /** Method to put failed cells back into the datamatrix. Failed cells are cell values
     * which could not be stored in an entity (e.g. Humu Supiuns in an ontology field).
     * Empty corrections should not be stored
     *
     * @param datamatrix two dimensional array containing entities and possibly also failed cells
     * @param failedcells list with maps of failed cells in [mappingcolumn, cell] format
     * @param correctedcells map of corrected cells in [cellhashcode, value] format
     **/
    def saveCorrectedCells(datamatrix, failedcells, correctedcells) {       
        // Loop through all failed cells

        failedcells.each { mcrecord ->
            mcrecord.each { mappingcolumn ->
                  // Get the corrected value
                  def correctedvalue = correctedcells.find { it.key.toInteger() == mappingcolumn.hashCode()}.value

                  // Find the record in the table which the mappingcolumn belongs to
                  def tablerecord = datamatrix.find { it.hashCode() == mcrecord.key }

                  // Loop through all entities in the record
                  tablerecord.each { rec ->
                      rec.each { entity ->
                            try {
                                // Update the entity field
                                entity.setFieldValue(mappingcolumn.value.property[0], correctedvalue)
                                println "Adjusted " + mappingcolumn.value.property[0] + " to " + correctedvalue
                            }
                            catch (Exception e) {
                                println "Could not map corrected ontology"
                            }
                      }
                  } // end of table record
            } // end of mapping record
        } // end of failed cells loop     
    }
   
    /**
     * Method to store a matrix containing the entities in a record like structure. Every row in the table
     * contains one or more entity objects (which contain fields with values). So actually a row represents
     * a record with fields from one or more different entities.
     *
     * @param study entity Study
     * @param datamatrix two dimensional array containing entities with values read from Excel file
     */    
    def saveDatamatrix(Study study, datamatrix) {
	def validatedSuccesfully = 0
        def entitystored = null
	study.refresh()        
	
	// go through the data matrix, read every record and validate the entity and try to persist it
	datamatrix.each { record ->
	    record.each { entity ->
                        switch (entity.getClass()) {
                        case Study	 :  print "Persisting Study `" + entity + "`: "
                                                entity.owner = AuthenticationService.getLoggedInUser()
                                            	if (persistEntity(entity)) validatedSuccesfully++
						break
			case Subject	 :  print "Persisting Subject `" + entity + "`: "
                                                entity.parent = study
                                                
                                                // is the current entity not already in the database?
                                                entitystored = isEntityStored(entity)
                                                
                                                // this entity is new, so add it to the study
                                                if (entitystored==null) study.addToSubjects(entity)
                                                else // existing entity, so update it
                                                    updateEntity(entitystored, entity)

                                                if (persistEntity(study)) validatedSuccesfully++
						break
			case Event	 :  print "Persisting Event `" + entity + "`: "
                                                entity.parent = study
						study.addToEvents(entity)
						if (persistEntity(entity)) validatedSuccesfully++
						break
			case Sample	 :  print "Persisting Sample `" + entity +"`: "
                                                entity.parent = study
						study.addToSamples(entity)
						if (persistEntity(entity)) validatedSuccesfully++
						break
			case SamplingEvent: print "Persisting SamplingEvent `" + entity + "`: "
                                                entity.parent = study
						study.addToSamplingEvents(entity)
						if (persistEntity(entity)) validatedSuccesfully++
						break
			default		 :  println "Skipping persisting of `" + entity.getclass() +"`"
						break
			} // end switch
	    } // end record
	} // end datamatrix
	return validatedSuccesfully
    }

    /**
     * Check whether an entity already exist. A unique field in the entity is
     * used to check whether the instantiated entity (read from Excel) is new.
     * If the entity is found in the database it will be returned as is.
     *
     * @param entity entity object like a Study, Subject, Sample et cetera
     * @return entity if found, otherwise null
     */
    def isEntityStored(entity) {
            switch (entity.getClass()) {
                        case Study          :  return Study.findByCode(entity.code)
                                               break
			case Subject        :  return Subject.findByParentAndName(entity.parent, entity.name)
                                               break
                        case Event          :  break
                        case Sample         :  break
                        case SamplingEvent  :  break
                        default             :  // unknown entity
                                               return null
            }
    }

    /**
     * Find the entity and update the fields. The entity is an instance
     * read from Excel. This method looks in the database for the entity
     * having the same identifier. If it has found the same entity
     * already in the database, it will update the record.
     *
     * @param entitystored existing record in the database to update
     * @param entity entity read from Excel
     */
    def updateEntity(entitystored, entity) {
        switch (entity.getClass()) {
                        case Study          :  break
			case Subject        :  entitystored.properties = entity.properties
                                               entitystored.save()
                                               break
                        case Event          :  break
                        case Sample         :  break
                        case SamplingEvent  :  break
                        default             :  // unknown entity
                                               return null
        }
    }

    /**
     * Method to persist entities into the database
     * Checks whether entity already exists (based on identifier column 'name')
     * 
     * @param entity entity object like Study, Subject, Protocol et cetera
     * 
     */
    boolean persistEntity(entity) {
	    println "persisting ${entity}"            
	    // if not validated
		if (entity.validate()) {
			if (entity.save()) { //.merge?
				return true
			}
			else { // if save was unsuccesful
				entity.errors.allErrors.each {
					println it
				}
				return false
			}
		}
	    else { // if not validated
		    entity.errors.each {
			    println it
		    }
			return false
	    }
	 }

	/**
	 * This method creates a record (array) containing entities with values
	 *
	 * @param template_id template identifier
	 * @param excelrow POI based Excel row containing the cells
	 * @param mcmap map containing MappingColumn objects
         * @return list of entities and list of failed cells
	 */
	def createRecord(template_id, Row excelrow, mcmap) {
		def df = new DataFormatter()
		def template = Template.get(template_id)
                def tft = TemplateFieldType
		def record = [] // list of entities and the read values
                def failed = [] // list of failed columns [mappingcolumn] with the value which couldn't be mapped into the entity

		// Initialize all possible entities with the chosen template
		def study = new Study(template: template)
		def subject = new Subject(template: template)
		def samplingEvent = new SamplingEvent(template: template)
		def event = new Event(template: template)
		def sample = new Sample(template: template)

		// Go through the Excel row cell by cell
		for (Cell cell: excelrow) {
			// get the MappingColumn information of the current cell
			def mc = mcmap[cell.getColumnIndex()]
			def value                        

			// Check if column must be imported
			if (mc!=null) if (!mc.dontimport) {
				try {
					value = formatValue(df.formatCellValue(cell), mc.templatefieldtype)
				} catch (NumberFormatException nfe) {
					value = ""
				}


                                //println "temateplfedielfdtype=" + mc.templatefieldtype
                                // Are we trying to map an ontology term which is empty? Then it is a failed cell
                                /*if (value=="") {
                                    println "empty term"
                                    def temp = new MappingColumn()
                                    //temp.properties = mc.properties
                                    temp.value = "undefined"
                                    failed.add(temp)
                                }*/
                              

                                try {
                                    /*if ((mc.templatefieldtype == TemplateFieldType.ONTOLOGYTERM) && (value == ""))
                                        {
                                            def temp = new MappingColumn()
                                            temp.properties = mc.properties
                                            temp.value="unknown"
                                            failed.add(temp)
                                        }*/

				// which entity does the current cell (field) belong to?
                                    switch (mc.entity) {
					case Study: // does the entity already exist in the record? If not make it so.
                                                (record.any {it.getClass() == mc.entity}) ? 0 : record.add(study)
						study.setFieldValue(mc.property, value)
						break
					case Subject: (record.any {it.getClass() == mc.entity}) ? 0 : record.add(subject)
						subject.setFieldValue(mc.property, value)
						break
					case SamplingEvent: (record.any {it.getClass() == mc.entity}) ? 0 : record.add(samplingEvent)
						samplingEvent.setFieldValue(mc.property, value)
						break
					case Event: (record.any {it.getClass() == mc.entity}) ? 0 : record.add(event)
						event.setFieldValue(mc.property, value)
						break
					case Sample: (record.any {it.getClass() == mc.entity}) ? 0 : record.add(sample)
						sample.setFieldValue(mc.property, value)
						break
					case Object:   // don't import
						break
                                    } // end switch
                                } catch (IllegalArgumentException iae) {
                                    // store the mapping column and value which failed
                                    def temp = new MappingColumn()
                                    temp.properties = mc.properties
                                    temp.value = value
                                    failed.add(temp)
                                }
			} // end
		} // end for
	
        // a failed column means that using the entity.setFieldValue() threw an exception        
        return [record, failed]        
    }

    /**
    * Method to parse a value conform a specific type
    * @param value string containing the value
    * @return object corresponding to the TemplateFieldType
    */
    def formatValue(String value, TemplateFieldType type) throws NumberFormatException {
	    switch (type) {
		case TemplateFieldType.STRING	    :   return value.trim()
		case TemplateFieldType.TEXT	    :   return value.trim()
		case TemplateFieldType.INTEGER	    :   return (int) Double.valueOf(value)
		case TemplateFieldType.FLOAT	    :   return Float.valueOf(value.replace(",","."));
		case TemplateFieldType.DOUBLE	    :   return Double.valueOf(value.replace(",","."));
		case TemplateFieldType.STRINGLIST   :   return value.trim()
		case TemplateFieldType.ONTOLOGYTERM :   return value.trim()
		case TemplateFieldType.DATE	    :   return value
		default				    :   return value
	    }
    }

}
