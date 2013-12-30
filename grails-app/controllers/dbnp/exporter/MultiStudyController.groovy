package dbnp.exporter

import dbnp.authentication.SecUser
import dbnp.studycapturing.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.dbnp.gdt.*
import grails.plugins.springsecurity.Secured
import grails.converters.JSON
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry

import java.text.NumberFormat

@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class MultiStudyController {

    def assayService
    def authenticationService
    def fileService

    def index() {
        def user = authenticationService.getLoggedInUser()
        [userStudies :Study.giveReadableStudies(user)]
    }

    def studyExport() {
        def params = getParams();
        def selectedStudies = []
        def selectedAssays = []
        def fieldMap;

        if(!params['studies'])
            throw new Exception( "No study selected" )

        params.list('studies').each {
            def studyId = it.toInteger();
            selectedStudies << Study.read(studyId)
            if(params['study-'+studyId+'-assay']) {
                params.list('study-'+studyId+'-assay').each {
                    selectedAssays << Assay.read(it.toInteger())
                }
            }
        }

        fieldMap = mergeFieldMaps( selectedAssays.collect { assay -> assayService.collectAssayTemplateFields(assay, null) } )

        def outputDelimiter, sampleData, studyData, locale = java.util.Locale.US
        outputDelimiter = ';'
        locale = java.util.Locale.GERMAN // force use of comma as decimal separator

        def filename = "export.xls"
        response.setHeader("Content-disposition", "attachment;filename=\"${filename}\"")
        response.setContentType("application/octet-stream")
        try {
            studyData = this.collectStudyData(fieldMap, selectedStudies)
            sampleData = this.collectSampleData(fieldMap, selectedAssays*.samples.flatten().unique())

            Workbook wb = new XSSFWorkbook()

            Sheet studySheet = wb.createSheet()
            Sheet samplesSheet = wb.createSheet()

            sampleData = convertColumnToRowStructure(sampleData)
            studyData = convertDataToStudyStructure(studyData)

            exportRowWiseDataToExcelSheet(studyData, studySheet)
            exportRowWiseDataToExcelSheet(sampleData, samplesSheet)

            wb.write(response.outputStream)
            response.outputStream.close()

            // clear the data from the session
            session.removeAttribute('rowData')
            session.removeAttribute('measurementTokens')
            session.removeAttribute('exportFileType')
            session.removeAttribute('exportMetadata')

        } catch (Exception e) {
            e.printStackTrace();
            render "An error has occurred while performing the export. Please notify an administrator"
        }
    }

    def collectStudyData(fieldMap, studies) throws Exception {
        [
                'Study Information' : getFieldValues(studies, fieldMap['Study Data']),
                'Event Information': ['Subject': ['test'] as ArrayList],
                'Sampling Event Data': ['Subject': ['test'] as ArrayList],
                'Assay': ['Subject': ['test'] as ArrayList]
        ]
    }

    def collectSampleData(fieldMap, samples) throws Exception {
        [
                'Study' : ['Study': samples.parent],
                'Subject Data': ['Subject': samples.parentSubject.name],
                'Sampling Event Data': getFieldValues(samples, fieldMap['Sampling Event Data'], 'parentEvent'),
                'Sample Data': getFieldValues(samples, fieldMap['Sample Data'])
        ]
    }

    /**
     * Merges multiple fieldmaps as returned from assayService.collectAssayTemplateFields(). For each category,
     * a list is returned without duplicates
     * @param fieldMaps     ArrayList of fieldMaps
     * @return              A single fieldmap
     */
    def mergeFieldMaps( fieldMaps ) {
        if( !fieldMaps || !( fieldMaps instanceof Collection ) )
            throw new Exception( "No or invalid fieldmaps given" )

        if( fieldMaps.size() == 1 )
            return fieldMaps[ 0 ]

        // Loop over each fieldmap and combine the fields from different categories
        def mergedMap = fieldMaps[ 0 ]
        fieldMaps[1..-1].each { fieldMap ->
            fieldMap.each { key, value ->
                if( value instanceof Collection ) {
                    if( mergedMap.containsKey( key ) ) {
                        value.each {
                            if( !mergedMap[ key ].contains( it ) )
                                mergedMap[ key ] << it
                        }
                    } else {
                        mergedMap[ key ] = value
                    }
                } else {
                    if( mergedMap.containsKey( key ) ) {
                        if( !mergedMap[ key ].contains( value ) )
                            mergedMap[ key ] << value
                    } else {
                        mergedMap[ key ] = [ value ]
                    }
                }
            }
        }

        mergedMap
    }

    def collectFieldValuesForTemplateEntities = { headerFields, templateEntities ->

        // return a hash map with for each field name all values from the
        // template entity list
        headerFields.inject([:]) { map, headerField ->

            map + [(headerField.displayName): templateEntities.collect { entity ->

                // default to an empty string
                def val = ''

                if (entity) {
                    def field
                    try {

                        val = entity.getFieldValue(headerField.name)

                        // Convert RelTime fields to human readable strings
                        field = entity.getField(headerField.name)
                        if (field.type == TemplateFieldType.RELTIME)
                            val = new RelTime(val as long)

                    } catch (NoSuchFieldException e) { /* pass */ }
                }

                (val instanceof Number) ? val : val.toString()
            }]
        }
    }

    def collectStaticFieldValuesForTemplateEntities = { headerFields, templateEntities ->
        headerFields.inject([:]) { map, headerField ->
            map + [(headerField.displayName): templateEntities.collectEntries { entity ->
                def returnVal = ''
                switch (headerField.displayName) {
                    case 'startTime':
                        returnVal = entity.startTime
                        break
                    case 'duration':
                        returnVal = entity.duration
                        break
                    default:
                        break
                }
                [(entity.id): returnVal]
            }]
        }
    }

    def getFieldValues = { templateEntities, headerFields, propertyName = '' ->

        def returnValue

        // if no property name is given, simply collect the fields and
        // values of the template entities themselves
        if (propertyName == '') {

            returnValue = collectFieldValuesForTemplateEntities(headerFields, templateEntities)

        } else {

            // if a property name is given, we'll have to do a bit more work
            // to ensure efficiency. The reason for this is that for a list
            // of template entities, the properties referred to by
            // propertyName can include duplicates. For example, for 10
            // samples, there may be less than 10 parent subjects. Maybe
            // there's only 1 parent subject. We don't want to collect field
            // values for this single subject 10 times ...
            def fieldValues
            def staticFieldValues
            def uniqueProperties

            // we'll get the unique list of properties to make sure we're
            // not getting the field values for identical template entity
            // properties more then once.
            if (propertyName.equals('parentEvent')) {
                uniqueProperties = templateEntities*.parentEvent*.event.unique()
                staticFieldValues = collectStaticFieldValuesForTemplateEntities(headerFields, templateEntities*.parentEvent.unique())
            } else {
                uniqueProperties = templateEntities*."$propertyName".unique()
            }

            fieldValues = collectFieldValuesForTemplateEntities(headerFields, uniqueProperties)

            // prepare a lookup hashMap to be able to map an entities'
            // property (e.g. a sample's parent subject) to an index value
            // from the field values list
            int i = 0
            def propertyToFieldValueIndexMap = uniqueProperties.inject([:]) { map, item -> map + [(item): i++] }

            // prepare the return value so that it has an entry for field
            // name. This will be the column name (second header line).
            returnValue = headerFields*.displayName.inject([:]) { map, item -> map + [(item): []] }

            // finally, fill map the unique field values to the (possibly
            // not unique) template entity properties. In our example with
            // 1 unique parent subject, this means copying that subject's
            // field values to all 10 samples.
            templateEntities.each { te ->

                headerFields*.displayName.each {
                    if (propertyName.equals('parentEvent')) {
                        if (it.equals('startTime') || it.equals('duration')) {
                            returnValue[it] << staticFieldValues[it][te.parentEvent.id]
                        } else {
                            returnValue[it] << fieldValues[it][propertyToFieldValueIndexMap[te.parentEvent['event']]]
                        }
                    } else {
                        returnValue[it] << fieldValues[it][propertyToFieldValueIndexMap[te[propertyName]]]
                    }
                }
            }
        }
        returnValue
    }

    /**
     * Converts column
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
     * @return row wise data
     */
    def convertColumnToRowStructure(columnData) {

        // check if all columns have the dimensionality 2
        if (columnData.every { it.value.every { it.value instanceof ArrayList } }) {

            def headers = [[]]

            columnData.each { category ->

                if (category.value.size()) {

                    // put category keys into first row separated by null values
                    // wherever there are > 1 columns per category
                    //headers[0] += [category.key] + [null] * (category.value.size() - 1)

                    // put non-category column headers into 2nd row
                    headers[0] += category.value.collect { it.key }

                }

            }

            def d = []

            // add all column wise data into 'd'
            columnData.each { it.value.each { d << it.value } }

            // transpose d into row wise data and combine with header rows
            headers + d.transpose()
        } else []

    }

    def convertDataToStudyStructure(columnData) {
            def headers = [:]
            def d = []
            def iter = 0;
            columnData.each { category ->

                if (category.value.size()) {

                    // put category keys into first row separated by null values
                    // wherever there are > 1 columns per category
                    def position = (iter - category.value.size())
                    if(position < 0) {
                        headers[0][0] = [category.key]
                    } else {
                        headers[0][position] = [category.key]
                    }

                    // put non-category column headers into 2nd row
                    headers[1][iter] = category.value.collect { it.key }

                }
                iter++

            }


            // add all column wise data into 'd'
            columnData.each { d += it.value.collect { it.value } }

            // transpose d into row wise data and combine with header rows
            headers + d
    }

    /**
     * Export row wise data in CSV to a stream. All values are surrounded with
     * double quotes (" ").
     *
     * @param rowData List of lists containing for each row all cell values
     * @param outputStream Stream to write to
     * @return
     */
    def exportRowWiseDataToCSVFile(rowData, outputStream, outputDelimiter, locale) {

        def formatter = NumberFormat.getNumberInstance(locale)
        formatter.setGroupingUsed false // we don't want grouping (thousands) separators
        formatter.setMaximumFractionDigits(15)

        outputStream << rowData.collect { row ->

            row.collect {

                // omit quotes in case of numeric values and format using chosen locale
                if (it instanceof Number) return formatter.format(it)

                def s = it?.toString() ?: ''

                def addQuotes = false

                // escape double quotes with double quotes if they exist and
                // enable surround with quotes
                if (s.contains('"')) {
                    addQuotes = true
                    s = s.replaceAll('"', '""')
                } else {
                    // enable surround with quotes in case of comma's
                    if (s.contains(',') || s.contains('\n')) addQuotes = true
                }

                addQuotes ? "\"$s\"" : s

            }.join(outputDelimiter)
        }.join('\n')

        //outputStream.close()
    }

    def exportRowWiseDataToExcelSheet(rowData, sheet) {
        // create all rows
        rowData.size().times { sheet.createRow it }

        sheet.eachWithIndex { Row row, ri ->
            if (rowData[ri]) {
                // create appropriate number of cells for this row
                rowData[ri].size().times { row.createCell it }

                row.eachWithIndex { Cell cell, ci ->

                    // Numbers and values of type boolean, String, and Date can be
                    // written as is, other types need converting to String
                    def value = rowData[ri][ci]

                    value = (value instanceof Number | value?.class in [
                            boolean.class,
                            String.class,
                            Date.class
                    ]) ? value : value?.toString()

                    // write the value (or an empty String if null) to the cell
                    cell.setCellValue(value ?: '')

                }
            }
        }
    }
}