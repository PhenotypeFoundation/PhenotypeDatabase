/**
 * AssayService Service
 *
 * @author  s.h.sikkema@gmail.com
 * @since	20101216
 * @package	dbnp.studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.studycapturing

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import grails.converters.JSON
import javax.servlet.http.HttpServletResponse

class AssayService {

    boolean transactional = true
    def authenticationService
    def moduleCommunicationService

    /**
     * Gathers all assay related data, including measurements from the module,
     * into 1 hash map containing: Subject Data, Sampling Event Data, Sample
     * Data, and module specific measurement data.
     * Data from each of the 4 hash map entries are themselves hash maps
     * representing a descriptive header (field name) as key and the data as
     * value.
     *
     * @param assay the assay to collect data for
     * @consumer the module url
     * @return The assay data structure as described above.
     */
    def collectAssayData(assay, consumer) throws Exception {

        def path = "/rest/getMeasurementData?assayToken=$assay.assayUUID"

        // check whether module is reachable
        if (!moduleCommunicationService.isModuleReachable(consumer)) {

            throw new Exception('Module is not reachable')

        }

        // Gather sample meta data from GSCF
        def samples = assay.samples

        def getUsedTemplateFieldNames = { templateEntities ->

            // gather all unique and non null template fields that haves values
            templateEntities*.giveFields().flatten().unique().findAll{ field ->

                field && templateEntities.any { it.fieldExists(field.name) && it.getFieldValue(field.name) }

            }*.name

        }

        def collectFieldValuesForTemplateEntities = { templateFieldNames, templateEntities ->

            // return a hash map with for each field name all values from the
            // template entity list
            templateFieldNames.inject([:]) { map, fieldName ->

                map + [(fieldName): templateEntities.collect {

                    it?.fieldExists(fieldName) ? it.getFieldValue(fieldName) : ''

                }]

            }

        }

        def getFieldValues = { templateEntities, propertyName = '' ->

            def returnValue

            // if no property name is given, simply collect the fields and
            // values of the template entities themselves
            if (propertyName == '') {

                returnValue = collectFieldValuesForTemplateEntities(getUsedTemplateFieldNames(templateEntities), templateEntities)

            } else {

                // if a property name is given, we'll have to do a bit more work
                // to ensure efficiency. The reason for this is that for a list
                // of template entities, the properties referred to by
                // propertyName can include duplicates. For example, for 10
                // samples, there may be less than 10 parent subjects. Maybe
                // there's only 1 parent subject. We don't want to collect field
                // values for this subject 10 times ...
                def fieldNames, fieldValues

                // we'll get the unique list of properties to make sure we're
                // not getting the field values for identical template entity
                // properties more then once.
                def uniqueProperties = templateEntities*."$propertyName".unique()

                fieldNames =    getUsedTemplateFieldNames(uniqueProperties)
                fieldValues =   collectFieldValuesForTemplateEntities(fieldNames, uniqueProperties)

                // prepare a lookup hashMap to be able to map an entities'
                // property (e.g. a sample's parent subject) to an index value
                // from the field values list
                int i = 0
                def propertyToFieldValueIndexMap = uniqueProperties.inject([:]) { map, item -> map + [(item):i++]}

                // prepare the return value so that it has an entry for field
                // name. This will be the column name (second header line).
                returnValue = fieldNames.inject([:]) { map, item -> map + [(item):[]] }

                // finally, fill map the unique field values to the (possibly
                // not unique) template entity properties. In our example with
                // 1 unique parent subject, this means copying that subject's
                // field values to all 10 samples.
                templateEntities.each{ te ->

                    fieldNames.each{

                        returnValue[it] << fieldValues[it][propertyToFieldValueIndexMap[te[propertyName]]]

                    }

                }

            }

            returnValue

        }

        [   'Subject Data' :            getFieldValues(samples, 'parentSubject'),
            'Sampling Event Data' :     getFieldValues(samples, 'parentEvent'),
            'Sample Data' :             getFieldValues(samples),
            'Event Group' :             [name: samples*.parentEventGroup*.name.flatten()],
            'Module Measurement Data':  requestModuleMeasurements(consumer, path)]
    }

    /**
     * Retrieves module measurement data through a rest call to the module
     *
     * @param consumer the url of the module
     * @param path of the rest call to the module
     * @return
     */
    def requestModuleMeasurements(consumer, path) {

        def (sampleTokens, measurementTokens, moduleData) = moduleCommunicationService.callModuleRestMethodJSON(consumer, consumer+path)

        if (!sampleTokens?.size()) return []

        def lastDataIndex   = moduleData.size() - 1
        def stepSize        = sampleTokens.size() + 1

        // Transpose the data to order it by measurement (compound) so it can be
        // written as 1 column
        int i = 0
        measurementTokens.inject([:]) { map, token ->

            map + [(token): moduleData[(i++..lastDataIndex).step(stepSize)]]

        }

    }

    /**
     * Export column wise data in Excel format to a stream.
     *
     * @param columnData multidimensional map containing column data.
     * On the top level, the data must be grouped by category. Each key is the
     * category title and the values are maps representing the columns. Each
     * column also has a title (its key) and a list of values. Columns must be
     * equally sized.
     *
     * For example, consider the following map:
     * [Category1:
     *      [Column1: [1,2,3], Column2: [4,5,6]],
     *  Category2:
     *      [Column3: [7,8,9], Column4: [10,11,12], Column5: [13,14,15]]]
     *
     * which will be written as:
     *
     * | Category1  |           | Category2 |           |           |
     * | Column1    | Column2   | Column3   | Column4   | Column5   |
     * | 1          | 4         | 7         | 10        | 13        |
     * | 2          | 5         | 8         | 11        | 14        |
     * | 3          | 6         | 9         | 12        | 15        |
     *
     * @param outputStream the stream to write to
     * @param useOfficeOpenXML flag to specify xlsx (standard) or xls output
     * @return
     */
    def exportColumnWiseDataToExcelFile(columnData, outputStream, useOfficeOpenXML = true) {

        def convertColumnToRowStructure = { data ->

            // check if all columns have the dimensionality 2
            if (data.every { it.value.every { it.value instanceof ArrayList } }) {

                def headers = [[],[]]

                data.each { category ->

                    if (category.value.size()) {

                        // put category keys into first row separated by null values
                        // wherever there are > 1 columns per category
                        headers[0] += [category.key] + [null] * (category.value.size() - 1)

                        // put non-category column headers into 2nd row
                        headers[1] += category.value.collect{it.key}

                    }

                }

                def d = []

                // add all column wise data into 'd'
                data.each { it.value.each { d << it.value } }

                // transpose d into row wise data and combine with header rows
                headers + d.transpose()
            }

        }
        // transform data into row based structure for easy writing
        def rows = convertColumnToRowStructure(columnData)

        if (rows) {

            exportRowWiseDataToExcelFile(rows, outputStream, useOfficeOpenXML)

        } else {

            throw new Exception('Wrong column data format.')

        }

    }

    /**
     * Export row wise data in Excel format to a stream
     *
     * @param rowData a list of lists containing for each row all cell values
     * @param outputStream the stream to write to
     * @param useOfficeOpenXML flag to specify xlsx (standard) or xls output
     * @return
     */
    def exportRowWiseDataToExcelFile(rowData, outputStream, useOfficeOpenXML = true) {

        Workbook wb = useOfficeOpenXML ? new XSSFWorkbook() : new HSSFWorkbook()
        Sheet sheet = wb.createSheet()

        // create all rows
        rowData.size().times { sheet.createRow it }

        sheet.eachWithIndex { Row row, ri ->

            // create appropriate number of cells for this row
            rowData[ri].size().times { row.createCell it }

            row.eachWithIndex { Cell cell, ci ->

                // Numbers and values of type boolean, String, and Date can be
                // written as is, other types need converting to String
                def value = rowData[ri][ci]

                value = (value instanceof Number | value?.class in [boolean.class, String.class, Date.class]) ? value : value?.toString()

                // write the value (or an empty String if null) to the cell
                cell.setCellValue(value ?: '')

            }

        }

        wb.write(outputStream)
        outputStream.close()

    }

}
