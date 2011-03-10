/**
 * Importer service
 *
 * The importer service handles the import of tabular, comma delimited and Excel format
 * based files.
 *
 * @package importer
 * @author t.w.abma@umcutrecht.nl
 * @since 20100126
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.importer

import org.dbnp.gdt.*
import org.apache.poi.ss.usermodel.*
import dbnp.studycapturing.*

class ImporterService {
	def authenticationService

	static transactional = false

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
	def getHeader(Workbook wb, int sheetindex, int headerrow, int datamatrix_start, theEntity = null) {
		def sheet = wb.getSheetAt(sheetindex)
		def sheetrow = sheet.getRow(datamatrix_start)
		//def header = []
		def header = []
		def df = new DataFormatter()
		def property = new String()

		//for (Cell c: sheet.getRow(datamatrix_start)) {

		(0..sheetrow.getLastCellNum() - 1).each { columnindex ->

			//def index	=   c.getColumnIndex()
			def datamatrix_celltype = sheet.getRow(datamatrix_start).getCell(columnindex, Row.CREATE_NULL_AS_BLANK).getCellType()
			def datamatrix_celldata = df.formatCellValue(sheet.getRow(datamatrix_start).getCell(columnindex))
			def datamatrix_cell = sheet.getRow(datamatrix_start).getCell(columnindex)
			def headercell = sheet.getRow(headerrow - 1 + sheet.getFirstRowNum()).getCell(columnindex)
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
					} catch (NumberFormatException nfe) {
						doubleBoolean = false
					}
					finally {
						if (doubleBoolean) fieldtype = TemplateFieldType.DOUBLE
					}

					header[columnindex] = new dbnp.importer.MappingColumn(name: df.formatCellValue(headercell),
							templatefieldtype: fieldtype,
							index: columnindex,
							entityclass: theEntity,
							property: property);

					break
				case Cell.CELL_TYPE_NUMERIC:
					def fieldtype = TemplateFieldType.LONG
					def doubleBoolean = true
					def longBoolean = true

				// is this cell really an integer?
					try {
						Long.valueOf(datamatrix_celldata)
					} catch (NumberFormatException nfe) {
						longBoolean = false
					}
					finally {
						if (longBoolean) fieldtype = TemplateFieldType.LONG
					}

				// it's not an long, perhaps a double?
					if (!longBoolean)
						try {
							formatValue(datamatrix_celldata, TemplateFieldType.DOUBLE)
						} catch (NumberFormatException nfe) {
							doubleBoolean = false
						}
						finally {
							if (doubleBoolean) fieldtype = TemplateFieldType.DOUBLE
						}

					if (DateUtil.isCellDateFormatted(datamatrix_cell)) fieldtype = TemplateFieldType.DATE

					header[columnindex] = new dbnp.importer.MappingColumn(name: df.formatCellValue(headercell),
							templatefieldtype: fieldtype,
							index: columnindex,
							entityclass: theEntity,
							property: property);
					break
				case Cell.CELL_TYPE_BLANK:
					header[columnindex] = new dbnp.importer.MappingColumn(name: df.formatCellValue(headercell),
					templatefieldtype: TemplateFieldType.STRING,
					index: columnindex,
					entityclass: theEntity,
					property: property);
					break
				default:
					header[columnindex] = new dbnp.importer.MappingColumn(name: df.formatCellValue(headercell),
					templatefieldtype: TemplateFieldType.STRING,
					index: columnindex,
					entityclass: theEntity,
					property: property);
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
	Object[][] getDatamatrix(Workbook wb, header, int sheetindex, int datamatrix_start, int count) {
		def sheet = wb.getSheetAt(sheetindex)
		def rows = []
		def df = new DataFormatter()

		count = (count < sheet.getLastRowNum()) ? count : sheet.getLastRowNum()

		// walk through all rows
		((datamatrix_start + sheet.getFirstRowNum())..count).each { rowindex ->
			def row = []

			(0..header.size() - 1).each { columnindex ->
				if (sheet.getRow(rowindex))
					row.add( sheet.getRow(rowindex).getCell(columnindex, Row.CREATE_NULL_AS_BLANK) )
			}

			rows.add(row)
		}

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
			file.transferTo(new File(folderpath, rnd + filename))
			return folderpath + filename
		} catch (Exception exception) {
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
	 * Retrieves records with sample, subject, samplingevent etc. from a study
	 * @param s		Study to retrieve records from
	 * @return		A list with hashmaps [ 'objects': [ 'Sample': .., 'Subject': .., 'SamplingEvent': .., 'Event': '.. ], 'templates': [], 'templateCombination': .. ]
	 */
	protected def getRecords( Study s ) {
		def records = [];

		s.samples?.each {
			def record = [ 'objects': retrieveEntitiesBySample( it ) ];

			def templates = [:]
			def templateCombination = [];
			record.objects.each { entity ->
				templates[ entity.key ] = entity.value?.template
				if( entity.value?.template )
					templateCombination << entity.key + ": " + entity.value?.template?.name;
			}

			record.templates = templates;
			record.templateCombination = templateCombination.join( ', ' )

			records << record
		}

		return records;
	}

	/**
	 * Returns a subject, event and samplingEvent that belong to this sample
	 * @param s		Sample to find the information for
	 * @return
	 */
	protected retrieveEntitiesBySample( Sample s ) {
		return [
			'Sample': s,
			'Subject': s?.parentSubject,
			'SamplingEvent': s?.parentEvent,
			'Event': s?.parentEventGroup?.events?.toList()?.getAt(0)
		]
	}

	/**
	 * Imports data from a workbook into a list of ImportRecords. If some entities are already in the database,
	 * these records are updated.
	 * 
	 * This method is capable of importing Subject, Samples, SamplingEvents and Events
	 * 
	 * @param	templates	Map of templates, identified by their entity as a key. For example: [ Subject: Template x, Sample: Template y ]
	 * @param	wb			Excel workbook to import
	 * @param	sheetindex	Number of the sheet to import data from
	 * @param	rowindex	Row to start importing from.
	 * @param	mcmap		Hashmap of mappingcolumns, with the first entry in the hashmap containing information about the first column, etc.
	 * @param	parent		Study to import all data into. Is used for determining which sample/event/subject/assay to update
	 * @param	createAllEntities	If set to true, the system will also create objects for entities that have no data imported, but do have
	 * 								a template assigned
	 * @return	List		List with two entries:
	 * 			0			List with ImportRecords, one for each row in the excelsheet
	 * 			1			List with ImportCell objects, mentioning the cells that could not be correctly imported
	 * 						(because the value in the excelsheet can't be entered into the template field)
	 */
	def importOrUpdateDataBySampleIdentifier( def templates, Workbook wb, int sheetindex, int rowindex, def mcmap, Study parent = null, boolean createAllEntities = true ) {
		if( !mcmap )
			return;

		// Check whether the rows should be imported in one or more entities
		def entities
		if( createAllEntities ) {
			entities = templates.entrySet().value.findAll { it }.entity;
		} else {
			entities = mcmap.findAll{ !it.dontimport }.entityclass.unique();
		}

		def sheet = wb.getSheetAt(sheetindex)
		def table = []
		def failedcells = [] // list of cells that have failed to import
		// First check for each record whether an entity in the database should be updated,
		// or a new entity should be added. This is done before any new object is created, since
		// searching after new objects have been created (but not yet saved) will result in
		// 	org.hibernate.AssertionFailure: collection [...] was not processed by flush()
		// errors
		def existingEntities = [:]
		for( int i = rowindex; i <= sheet.getLastRowNum(); i++ ) {
			existingEntities[i] = findExistingEntities( entities, sheet.getRow(i), mcmap, parent );
		}

		// walk through all rows and fill the table with records
		for( int i = rowindex; i <= sheet.getLastRowNum(); i++ ) {
			def row = sheet.getRow(i);
			
			if( row && !rowIsEmpty( row ) ) {
				// Create an entity record based on a row read from Excel and store the cells which failed to be mapped
				def (record, failed) = importOrUpdateRecord( templates, entities, row, mcmap, parent, table, existingEntities[i] );
	
				// Setup the relationships between the imported entities
				relateEntities( record );
	
				// Add record with entities and its values to the table
				table.add(record)
	
				// If failed cells have been found, add them to the failed cells list
				if (failed?.importcells?.size() > 0) failedcells.add(failed)
			}
		}

		return [ "table": table, "failedCells": failedcells ]
	}
	
	/**
	 * Checks whether an excel row is empty
	 * @param row	Row from the excel sheet
	 * @return		True if all cells in this row are empty or the given row is null. False otherwise
	 */
	def rowIsEmpty( Row excelRow ) {
		if( !excelRow )
			return true;
		
		def df = new DataFormatter();
		for( int i = excelRow.getFirstCellNum(); i < excelRow.getLastCellNum(); i++ ) {
			Cell cell = excelRow.getCell( i );
			
			try {
				def value = df.formatCellValue(cell)
				if( value )
					return false
			} catch (NumberFormatException nfe) {
				// If the number can't be formatted, the row isn't empty
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Checks whether entities in the given row already exist in the database
	 * they are updated.
	 *
	 * @param	entities	Entities that have to be imported for this row
	 * @param	excelRow	Excel row to import into this record
	 * @param	mcmap		Hashmap of mappingcolumns, with the first entry in the hashmap containing information about the first column, etc.
	 * @return	Map			Map with entities that have been found for this row. The key for the entities is the entity name (e.g.: [Sample: null, Subject: <subject object>]
	 */
	def findExistingEntities(def entities, Row excelRow, mcmap, parent ) {
		DataFormatter df = new DataFormatter();

		// Find entities based on sample identifier
		def sample = findEntityByRow( dbnp.studycapturing.Sample, excelRow, mcmap, parent, [], df );
		return retrieveEntitiesBySample( sample );
	}

	/**
	 * Imports a records from the excelsheet into the database. If the entities are already in the database
	 * they are updated.
	 * 
	 * This method is capable of importing Subject, Samples, SamplingEvents and Events
	 * 
	 * @param	templates	Map of templates, identified by their entity as a key. For example: [ Sample: Template y ]
	 * @param	entities	Entities that have to be imported for this row
	 * @param	excelRow	Excel row to import into this record
	 * @param	mcmap		Hashmap of mappingcolumns, with the first entry in the hashmap containing information about the first column, etc.
	 * @param	parent		Study to import all data into. Is used for determining which sample/event/subject/assay to update
	 * @param	importedRows	Rows that have been imported before this row. These rows might contain the same entities as are
	 * 							imported in this row. These entities should be used again, to avoid importing duplicates.
	 * @return	List		List with two entries:
	 * 			0			List with ImportRecords, one for each row in the excelsheet
	 * 			1			List with ImportCell objects, mentioning the cells that could not be correctly imported
	 * 						(because the value in the excelsheet can't be entered into the template field)
	 */
	def importOrUpdateRecord(def templates, def entities, Row excelRow, mcmap, Study parent = null, List importedRows, Map existingEntities ) {
		DataFormatter df = new DataFormatter();
		def record = [] // list of entities and the read values
		def failed = new ImportRecord() // map with entity identifier and failed mappingcolumn

		// Check whether this record mentions a sample that has been imported before. In that case,
		// we update that record, in order to prevent importing the same sample multiple times
		def importedEntities = [];
		if( importedRows )
			importedEntities = importedRows.flatten().findAll { it.class == dbnp.studycapturing.Sample }.unique();

		def importedSample = findEntityInImportedEntities( dbnp.studycapturing.Sample, excelRow, mcmap, importedEntities, df )
		def imported = retrieveEntitiesBySample( importedSample );
		
		for( entity in entities ) {
			// Check whether this entity should be added or updated
			// The entity is updated is an entity with the same 'identifier' (field
			// specified to be the identifying field) is found in the database
			def entityName = entity.name[ entity.name.lastIndexOf( '.' ) + 1..-1];
			def template = templates[ entityName ];

			// If no template is specified for this entity, continue with the next
			if( !template )
				continue;

			// Check whether the object exists in the list of already imported entities
			def entityObject = imported[ entityName ]

			// If it doesn't, search for the entity in the database
			if( !entityObject && existingEntities )
				entityObject = existingEntities[ entityName ];

			// Otherwise, create a new object
			if( !entityObject )
				entityObject = entity.newInstance();

			// Update the template
			entityObject.template = template;

			// Go through the Excel row cell by cell
			for( int i = excelRow.getFirstCellNum(); i < excelRow.getLastCellNum(); i++ ) {
				Cell cell = excelRow.getCell( i );
				
				// get the MappingColumn information of the current cell
				def mc = mcmap[cell.getColumnIndex()]
				def value

				// Check if column must be imported
				if (mc != null && !mc.dontimport && mc.entityclass == entity) {
					try {
						if( cell.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell) ) {
							// The format for date template fields is dd/mm/yyyy
							def date = cell.getDateCellValue();
							value = date.format( "dd/MM/yyyy" )
						} else {
							value = formatValue(df.formatCellValue(cell), mc.templatefieldtype)
						}
					} catch (NumberFormatException nfe) {
						value = ""
					}

					try {
						entityObject.setFieldValue(mc.property, value)
					} catch (Exception iae) {
						log.error ".import wizard error could not set property `" + mc.property + "` to value `" + value + "`"

						// store the mapping column and value which failed
						def identifier = entityName.toLowerCase() + "_" + entityObject.getIdentifier() + "_" + mc.property

						def mcInstance = new MappingColumn()
						mcInstance.properties = mc.properties
						failed.addToImportcells(new ImportCell(mappingcolumn: mcInstance, value: value, entityidentifier: identifier))
					}
				} // end if
			} // end for

			// If a Study is entered, use it as a 'parent' for other entities
			if( entity == Study )
				parent = entityObject;

			record << entityObject;
		}

		// a failed column means that using the entity.setFieldValue() threw an exception
		return [record, failed]
	}

	/**
	 * Looks into the database to find an object of the given entity that should be updated, given the excel row.
	 * This is done by looking at the 'preferredIdentifier' field of the object. If it exists in the row, and the
	 * value is already in the database for that field, an existing object is returned. Otherwise, null is returned
	 * 
	 * @param	entity		Entity to search
	 * @param	excelRow	Excelrow to search for
	 * @param	mcmap		Map with MappingColumns
	 * @param	parent		Parent study for the entity (if applicable). The returned entity will also have this parent
	 * @param	importedRows	List of entities that have been imported before. The function will first look through this list to find
	 * 							a matching entity.
	 * @return	An entity that has the same identifier as entered in the excelRow. The entity is first sought in the importedRows. If it
	 * 			is not found there, the database is queried. If no entity is found at all, null is returned.
	 */
	def findEntityByRow( Class entity, Row excelRow, def mcmap, Study parent = null, List importedEntities = [], DataFormatter df = null ) {
		if( !excelRow )
			return
		
		if( df == null )
			df = new DataFormatter();

		def identifierField = givePreferredIdentifier( entity );

		if( identifierField ) {
			// Check whether the identifierField is chosen in the column matching
			def identifierColumn = mcmap.find { it.entityclass == entity && it.property == identifierField.name };

			// If it is, find the identifier and look it up in the database
			if( identifierColumn ) {
				def identifierCell = excelRow.getCell( identifierColumn.index );
				def identifier;
				try {
					identifier = formatValue(df.formatCellValue(identifierCell), identifierColumn.templatefieldtype)
				} catch (NumberFormatException nfe) {
					identifier = null
				}

				// Search for an existing object with the same identifier.
				if( identifier ) {
					// First search the already imported rows
					if( importedEntities ) {
						def imported = importedEntities.find { it.getFieldValue( identifierField.name ) == identifier };
						if( imported )
							return imported;
					}

					def c = entity.createCriteria();

					// If the entity has a field 'parent', the search should be limited to
					// objects with the same parent. The method entity.hasProperty( "parent" ) doesn't
					// work, since the java.lang.Class entity doesn't know of the parent property.
					if( entity.belongsTo?.containsKey( "parent" ) ) {
						// If the entity requires a parent, but none is given, no
						// results are given from the database. This prevents the user
						// of changing data in another study
						if( parent && parent.id ) {
							return c.get {
								eq( identifierField.name, identifier )
								eq( "parent", parent )
							}
						}
					} else  {
						return c.get {
							eq( identifierField.name, identifier )
						}
					}
				}
			}
		}

		// No object is found
		return null;
	}

	/**
	 * Looks into the list of already imported entities to find an object of the given entity that should be 
	 * updated, given the excel row. This is done by looking at the 'preferredIdentifier' field of the object. 
	 * If it exists in the row, and the list of imported entities contains an object with the same
	 * identifier, the existing object is returned. Otherwise, null is returned
	 *
	 * @param	entity		Entity to search
	 * @param	excelRow	Excelrow to search for
	 * @param	mcmap		Map with MappingColumns
	 * @param	importedRows	List of entities that have been imported before. The function will first look through this list to find
	 * 							a matching entity.
	 * @return	An entity that has the same identifier as entered in the excelRow. The entity is first sought in the importedRows. If it
	 * 			is not found there, the database is queried. If no entity is found at all, null is returned.
	 */
	def findEntityInImportedEntities( Class entity, Row excelRow, def mcmap, List importedEntities = [], DataFormatter df = null ) {
		if( df == null )
			df = new DataFormatter();

		def allFields = entity.giveDomainFields();
		def identifierField = allFields.find { it.preferredIdentifier }

		if( identifierField ) {
			// Check whether the identifierField is chosen in the column matching
			def identifierColumn = mcmap.find { it.entityclass == entity && it.property == identifierField.name };

			// If it is, find the identifier and look it up in the database
			if( identifierColumn ) {
				def identifierCell = excelRow.getCell( identifierColumn.index );
				def identifier;
				try {
					identifier = formatValue(df.formatCellValue(identifierCell), identifierColumn.templatefieldtype)
				} catch (NumberFormatException nfe) {
					identifier = null
				}

				// Search for an existing object with the same identifier.
				if( identifier ) {
					// First search the already imported rows
					if( importedEntities ) {
						def imported = importedEntities.find {
							def fieldValue = it.getFieldValue( identifierField.name )

							if( fieldValue instanceof String )
								return fieldValue.toLowerCase() == identifier.toLowerCase();
							else
								return fieldValue == identifier
						};
						if( imported )
							return imported;
					}
				}
			}
		}

		// No object is found
		return null;
	}


	/**
	 * Creates relation between multiple entities that have been imported. The entities are
	 * all created from one row in the excel sheet.
	 */
	def relateEntities( List entities) {
		def study = entities.find { it instanceof Study }
		def subject = entities.find { it instanceof Subject }
		def sample = entities.find { it instanceof Sample }
		def event = entities.find { it instanceof Event }
		def samplingEvent = entities.find { it instanceof SamplingEvent }
		def assay = entities.find { it instanceof Assay }

		// A study object is found in the entity list
		if( study ) {
			if( subject ) {
				subject.parent = study;
				study.addToSubjects( subject );
			}
			if( sample ) {
				sample.parent = study
				study.addToSamples( sample );
			}
			if( event ) {
				event.parent = study
				study.addToEvents( event );
			}
			if( samplingEvent ) {
				samplingEvent.parent = study
				study.addToSamplingEvents( samplingEvent );
			}
			if( assay ) {
				assay.parent = study;
				study.addToAssays( assay );
			}
		}

		if( sample ) {
			if( subject ) sample.parentSubject = subject
			if( samplingEvent ) sample.parentEvent = samplingEvent;
			if( event ) {
				def evGroup = new EventGroup();
				evGroup.addToEvents( event );
				if( subject ) evGroup.addToSubjects( subject );
				if( samplingEvent ) evGroup.addToSamplingEvents( samplingEvent );

				sample.parentEventGroup = evGroup;
			}

			if( assay ) assay.addToSamples( sample );
		}
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
		def template = Template.get(template_id)
		def table = []
		def failedcells = [] // list of records
		// walk through all rows and fill the table with records
		(rowindex..sheet.getLastRowNum()).each { i ->
			// Create an entity record based on a row read from Excel and store the cells which failed to be mapped
			def (record, failed) = createRecord(template, sheet.getRow(i), mcmap)

			// Add record with entity and its values to the table
			table.add(record)

			// If failed cells have been found, add them to the failed cells list
			if (failed?.importcells?.size() > 0) failedcells.add(failed)
		}

		return [table, failedcells]
	}

	/**
	 * Removes a cell from the failedCells list, based on the entity and field. If the entity and field didn't fail before
	 * the method doesn't do anything.
	 * 
	 * @param failedcell 	list of cells that have failed previously
	 * @param entity 		entity to remove from the failedcells list
	 * @param field			field to remove the failed cell for. If no field is given, all cells for this entity will be removed
	 * @return List			Updated list of cells that have failed
	 */
	def removeFailedCell(failedcells, entity, field = null ) {
		if( !entity )
			return failedcells;

		def filterClosure
		if( field ) {
			def entityIdField = "entity_" + entity.getIdentifier() + "_" + field.name.toLowerCase()
			filterClosure = { cell -> cell.entityidentifier != entityIdField }
		} else {
			def entityIdField = "entity_" + entity.getIdentifier() + "_"
			filterClosure = { cell -> !cell.entityidentifier.startsWith( entityIdField ) }
		}

		failedcells.each { record ->
			record.importcells = record.importcells.findAll( filterClosure )
		}

		return failedcells;
	}

	/**
	 * Returns the name of an input field as it is used for a specific entity in HTML.
	 *
	 * @param entity 		entity to retrieve the field name for
	 * @param field			field to retrieve the field name for
	 * @return String		Name of the HTML field for the given entity and field. Can also be used in the map
	 * 						of request parameters
	 */
	def getFieldNameInTableEditor(entity, field) {
		def entityName = entity?.class.name[ entity?.class.name.lastIndexOf(".") + 1..-1]

		if( field instanceof TemplateField )
			field = field.escapedName();

		return entityName.toLowerCase() + "_" + entity.getIdentifier() + "_" + field.toLowerCase()
	}

	/**
	 * Retrieves a mapping column from a list based on the given fieldname
	 * @param mappingColumns		List of mapping columns
	 * @param fieldName				Field name to find
	 * @return						Mapping column if a column is found, null otherwise
	 */
	def findMappingColumn( mappingColumns, String fieldName ) {
		return mappingColumns.find { it.property == fieldName.toLowerCase() }
	}

	/** Method to put failed cells back into the datamatrix. Failed cells are cell values
	 * which could not be stored in an entity (e.g. Humu Supiuns in an ontology field).
	 * Empty corrections should not be stored
	 *
	 * @param datamatrix two dimensional array containing entities and possibly also failed cells
	 * @param failedcells list with maps of failed cells in [mappingcolumn, cell] format
	 * @param correctedcells map of corrected cells in [cellhashcode, value] format
	 * */
	def saveCorrectedCells(datamatrix, failedcells, correctedcells) {

		// Loop through all failed cells (stored as
		failedcells.each { record ->
			record.value.importcells.each { cell ->

				// Get the corrected value
				def correctedvalue = correctedcells.find { it.key.toInteger() == cell.getIdentifier()}.value

				// Find the record in the table which the mappingcolumn belongs to
				def tablerecord = datamatrix.find { it.hashCode() == record.key }

				// Loop through all entities in the record and correct them if necessary
				tablerecord.each { rec ->
					rec.each { entity ->
						try {
							// Update the entity field
							entity.setFieldValue(cell.mappingcolumn.property, correctedvalue)
							//log.info "Adjusted " + cell.mappingcolumn.property + " to " + correctedvalue
						}
						catch (Exception e) {
							//log.info "Could not map corrected ontology: " + cell.mappingcolumn.property + " to " + correctedvalue
						}
					}
				} // end of table record
			} // end of cell record
		} // end of failedlist
	}

	/**
	 * Method to store a matrix containing the entities in a record like structure. Every row in the table
	 * contains one or more entity objects (which contain fields with values). So actually a row represents
	 * a record with fields from one or more different entities.
	 *
	 * @param study entity Study
	 * @param datamatrix two dimensional array containing entities with values read from Excel file
	 */
	static saveDatamatrix(Study study, importerEntityType, datamatrix, authenticationService, log) {
		def validatedSuccesfully = 0
		def entitystored = null

		// Study passed? Sync data
		if (study != null && importerEntityType != 'Study') study.refresh()

		// go through the data matrix, read every record and validate the entity and try to persist it
		datamatrix.each { record ->
			record.each { entity ->
				switch (entity.getClass()) {
					case Study: log.info ".importer wizard, persisting Study `" + entity + "`: "
						entity.owner = authenticationService.getLoggedInUser()

						if (entity.validate()) {
							if (!entity.save(flush:true)) {
								log.error ".importer wizard, study could not be saved: " + entity
								throw new Exception('.importer wizard, study could not be saved: ' + entity)
							}
						} else {
							log.error ".importer wizard, study could not be validated: " + entity
							throw new Exception('.importer wizard, study could not be validated: ' + entity)
						}

						break
					case Subject: log.info ".importer wizard, persisting Subject `" + entity + "`: "

					// is the current entity not already in the database?
					//entitystored = isEntityStored(entity)

					// this entity is new, so add it to the study
					//if (entitystored==null)

						study.addToSubjects(entity)

						break
					case Event: log.info ".importer wizard, persisting Event `" + entity + "`: "
						study.addToEvents(entity)
						break
					case Sample: log.info ".importer wizard, persisting Sample `" + entity + "`: "

					// is this sample validatable (sample name unique for example?)
						study.addToSamples(entity)

						break
					case SamplingEvent: log.info ".importer wizard, persisting SamplingEvent `" + entity + "`: "
						study.addToSamplingEvents(entity)
						break
					default: log.info ".importer wizard, skipping persisting of `" + entity.getclass() + "`"
						break
				} // end switch
			} // end record
		} // end datamatrix

		// validate study
		if (importerEntityType != 'Study') {
			if (study.validate()) {
				if (!study.save(flush: true)) {
					//this.appendErrors(flow.study, flash.wizardErrors)
					throw new Exception('.importer wizard [saveDatamatrix] error while saving study')
				}
			} else {
				throw new Exception('.importer wizard [saveDatamatrix] study does not validate')
			}
		}

		//persistEntity(study)

		//return [validatedSuccesfully, updatedentities, failedtopersist]
		//return [0,0,0]
		return true
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
			case Study: return Study.findByCode(entity.code)
				break
			case Subject: return Subject.findByParentAndName(entity.parent, entity.name)
				break
			case Event: break
			case Sample:
				break
			case SamplingEvent: break
			default:  // unknown entity
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
			case Study: break
			case Subject: entitystored.properties = entity.properties
				entitystored.save()
				break
			case Event: break
			case Sample: break
			case SamplingEvent: break
			default:  // unknown entity
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
		/*log.info ".import wizard persisting ${entity}"
		 try {            
		 entity.save(flush: true)
		 return true
		 } catch (Exception e) {
		 def session = sessionFactory.currentSession
		 session.setFlushMode(org.hibernate.FlushMode.MANUAL)
		 log.error ".import wizard, failed to save entity:\n" + org.apache.commons.lang.exception.ExceptionUtils.getRootCauseMessage(e)
		 }
		 return true*/
		//println "persistEntity"
	}

	/**
	 * This method creates a record (array) containing entities with values
	 *
	 * @param template_id template identifier
	 * @param excelrow POI based Excel row containing the cells
	 * @param mcmap map containing MappingColumn objects
	 * @return list of entities and list of failed cells
	 */
	def createRecord(template, Row excelrow, mcmap) {
		def df = new DataFormatter()
		def tft = TemplateFieldType
		def record = [] // list of entities and the read values
		def failed = new ImportRecord() // map with entity identifier and failed mappingcolumn

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
			if (mc != null) if (!mc.dontimport) {
				try {
					value = formatValue(df.formatCellValue(cell), mc.templatefieldtype)
				} catch (NumberFormatException nfe) {
					value = ""
				}

				try {
					// which entity does the current cell (field) belong to?
					switch (mc.entityclass) {
						case Study: // does the entity already exist in the record? If not make it so.
							(record.any {it.getClass() == mc.entityclass}) ? 0 : record.add(study)
							study.setFieldValue(mc.property, value)
							break
						case Subject: (record.any {it.getClass() == mc.entityclass}) ? 0 : record.add(subject)
							subject.setFieldValue(mc.property, value)
							break
						case SamplingEvent: (record.any {it.getClass() == mc.entityclass}) ? 0 : record.add(samplingEvent)
							samplingEvent.setFieldValue(mc.property, value)
							break
						case Event: (record.any {it.getClass() == mc.entityclass}) ? 0 : record.add(event)
							event.setFieldValue(mc.property, value)
							break
						case Sample: (record.any {it.getClass() == mc.entityclass}) ? 0 : record.add(sample)
							sample.setFieldValue(mc.property, value)
							break
						case Object:   // don't import
							break
					} // end switch
				} catch (Exception iae) {
					log.error ".import wizard error could not set property `" + mc.property + "` to value `" + value + "`"
					// store the mapping column and value which failed
					def identifier
					def fieldName = mc.property?.toLowerCase()
					
					switch (mc.entityclass) {
						case Study: identifier = "entity_" + study.getIdentifier() + "_" + fieldName
							break
						case Subject: identifier = "entity_" + subject.getIdentifier() + "_" + fieldName
							break
						case SamplingEvent: identifier = "entity_" + samplingEvent.getIdentifier() + "_" + fieldName
							break
						case Event: identifier = "entity_" + event.getIdentifier() + "_" + fieldName
							break
						case Sample: identifier = "entity_" + sample.getIdentifier() + "_" + fieldName
							break
						case Object:   // don't import
							break
					}

					def mcInstance = new MappingColumn()
					mcInstance.properties = mc.properties
					failed.addToImportcells(new ImportCell(mappingcolumn: mcInstance, value: value, entityidentifier: identifier))
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
			case TemplateFieldType.STRING: return value.trim()
			case TemplateFieldType.TEXT: return value.trim()
			case TemplateFieldType.LONG: return (long) Double.valueOf(value)
			//case TemplateFieldType.FLOAT	    :   return Float.valueOf(value.replace(",","."));
			case TemplateFieldType.DOUBLE: return Double.valueOf(value.replace(",", "."));
			case TemplateFieldType.STRINGLIST: return value.trim()
			case TemplateFieldType.ONTOLOGYTERM: return value.trim()
			case TemplateFieldType.DATE: return value
			default: return value
		}
	}

	/**
	 * Returns the preferred identifier field for a given entity or 
	 * null if no preferred identifier is given
	 * @param entity	TemplateEntity class
	 * @return	The preferred identifier field or NULL if no preferred identifier is given
	 */
	public TemplateField givePreferredIdentifier( Class entity ) {
		def allFields = entity.giveDomainFields();
		return allFields.find { it.preferredIdentifier }
	}

	// classes for fuzzy string matching
	// <FUZZY MATCHING>

	static def similarity(l_seq, r_seq, degree = 2) {
		def l_histo = countNgramFrequency(l_seq, degree)
		def r_histo = countNgramFrequency(r_seq, degree)

		dotProduct(l_histo, r_histo) /
				Math.sqrt(dotProduct(l_histo, l_histo) *
				dotProduct(r_histo, r_histo))
	}

	static def countNgramFrequency(sequence, degree) {
		def histo = [:]
		def items = sequence.size()

		for (int i = 0; i + degree <= items; i++) {
			def gram = sequence[i..<(i + degree)]
			histo[gram] = 1 + histo.get(gram, 0)
		}
		histo
	}

	static def dotProduct(l_histo, r_histo) {
		def sum = 0
		l_histo.each { key, value ->
			sum = sum + l_histo[key] * r_histo.get(key, 0)
		}
		sum
	}

	static def stringSimilarity(l_str, r_str, degree = 2) {

		similarity(l_str.toString().toLowerCase().toCharArray(),
				r_str.toString().toLowerCase().toCharArray(),
				degree)
	}

	static def mostSimilar(pattern, candidates, threshold = 0) {
		def topScore = 0
		def bestFit = null

		candidates.each { candidate ->
			def score = stringSimilarity(pattern, candidate)
			if (score > topScore) {
				topScore = score
				bestFit = candidate
			}
		}

		if (topScore < threshold)
			bestFit = null

		bestFit
	}
	// </FUZZY MATCHING>

}
