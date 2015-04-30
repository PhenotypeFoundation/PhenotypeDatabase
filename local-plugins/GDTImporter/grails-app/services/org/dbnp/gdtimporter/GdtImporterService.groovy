/**
 *  GDTImporter, a plugin for importing data into Grails Domain Templates
 *  Copyright (C) 2011 Tjeerd Abma, Siemen Sikkema
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  $Author$
 *  $Rev$
 *  $Date$
 */

package org.dbnp.gdtimporter

import org.dbnp.gdt.*
import org.apache.poi.ss.usermodel.*
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.orm.hibernate.validation.UniqueConstraint
import org.codehaus.groovy.grails.validation.NullableConstraint
import grails.util.Holders
import java.text.SimpleDateFormat
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator

class GdtImporterService {
    def authenticationService
    def gdtService
    static transactional = true

    /**
	 * @param is input stream representing the (workbook) resource
	 * @return high level representation of the workbook
	 */
	Workbook getWorkbook(InputStream is) {
        def workbook = null

		try {
            workbook = WorkbookFactory.create(is)
        } catch (Exception e) {
            log.error ".import wizard could not instantiate workbook, exception: " + e
        }

        workbook
	}

    /**
     * This method reads the header from the workbook.
     *
	 * @param datamatrix two dimensional datamatrix containing raw read data from Excel
     * @param headerRowIndex row where the header starts
     * @param entityInstance type of entity we are reading
	 * @return header representation as a GdtMappingColumn hashmap
	 */
    def getHeader(String[][] datamatrix, int headerRowIndex, entityInstance = null) {
        def header = []

		// Loop through all columns from the first row in the datamatrix and try to
        // determine the type of values stored (integer, string, float)
        datamatrix[0].length.times { columnIndex ->

            // Default TemplateFieldType is a String
            def fieldType = TemplateFieldType.STRING

            // Create the GdtMappingColumn object for the current column and store it in the header HashMap
            header[columnIndex] = new GdtMappingColumn(name: datamatrix[headerRowIndex][columnIndex],
							templatefieldtype: fieldType,
							index: columnIndex,
							entityclass: entityInstance.class,
							property: "")
		}

        header

//        def i = 0
//        datamatrix[headerRowIndex].collect{ headerName ->
//
//            new GdtMappingColumn(
//                    name:               headerName,
//					templatefieldtype:  TemplateField.STRING,
//					index:              i++,
//					entityclass:        entityInstance.class,
//					property:           "")
//        }

    }

    /**
	 * This method is meant to return a matrix of the rows and columns
	 * used in the preview.
	 *
	 * @param workbook Workbook class object
	 * @param sheetIndex sheet index used
     * @param dataMatrixStartRow row to start reading from
	 * @param count amount of rows of data to read
	 * @return two dimensional array (dataMatrix) of cell values
	 */
    String[][] getDataMatrix(Workbook workbook, int sheetIndex, int count = 0) {
        def sheet = workbook.getSheetAt(sheetIndex)
        def df = new DataFormatter()
		def dataMatrix = []
        def formulaEvaluator = null

             // Is this an XLS (old fashioned Excel file)?
             try {
                 formulaEvaluator = new HSSFFormulaEvaluator(sheet, workbook);
             } catch (Exception e) {
                 log.error ".import wizard could not create Excel (XLS) formula evaluator, skipping to Excel XML (XLSX)"
             }

             // Or is this an XLSX (modern style Excel file)?
             if (formulaEvaluator==null) try {
                 formulaEvaluator = new XSSFFormulaEvaluator(workbook);
             } catch (Exception e) {
                 log.error ".import wizard could not create Excel XML (XLSX) formula evaluator either, unknown Excel formula format?"
             }

        count = count ? Math.min(sheet.lastRowNum, count) : sheet.lastRowNum

        // Determine amount of columns
        def columnCount = sheet.getRow(sheet.getFirstRowNum())?.getLastCellNum()

		// Walk through all rows
		(sheet.firstRowNum..count).each { rowIndex ->

            def dataMatrixRow = []

            // Get the current row
            def excelRow = sheet.getRow(rowIndex)

            // Excel contains some data?
            if (excelRow)
                columnCount.times { columnIndex ->

                    // Read the cell, even is it a blank
                    def cell = excelRow.getCell(columnIndex, Row.CREATE_NULL_AS_BLANK)
                    // Set the cell type to string, this prevents any kind of formatting
                    def dateValue

                    // It is a numeric cell?
                    if (cell.cellType == Cell.CELL_TYPE_NUMERIC)
                        // It isn't a date cell?
                        if (!DateUtil.isCellDateFormatted(cell))
                            cell.setCellType(Cell.CELL_TYPE_STRING)

                    switch (cell.cellType) {
                        case Cell.CELL_TYPE_STRING:     dataMatrixRow.add( cell.stringCellValue )
                                                        break
                        case Cell.CELL_TYPE_NUMERIC:    try {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                                            dateValue = sdf.format(cell.getDateCellValue());
                                                            dataMatrixRow.add( dateValue )
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        break
                        case Cell.CELL_TYPE_FORMULA:    (cell != null) ? dataMatrixRow.add(formulaEvaluator.evaluateInCell(cell)) :
                                                            dataMatrixRow.add('')
                                                        break
                        default:                        dataMatrixRow.add( '' )

                    }
                }

            if ( dataMatrixRow.any{it} ) // is at least 1 of the cells non empty?
			    dataMatrix.add(dataMatrixRow)
		}

        dataMatrix
    }

    /**
	 * Method to read data from a Workbook class object and import entities
     * into a list
	 *
     * @param theEntity entity we are trying to read (Subject, Study et cetera)
	 * @param theTemplate Template to use
	 * @param dataMatrix Two-dimensional string array containing excel data
	 * @param mcmap linked hashmap (preserved order) of GdtMappingColumns
	 * @param dateFormat date formatter used when parsing/reading dates (yy/MM/dddd et cetera)
	 * @return list containing entities
	 *
	 * @see org.dbnp.gdtimporter.GdtMappingColumn
	 */
	def getDataMatrixAsEntityList(theEntity, theTemplate, dataMatrix, mcmap, dateFormat) {
		def entityList = []
		def errorList = []

		// Walk through all rows and fill the table with entities
		dataMatrix.each { row ->

            // Create an entity record based on a row read from Excel and store the cells which failed to be mapped
			def (entity, error) = createEntity(theEntity, theTemplate, row, mcmap, dateFormat)

            // Add entity to the table if it is not empty
            if (!isEntityEmpty(entity))
                entityList.add(entity)

			// If failed cells have been found, add them to the fieldError list
            // Error contains the entity+identifier+property and the original (failed) value
			if (error) errorList.add(error)
		}

		[entityList, errorList]
	}

    /**
     * @param entityList the list of entities
     * @param parentEntity the parent entity (if any) to which the entities (will) belong
     * @param childEntityParentName parent name the child entity belongs to
     * @return true if
     */
    def replaceExistingEntitiesHasEqualTemplate(entityList, parentEntity, childEntityParentName) {
        if (entityList == null) return false

        def preferredIdentifierField = entityList[0].giveDomainFields().find { it.preferredIdentifier }
            if (preferredIdentifierField) {

                def preferredIdentifierValue = entity[preferredIdentifierField.name]

                def c = entity.createCriteria()

                // find an entity with the same parent (in case of a non-parent
                // entity) and preferred identifier value
                def existingEntity = c.get {
                    eq( preferredIdentifierField.name, preferredIdentifierValue )
                    if (parentEntity) eq( childEntityParentName, parentEntity )
                }
            }

        existingEntity.template == entityList[0].template
    }

    /**
     * Replaces entities in the list with existing ones if the preferred
     * identifier matches otherwise leaves the entities untouched.
     *
     * @param entityList the list of entities
     * @param parentEntity the parent entity (if any) to which the entities
     *  (will) belong
     * @return [updated entity list, the number of updated entities, the number
     *          of template changes]
     */
    def replaceEntitiesByExistingOnesIfNeeded(entityList, parentEntity, childEntityParentName) {

        def numberOfUpdatedEntities     = 0
        def numberOfChangedTemplates    = 0

        [entityList.collect { entity ->

            def preferredIdentifierField = entity.giveDomainFields().find { it.preferredIdentifier }

            if (preferredIdentifierField) {

                def preferredIdentifierValue = entity[preferredIdentifierField.name]

                def c = entity.createCriteria()

                // find an entity with the same parent (in case of a non-parent
                // entity) and preferred identifier value
                def existingEntity = c.get {
                    eq( preferredIdentifierField.name, preferredIdentifierValue )
                    if (parentEntity) eq( childEntityParentName, parentEntity )
                }

                if (existingEntity) {

                    numberOfUpdatedEntities++

                    // Set the existing entity's template to the user selected template.
                    // they are the same for all entities so we'll get the template for
                    // the first entity
                    if (existingEntity.template != entity.template) {
                        numberOfChangedTemplates++
                        existingEntity.setTemplate(entity.template)
                    }

                    // overwrite all field values of the existing entity
                    entity.giveFields().each { field ->
                        try {
                            existingEntity.setFieldValue(field.name, entity.getFieldValue(field.name))
                        } catch (Exception e) {
                            log.error "Can not set field `" + field.name + " to `" + entity.getFieldValue(field.name) + "`"
                        }
                    }

                    existingEntity
                } else
                    entity
            } else // not an identifier field, keep the original entity too
                entity
        }, numberOfUpdatedEntities, numberOfChangedTemplates]
    }

    /**
     * Sets field values for a list of entities based on user input via params
     * variable.
     *
     * @param entityList the list of entities to update
     * @param params the params list from the view
     */
    def setEntityListFieldValuesFromParams(entityList, params) {

        def failedFields = []

        entityList.each { entity ->

            entity.giveFields().each { field ->

                def cellName = "entity_${entity.identifier}_${field.escapedName()}"

                def value = params[cellName]

                if (value) {

                    try {

                        entity.setFieldValue( field.name, value, true )

                    } catch(Exception e) {

                        failedFields += [error: 'Empty or non-valid value', identifier: entity.getIdentifier(), property: field.name, entity: cellName, originalValue: value]

                    }
                }
            }
        }

        [entityList, failedFields]

    }

    /**
	 * Checks whether unique constraints are violated within the entityList.
     * This type of violation can occur in two ways.
     *
     * 1. Entities with same property values already exist
     * 2. Within the entity list there are duplicate values
     *
     * Violations of type 1 will throw an exception but those of type 2 don't
     * (always?). This simply collects all values of the old and new entities
     * within a parent entity and finds duplicates where they are not allowed.
     *
     * see: http://grails.org/doc/latest/ref/Constraints/unique.html
     *
	 * @parentEntity the parent entity
     * @entityList the list of entities to add to parentEntity
     * @childEntityParentName a child refers to a parent via a name (belongsTo)
     *
     * @return empty list on success, list of errors on failure
	 */
	def detectUniqueConstraintViolations(entityList, parentEntity, childEntityParentName) {

        def firstEntity     = entityList[0]

        def preferredIdentifierName = firstEntity.giveDomainFields().find { it.preferredIdentifier }?.name

        def failedFields    = []

        def domainClass     = Holders.grailsApplication.getDomainClass(firstEntity.class.name)
        def domainClassReferenceInstance = domainClass.referenceInstance

        // we need all children of parentEntity of same type as the added
        // entities (including ones to be added)
        def childEntities = domainClassReferenceInstance.findAllWhere("$childEntityParentName": parentEntity) + entityList

        // this closure seeks duplicate values of the property with the given
        // name within the childEntities (old and new ones).
        def checkForDuplicates = { propertyName, isNullable ->

            // skip checking existing entities (only new ones) if we're
            // dealing with a preferred identifier. This enables updating.
            def entityProperties = propertyName == preferredIdentifierName ?
                entityList*."$propertyName" : childEntities*."$propertyName"

            def uniques     = [] as Set
            def duplicates  = [] as Set

            // this approach separates the unique from the duplicate entries
            entityProperties.each {
                if (!uniques.add(it)) {

                    // only add to duplicates if null is not allowed and value is null
                    if (!(it == null && isNullable))
                        duplicates.add(it)
                }
            }

            if (duplicates) {

                // Collect all entities with a duplicate value of the unique
                // property. Add corresponding entries to 'failedFields'.
                failedFields += entityList.findAll { it."$propertyName" in duplicates }.collect { duplicate ->

                    [   identifier: duplicate.getIdentifier(),
                        entity :        "entity_${duplicate.identifier}_$propertyName",
                        property: propertyName,
                        originalValue : duplicate[propertyName] ]

                }
            }
        }

        // search through the constrained properties for a 'Unique' constraint
        domainClass.constrainedProperties.each { constrainedProperty ->

            def hasUniqueConstraint = constrainedProperty.value.appliedConstraints.any { appliedConstraint ->

                appliedConstraint instanceof UniqueConstraint

            }

            def isNullable = constrainedProperty.value.appliedConstraints.any { appliedConstraint ->

                appliedConstraint instanceof NullableConstraint && appliedConstraint.isNullable()

            }
            // did we find a 'Unique' constraint? check for duplicate entries
            if (hasUniqueConstraint) {

                checkForDuplicates(constrainedProperty.key, isNullable)
            }

        }

        failedFields
	}

    /**
     * Validates a list of entities. Ignores field errors for preferred ids for
     * parent entities. Ignores field errors for parent relations so they can be
     * set afterwards.
     * @param entityList
     * @param childEntityParentName parent name the child belongs to
     * @return a list of failed fields and a list of failed entities
     */
    def validateEntities(entityList, childEntityParentName) {

        def failedFields = []
        def failedEntities = []

        // collect fieldError not related to setting fields, e.g. non-nullable fields
        // that were null.
        entityList.each { entity ->

            if (!entity.validate()) failedEntities.add(entity)

            entity.errors.fieldErrors.each { fieldError ->

                def useError = true

                // if we encounter a parent entity (which has no parent)
                if (!entity.hasProperty( childEntityParentName )) {

                    // find the preferred identifier
                    def preferredIdentifierField = entity.giveDomainFields().find { it.preferredIdentifier }

                    // ignore this field error when it is the preferred identifier
                    useError = (fieldError.field.toString() != preferredIdentifierField.toString())
                }

                if (useError && fieldError.field != childEntityParentName ) { // ignore parent errors because we'll add the entities to their parent later
                    def entityIdentifier = "entity_${entity.identifier}_${fieldError.field.toLowerCase().replaceAll("([^a-z0-9])", "_")}"
                    if (!failedFields.find {
                            it.entity == entityIdentifier
                        }) failedFields += [error:fieldError, identifier:entity.getIdentifier(), property:fieldError.field.toLowerCase().replaceAll("([^a-z0-9])", "_"), entity: entityIdentifier, originalValue: fieldError.rejectedValue ?: '']
                }
            }
        }

        [failedFields, failedEntities]
    }

    /**
     * Attaches samples to subjects. The list of samples and subject names and
     * time points should be equally long and the order defines the relation.
     * New sampling events will be based on the time points and event groups
     * will be made for each sampling event.
     *
     * @param samples The samples that will be connected to the subject
     * @param subjectNames Names of the subjects that will be connected to the
     *  samples
     * @param timePoints Time points on which to base sampling events
     * @param sampleTemplate The sample template that will be referenced by the
     *  sampling events that will be created
     * @param parentEntity The parent entity to which the samples, sampling
     *  events and event groups will be added to
     * @return -
     */
    def attachSamplesToSubjects(samples, subjectNames, sampleTemplate, parentEntity) {

        // get a list of subject names with duplicates removed
        def uniqueSubjectNames = subjectNames.clone().unique()

        // get the referenced subjects from the parent entity
        def subjects = uniqueSubjectNames.collect{
            subjectName -> parentEntity.subjects.find{
                it.name == subjectName
            }
        }

        // add each sample to their corresponding subject
        samples.eachWithIndex { sample, idx ->
            def subject         = subjects.find{it.name == subjectNames[idx]}

            parentEntity.addToSamples(sample)
            sample.parentSubject    = subject
        }
    }

    /**
     * Removes duplicates from a list of entities and return the consolidated
     * list and a list of index numbers relating the old list to the new one.
     *
     * Example input: entity1, entity2, entity1, entity3
     * Corresponding output: [[entity1, entity2, entity3],[0,1,0,2]]
     *
     * @param events The events to remove duplicates from
     * @return the consolidated list and a list relating the old list to the new
     */
    def consolidateEntities(entities) {

        def getFieldValueSet = { entity ->

            entity.giveFields().collect { field -> entity.getFieldValue( field.name ) } as Set

        }

        def entityComparator = [ compare: { a, b ->

            getFieldValueSet( a ).equals(getFieldValueSet( b )) ? 0 : 1

        } ] as Comparator


        def consolidatedEntities = entities.clone().unique( entityComparator ) //entityComparator }

        def indexList = entities.collect { entity ->

            consolidatedEntities.findIndexOf {
                entityComparator.compare( it, entity ) == 0
            }
        }

        [ consolidatedEntities, indexList ]

    }

    /**
     * Attach events to subjects. Events and subject names should be lists of
     * equal size and the order defines their relations. Event group will be
     * made based on unique combinations of events for all subjects.
     *
     * @param events The events to add
     * @param subjectNames The names of the subjects to add the events to
     * @param parentEntity The parent entity to which events and event groups
     *  will be added to
     * @return -
     */
    def attachEventsToSubjects(events, eventReferences, subjectNames, parentEntity) {

        // get a list of subject names with duplicates removed
        def uniqueSubjectNames = subjectNames.clone().unique()

        // get the referenced subjects from the parent entity
        def subjects = uniqueSubjectNames.collect {
            subjectName -> parentEntity.subjects.find {
                it.name == subjectName
            }
        }

        def eventsInOriginalOrder = eventReferences.collect { events[it] }

        def i = 0

        // find all events belonging to each subject
        def subjectEvents = eventsInOriginalOrder.groupBy { subjectNames[i++] }

        // find all unique combinations of events
        def uniqueEventCombos = subjectEvents*.value.unique()

        // create event groups; add subjects + events
        uniqueEventCombos.each { eventCombo ->

            def eventGroupBaseName = eventCombo.collect { event ->
                "${ event.template.name.split(' ')*.capitalize().join() }_${ new RelTime(event.startTime) }"
            }.join('_')

            def eventGroupName = generateUniqueString(eventGroupBaseName, parentEntity.eventGroups*.name)

            // make sure all existing event groups have identifiers
            parentEntity.eventGroups*.identifier

            // we can't make an event group directly from the module but we can use addToEventGroups on the parent entity
            parentEntity.addToEventGroups( name: eventGroupName )

            // find the last inserted event group, which is the one with the highest identifier
            def eventGroup = parentEntity.eventGroups.sort { it.identifier }[-1]

            eventCombo.each { eventGroup.addToEvents it }

            // fetch subjects that have this combination of events
            def subjectNamesWithTheseEvents = subjectEvents.findAll { it.value == eventCombo }*.key
            def subjectsWithTheseEvents     = subjectNamesWithTheseEvents.collect { subjectName -> subjects.find { it.name == subjectName } }

            // add those subject to the group
            subjectsWithTheseEvents.each { eventGroup.addToSubjects it }

        }

        events.each { parentEntity.addToEvents( it ) }

    }

    /**
     * Generates a unique string based on 'baseString' by appending a number
     * between parentheses if necessary. The existing strings are scanned for
     * strings following the pattern "baseString ($i)" where i > 1. If
     * fillMissing is set, the lowest available number is used, otherwise the
     * largest + 1. If baseString does not exist but there are string following
     * the pattern, then the output also depends on fillMissing, with the result
     * being equal to baseString if fillMissing is true but with a number larger
     * than the existing ones if not.
     *
     * @param baseString Input string to make unique
     * @param existingStrings Strings which the output should not equal
     * @param fillMissing If true, fills gaps in the sequence
     * @return a string based on 'baseString' which does not exist in
     * 'existingStrings'
     */
    def generateUniqueString(baseString, existingStrings, fillMissing = false) {

        def baseStringExists = (baseString in existingStrings)

        // find all strings which follow the pattern: "$baseString ($number)", where number is an integer > 1
        def matchingStrings = existingStrings.findAll { it =~ /${baseString} \([2-9]{1}[0-9]*\)$/ }

        // grab all numbers from within the parentheses and sort; prepend '1' if baseStringExists
        def iterators = (baseStringExists ? [1] : []) + matchingStrings?.collect{ it[baseString.length()+2..-2].toInteger() }?.sort()

        // iterator will be largest_value_from(iterators) + 1, or 1 if iterators == []
        def iterator = iterators.size() ? iterators[-1] + 1 : 1

        // if we're filling gaps, find the lowest unused number
        if (fillMissing) iterator = (1 .. iterator).find { !(it in iterators) }

        // append the iterator if needed
        if (iterator > 1) baseString += " ($iterator)"

        baseString

    }

    /**
     * Adds entities from the list to parent entity. Remains agnostic about the
     * specific type of TemplateEntity.
     *
     * @param entityList
     * @param parentEntity
     * @return -
     */
    def addEntitiesToParentEntity(entityList, parentEntity, childEntityParentName) {

        def firstEntity     = entityList[0]
        def domainClass     = Holders.grailsApplication.getDomainClass(firstEntity.class.name)

        // figure out the collection name via the hasMany property
        def hasMany         = GrailsClassUtils.getStaticPropertyValue(parentEntity.class, 'hasMany')
        def collectionName  = hasMany.find{it.value == domainClass.clazz}.key.capitalize()

        // add the entities one by one to the parent entity (unless it's set already)
        entityList.each { if (!it."$childEntityParentName") parentEntity."addTo$collectionName" it }

    }

    /**
    * Method to check if all fields of an entity are empty
    *
    * @param theEntity entity object
    */
    def isEntityEmpty(theEntity) {

        theEntity.giveFields().every {

            !theEntity.getFieldValue(it.name)
        }
    }

    /**
	 * This method reads a data row and returns it as filled entity
	 *
     * @param theEntity entity to use
	 * @param theTemplate Template object
	 * @param row list of string values
	 * @param mcmap map containing MappingColumn objects
     * @param dateFormat date formatter (yyyy/MM/dd et cetera)
	 * @return list of entities and list of failed cells
	 */
    def createEntity(theEntity, theTemplate, String[] row, mcmap, dateFormat) {
        def error

		// Initialize the entity with the chosen template
		def entity = gdtService.getInstanceByEntityName(theEntity.entity).
            newInstance(template:theTemplate)

		// Read every cell in the row
		row.eachWithIndex { value, columnIndex ->

            // Get the MappingColumn information of the current cell
			def mc = mcmap[columnIndex]
			// Check if column must be imported
			if (mc != null) if (!mc.dontimport) {
				try {
                    // Format the cell conform the TemplateFieldType
                    value = formatValue(value, mc.templatefieldtype, dateFormat)
                } catch (NumberFormatException nfe) {
                    // Formatting went wrong, so set the value to an empty string
					value = ""
				}

				// Try to set the value for this entity
                try {
                    entity.setFieldValue(mc.property, value, true)
				} catch (Exception iae) {

                    // The entity field value could not be set
                    log.error ".import wizard fieldError could not set property `" + mc.property + "` to value `" + value + "`"

					// Store the fieldError value (might improve this with name of entity instead of "entity_")
                    // as a map containing the entity+identifier+property and the original value which failed
                    error = [ error: 'Empty or non-valid value', identifier: entity.getIdentifier(), property: mc.property.toLowerCase(), entity: "entity_" + entity.getIdentifier() + "_" + mc.property.toLowerCase().replaceAll("([^a-z0-9])", "_"), originalValue: value]
				}
			}
		}

        [entity, error]
    }

    /**
	 * Method to parse a value conform a TemplateFieldType
     *
	 * @param value string containing the value to be formatted
     * @param templateFieldType TemplateFieldType to cast this value to
     * @param dateFormat date formatter (yyyy/MM/dd format)
	 * @return object corresponding to the TemplateFieldType
	 */
	def formatValue(String value, TemplateFieldType templateFieldType, String dateFormat) throws NumberFormatException {
		switch (templateFieldType) {
            case TemplateFieldType.LONG:    return Double.valueOf(value.replace(",", ".")).longValue()
            case TemplateFieldType.DOUBLE:  return Double.valueOf(value.replace(",", "."))
            case TemplateFieldType.DATE:    try {
                                                // Build the date formatter using date format from the parameter to
                                                // parse the value to a date object
                                                SimpleDateFormat parseDateFormatter = new SimpleDateFormat(dateFormat);

                                                // Replace dashes with slashes
                                                def parsedDate = parseDateFormatter.parse( value.replace("-", "/") )

                                                // Build the formatter to convert the date object to the format GSCF
                                                // requires: DAY MONTH YEAR
                                                SimpleDateFormat GSCFDateFormatter = new SimpleDateFormat("dd/MM/yyyy")
                                                return GSCFDateFormatter.format(parsedDate)
                                            }
                                            catch (Exception e) {
                                                log.error ".importer wizard: could not format value `${value}` using formatter [${dateFormat}]"
                                                return value
                                            }
		}
        return value.trim()
	}

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
}
