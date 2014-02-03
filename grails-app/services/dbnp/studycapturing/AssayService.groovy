/**
 * AssayService Service
 *
 * @author s.h.sikkema@gmail.com
 * @since 20101216
 * @package dbnp.studycapturing
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
import org.codehaus.groovy.grails.web.json.JSONObject
import org.dbnp.gdt.RelTime
import org.dbnp.gdt.TemplateFieldType
import java.text.NumberFormat
import dbnp.authentication.SecUser

class AssayService {

    boolean transactional = false
    def authenticationService
    def moduleCommunicationService
    static scope = "singleton" // make sure we have default scope to use the measurementTokensMap

    // Store for measurementToken selections which is used by the Galaxy export routine
    def measurementTokensMap = [:]

    void addMeasurementTokenSelection(String sessionToken, measurementTokens) {
        measurementTokensMap[sessionToken] = measurementTokens
    }

    def retrieveMeasurementTokenSelection(String sessionToken) {
        return measurementTokensMap.remove(sessionToken)
    }

    /**
     * Collects the assay field names per category in a map as well as the
     * module's measurements.
     *
     * @param assay the assay for which to collect the fields
     * @param samples list of samples to retrieve the field names for. If not given, all samples from the assay are used.
     * @return a map of categories as keys and field names or measurements as
     *  values
     */
    def collectAssayTemplateFields(assay, samples, SecUser remoteUser = null) throws Exception {

        def moduleError = '', moduleMeasurements = []

        try {
            moduleMeasurements = requestModuleMeasurementNames(assay, remoteUser)
        } catch (e) {
            moduleError = e.message
        }

        if (!samples)
            samples = assay.samples

        [       'Study Data': getUsedTemplateFields(samples.parent),
                'Subject Data': getUsedTemplateFields(samples*."parentSubject".unique()),
                'Sampling Event Data': (getUsedTemplateFields(samples*.parentEvent.unique()) << getUsedTemplateFields(samples*.parentEvent*.event.unique())).flatten(),
                'Sample Data': getUsedTemplateFields(samples),
                'Event Group': [
                        [name: 'name', comment: 'Name of Event Group', displayName: 'name']
                ],
                'Module Measurement Data': moduleMeasurements,
                'Module Error': moduleError
        ]

    }

    def getUsedTemplateFields = { templateEntities ->
        if (templateEntities instanceof ArrayList && templateEntities.size() > 0 && templateEntities[0] instanceof SamplingEventInEventGroup) {
            return [[name: 'startTime', comment: '', displayName: 'startTime'],
                    [name: 'duration', comment: '', displayName: 'duration']]
        }

        // gather all unique and non null template fields that haves values
        templateEntities*.giveFields().flatten().unique().findAll { field ->

            field && templateEntities.any { it?.fieldExists(field.name) && it.getFieldValue(field.name) != null }

        }.collect { [name: it.name, comment: it.comment, displayName: it.name + (it.unit ? " ($it.unit)" : '')] }
    }

    /**
     * Gathers all assay related data, including measurements from the module,
     * into 1 hash map containing: Subject Data, Sampling Event Data, Sample
     * Data, and module specific measurement data.
     * Data from each of the 4 hash map entries are themselves hash maps
     * representing a descriptive header (field name) as key and the data as
     * value.
     *
     * @param assay the assay to collect data for
     * @param fieldMap map with categories as keys and fields as values
     * @param measurementTokens selection of measurementTokens
     * @param samples list of samples for which the data should be retrieved.
     * 								Defaults to all samples from this assay. Supply [] or
     * 								null to include all samples.
     * @return The assay data structure as described above.
     */
    def collectAssayData(assay, fieldMap, measurementTokens, samples, SecUser remoteUser = null) throws Exception {

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

        // Find samples and sort by name
        if (!samples) samples = assay.samples.toList()
        samples = samples.sort { it.name }

        def eventFieldMap = [:]

        // check whether event group data was requested
        if (fieldMap['Event Group']) {

            def names = samples*.parentEvent*.event.name.flatten()

            // only set name field when there's actual data
            if (!names.every { !it }) eventFieldMap['name'] = names

        }

        def moduleError = '', moduleMeasurementData = [:], moduleMeasurementMetaData = [:]

        if (measurementTokens) {

            try {
                moduleMeasurementData = requestModuleMeasurements(assay, measurementTokens, samples, remoteUser)
            } catch (e) {
                moduleMeasurementData = ['error': [
                        'Module error, module not available or unknown assay']
                        * samples.size()]
                e.printStackTrace()
                moduleError = e.message
            }
        }

        [
                'Study' : ['Title': samples.parent],
                'Subject Data': getFieldValues(samples, fieldMap['Subject Data'], 'parentSubject'),
                'Sampling Event Data': getFieldValues(samples, fieldMap['Sampling Event Data'], 'parentEvent'),
                'Sample Data': getFieldValues(samples, fieldMap['Sample Data']),
                'Event Group': eventFieldMap,
					('Module Measurement Data: ' + assay.name ): 		moduleMeasurementData,
                'Module Error': moduleError
        ]
    }

    /**
     * Prepend data from study to the data structure
     * @param assayData Column wise data structure of samples
     * @param assay Assay object the data should be selected from
     * @param numValues Number of values for this assay
     * @return Extended column wise data structure
     */
    def prependStudyData(inputData, Assay assay, numValues) {
        if (!assay)
            return inputData;

        // Retrieve study data
        def studyData = [:]
        assay.parent?.giveFields().each {
            def value = assay.parent.getFieldValue(it.name)
            if (value)
                studyData[it.name] = [value] * numValues
        }

        return [
                'Study Data': studyData
        ] + inputData
    }

    /**
     * Prepend data from assay to the data structure
     * @param assayData Column wise data structure of samples
     * @param assay Assay object the data should be selected from
     * @param numValues Number of values for this assay
     * @return Extended column wise data structure
     */
    def prependAssayData(inputData, Assay assay, numValues) {
        if (!assay)
            return inputData;

        // Retrieve assay data
        def assayData = [:]
        assay.giveFields().each {
            def value = assay.getFieldValue(it.name)
            if (value)
                assayData[it.name] = [value] * numValues
        }

        return [
                'Assay Data': assayData
        ] + inputData
    }

    /**
     * Retrieves measurement names from the module through a rest call
     *
     * @param consumer the url of the module
     * @param path path of the rest call to the module
     * @return
     */
    def requestModuleMeasurementNames(assay, SecUser remoteUser = null) {

        def moduleUrl = assay.module.baseUrl

        def path = moduleUrl + "/rest/getMeasurements/query"
        def query = "assayToken=${assay.UUID}"
        def jsonArray

        try {
            jsonArray = moduleCommunicationService.callModuleMethod(moduleUrl, path, query, "POST", remoteUser)
        } catch (e) {
            throw new Exception("An error occured while trying to get the measurement tokens from the $assay.module.name. \
             This means the module containing the measurement data is not available right now. Please try again \
             later or notify the system administrator if the problem persists. URL: $path?$query.")
        }

        def result = jsonArray.collect {
            if (it == JSONObject.NULL)
                return ""
            else
                return it.toString()
        }

        return result
    }

    /**
     * Retrieves module measurement data through a rest call to the module
     *
     * @param assay Assay for which the module measurements should be retrieved
     * @param measurementTokens List with the names of the fields to be retrieved. Format: [ 'measurementName1', 'measurementName2' ]
     * @param samples Samples to collect measurements for
     * @return
     */
    def requestModuleMeasurements(assay, List inputMeasurementTokens, List samples, SecUser remoteUser = null) {

        def moduleUrl = assay.module.baseUrl

        def tokenString = ''

        inputMeasurementTokens.each { tokenString += "&measurementToken=${it.encodeAsURL()}" }

        /* Contact module to fetch measurement data */
        def path = moduleUrl + "/rest/getMeasurementData/query"
        def query = "assayToken=$assay.UUID$tokenString"

        if (samples) {
            query += '&' + samples*.UUID.collect { "sampleToken=$it" }.join('&')
        }

        def sampleTokens = [], measurementTokens = [], moduleData = []

        try {
            (sampleTokens, measurementTokens, moduleData) = moduleCommunicationService.callModuleMethod(moduleUrl, path, query, "POST", remoteUser)
        } catch (e) {
            e.printStackTrace()
            throw new Exception("An error occured while trying to get the measurement data from the $assay.module.name. \
             This means the module containing the measurement data is not available right now. Please try again \
             later or notify the system administrator if the problem persists. URL: $path?$query.")
        }

        if (!sampleTokens?.size()) return []

        // Convert the three different maps into a map like:
        //
        // [ "measurement 1": [ value1, value2, value3 ],
        //   "measurement 2": [ value4, value5, value6 ] ]
        //
        // The returned values should be in the same order as the given samples-list
        def map = [:]
        def numSampleTokens = sampleTokens.size();

        measurementTokens.eachWithIndex { measurementToken, measurementIndex ->
            def measurements = [];

            samples.each { sample ->

                // Do measurements for this sample exist? If not, a null value is returned
                // for this sample. Otherwise, the measurement is looked up in the list with
                // measurements, based on the sample token
                if (sampleTokens.collect { it.toString() }.contains(sample.UUID)) {
                    def tokenIndex = sampleTokens.indexOf(sample.UUID);
                    def valueIndex = measurementIndex * numSampleTokens + tokenIndex;

                    // If the module data is in the wrong format, show an error in the log file
                    // and return a null value for this measurement.
                    if (valueIndex >= moduleData.size()) {
                        log.error "Module measurements given by module " + assay.module.name + " are not in the right format: " + measurementTokens?.size() + " measurements, " + sampleTokens?.size() + " samples, " + moduleData?.size() + " values"
                        measurements << null
                    } else {

                        def val
                        def measurement = moduleData[valueIndex]

                        if (measurement == JSONObject.NULL) val = ""
                        else if (measurement instanceof Number) val = measurement
                        else if (measurement.isDouble()) val = measurement.toDouble()
                        else val = measurement.toString()
                        measurements << val

                    }
                } else {
                    measurements << null
                }
            }
            map[measurementToken.toString()] = measurements
        }

        return map;
    }

    /**
     * Retrieves module measurement meta data through a rest call to the module
     *
     * @param assay Assay for which the module measurements should be retrieved
     * @param measurementTokens List with the names of the fields to be retrieved. Format: [ 'measurementName1', 'measurementName2' ]
     * @return
     */
    def requestModuleMeasurementMetaDatas(assay, inputMeasurementTokens, SecUser remoteUser = null) {

        def measurementTokenMetaData = [:]

        def moduleUrl = assay.module.baseUrl

        def tokenString = ''
        inputMeasurementTokens.each { tokenString += "&measurementToken=${it.encodeAsURL()}" }

        def pathMeasurementMetaData = moduleUrl + "/rest/getMeasurementMetaData/"
        def queryMeasurementMetaData = "assayToken=$assay.UUID$tokenString"

        try {
            moduleCommunicationService.callModuleMethod(moduleUrl, pathMeasurementMetaData, queryMeasurementMetaData, "POST", remoteUser).each { metaDataArray ->
                if (metaDataArray['name']) {
                    measurementTokenMetaData[metaDataArray['name']] = metaDataArray //convert list to an associative array
                }
            }
        } catch (e) {
            e.printStackTrace()
            throw new Exception("An error occured while trying to get the measurement meta data from the $assay.module.name. \
			This means the module containing the measurement data is not available right now. Please try again \
			later or notify the system administrator if the problem persists. URL: $path?$query.")
        }

        return measurementTokenMetaData
    }

    /**
     * Modules can provide meta-data about the measurements. If so they can be added to the export.
     * This method extends the data with the meta-data.
     *
     * @return data + meta-data
     */
    def mergeModuleDataWithMetadata(data, metadata = null) {

        if (metadata == null) {
            return data
        }

        //check if there is meta-data available for the features in the data
        if (!(data[1].intersect(metadata.keySet()))) {
            return data
        }

        //find out where the measurements start in the data
        def addLabelsAtColumnPosition = null
        data[0].eachWithIndex { cat, i ->
            if (cat == 'Module Measurement Data') {
                addLabelsAtColumnPosition = i
            }
        }

        //check if we have a position to inject the labels
        if (addLabelsAtColumnPosition == null) {
            return data //returning data as we were unable to find any measurements. Or at least where the start.
        }

        //find out all (unique) feature properties
        def featureProperties = []
        metadata.values().each { featureProperties += it.keySet() }
        featureProperties = featureProperties.unique()

        //prepare the additional rows with meta-data
        def additionalRows = []
        featureProperties.each { mdfp ->
            def addRow = []
            (1..addLabelsAtColumnPosition).each {
                //add some empty fields before the labels of the meta-data properties
                addRow.add(null)
            }

            addRow.add(mdfp)
            data[1].eachWithIndex { feature, i ->
                if (i >= addLabelsAtColumnPosition) {
                    if (metadata[feature]) {
                        addRow.add(metadata[feature][mdfp])
                    } else {
                        addRow.add('')
                    }
                }
            }
            additionalRows.add(addRow)
        }

        //add the additional row and add a null to the feature label column in the data rows
        if (additionalRows) {
            def tempData = []
            data.eachWithIndex { row, iRow ->

                //this is an existing row (not meta-data), so we add a null under the feature label column
                def tempR = []
                row.eachWithIndex { rowElement, iRowElement ->
                    if ((iRow != 0) && (iRowElement == addLabelsAtColumnPosition)) {
                        tempR.add(null) //add an empty element under the meta-data label
                    }
                    tempR.add(rowElement)
                }
                tempData.add(tempR)

                //as the additional rows have already been formatted in the correct way we only have to add them under the row that holds the categories
                if (iRow == 1) {
                    additionalRows.each { additionalRow ->
                        tempData.add(additionalRow)
                    }
                }
            }
            //overwrite data with the tempRowData
            data = tempData
        }

        return data
    }

    /**
     * Merges the data from multiple studies into a structure that can be exported to an excel file. The format for each assay is
     *
     * 	[Category1:
     *      [Column1: [1,2,3], Column2: [4,5,6]],
     *   Category2:
     *      [Column3: [7,8,9], Column4: [10,11,12], Column5: [13,14,15]]]
     *
     * Where the category describes the category of data that is presented (e.g. subject, sample etc.) and the column names describe
     * the fields that are present. Each entry in the lists shows the value for that column for an entity. In this case, 3 entities are described.
     * Each field should give values for all entities, so the length of all value-lists should be the same.
     *
     * Example: If the following input is given (2 assays)
     *
     * 	[
     *    [Category1:
     *      [Column1: [1,2,3], Column2: [4,5,6]],
     *     Category2:
     *      [Column3: [7,8,9], Column4: [10,11,12], Column5: [13,14,15]]],
     *    [Category1:
     *      [Column1: [16,17], Column6: [18,19]],
     *     Category3:
     *      [Column3: [20,21], Column8: [22,23]]]
     * ]
     *
     * the output will be (5 entries for each column, empty values for fields that don't exist in some assays)
     *
     * 	[
     *    [Category1:
     *      [Column1: [1,2,3,16,17], Column2: [4,5,6,,], Column6: [,,,18,19]],
     *     Category2:
     *      [Column3: [7,8,9,,], Column4: [10,11,12,,], Column5: [13,14,15,,]],
     *     Category3:
     *      [Column3: [,,,20,21], Column8: [,,,22,23]]
     * ]
     *
     *
     * @param columnWiseAssayData List with each entry being the column wise data of an assay. The format for each
     * 								entry is described above
     * @return Hashmap                Combined assay data, in the same structure as each input entry. Empty values are given as an empty string.
     * 								So for input entries
     */
    def mergeColumnWiseDataOfMultipleStudies(def columnWiseAssayData) {
        // Compute the number of values that is expected for each assay. This number is
        // used later on to determine the number of empty fields to add if a field is not present in this
        // assay
        def numValues = columnWiseAssayData.collect { assay ->
            for (cat in assay) {
                if (cat) {
                    for (field in cat.value) {
                        if (field?.value?.size() > 0) {
                            return field.value.size();
                        }
                    }
                }
            }

            return 0;
        }

        // Merge categories from all assays. Create a list for all categories
        def categories = columnWiseAssayData*.keySet().toList().flatten().unique();
        def mergedColumnWiseData = [:]
        categories.each { category ->
            // Only work with this category for all assays
            def categoryData = columnWiseAssayData*.getAt(category);

            // Find the different fields in all assays
            def categoryFields = categoryData.findAll { it }*.keySet().toList().flatten().unique();

            // Find data for all assays for these fields. If the fields do not exist, return an empty string
            def categoryValues = [:]
            categoryFields.each { field ->
                categoryValues[field] = [];

                // Loop through all assays
                categoryData.eachWithIndex { assayValues, idx ->
                    if (assayValues && assayValues.containsKey(field)) {
                        // Append the values if they exist
                        categoryValues[field] += assayValues[field];
                    } else {
                        // Append empty string for each entity if the field doesn't exist
                        categoryValues[field] += [""] * numValues[idx]
                    }
                }
            }

            mergedColumnWiseData[category] = categoryValues
        }

        return mergedColumnWiseData;
    }

    /**
     * Merges the data from multiple studies into a structure that can be exported to an excel file. The format for each assay is
     *
     * 	[Category1:
     *      [Column1: [1,2,3], Column2: [4,5,6]],
     *   Category2:
     *      [Column3: [7,8,9], Column4: [10,11,12], Column5: [13,14,15]]]
     *
     * Where the category describes the category of data that is presented (e.g. subject, sample etc.) and the column names describe
     * the fields that are present. Each entry in the lists shows the value for that column for an entity. In this case, 3 entities are described.
     * Each field should give values for all entities, so the length of all value-lists should be the same.
     *
     * Example: If the following input is given (2 assays)
     *
     * 	[
     *    [Category1:
     *      [Column1: [1,2,3], Column2: [4,5,6]],
     *     Category2:
     *      [Column3: [7,8,9], Column4: [10,11,12], Column5: [13,14,15]]],
     *    [Category1:
     *      [Column1: [16,17], Column6: [18,19]],
     *     Category3:
     *      [Column3: [20,21], Column8: [22,23]]]
     * ]
     *
     * the output will be (5 entries for each column, empty values for fields that don't exist in some assays)
     *
     * 	[
     *    [Category1:
     *      [Column1: [1,2,3,16,17], Column2: [4,5,6,,], Column6: [,,,18,19]],
     *     Category2:
     *      [Column3: [7,8,9,,], Column4: [10,11,12,,], Column5: [13,14,15,,]],
     *     Category3:
     *      [Column3: [,,,20,21], Column8: [,,,22,23]]
     * ]
     *
     *
     * @param columnWiseAssayData List with each entry being the column wise data of an assay. The format for each
     * 								entry is described above. The data MUST have a category named 'Sample Data' and in that map a field
     * 								named 'id'. This field is used for matching rows. However, the column is removed, unless
     * 								removeIdColumn is set to false
     * @param removeIdColumn If set to true (default), the values for the sample id are removed from the output.
     * @return Hashmap                Combined assay data, in the same structure as each input entry. Empty values are given as an empty string.
     * 								So for input entries
     */
    def mergeColumnWiseDataOfMultipleStudiesForASetOfSamples(def columnWiseAssayData, boolean removeIdColumn = true) {
        // Merge all assays and studies into one list
        def mergedData = mergeColumnWiseDataOfMultipleStudies(columnWiseAssayData)

        // A map with keys being the sampleIds, and the values are the indices of that sample in the values list
        def idMap = [:]

        // A map with the key being an index in the value list, and the value is the index the values should be copied to
        def convertMap = [:]

        for (int i = 0; i < mergedData["Sample Data"]["id"].size(); i++) {
            def id = mergedData["Sample Data"]["id"][i];

            if (idMap[id] == null) {
                // This id occurs for the first time
                idMap[id] = i;
                convertMap[i] = i;
            } else {
                convertMap[i] = idMap[id];
            }
        }

        /*
         * Example output:
         * idMap:      [ 12: 0, 24: 1, 26: 3 ]
         * convertMap: [ 0: 0, 1: 1, 2: 0, 3: 3, 4: 3 ]
         *   (meaning: rows 0, 1 and 3 should remain, row 2 should be merged with row 0 and row 4 should be merged with row 3)
         *
         * The value in the convertMap is always lower than its key. So we sort the convertMap on the keys. That way, we can
         * loop through the values and remove the row that has been merged.
         */

        convertMap.sort { a, b -> b.key <=> a.key }.each {
            def row = it.key;
            def mergeWith = it.value;

            if (row != mergeWith) {
                // Combine the data on row [row] with the data on row [mergeWith]

                mergedData.each {
                    def cat = it.key; def fields = it.value;
                    fields.each { fieldData ->
                        def fieldName = fieldData.key;
                        def fieldValues = fieldData.value;

                        // If one of the fields to merge is empty, use the other one
                        // Otherwise the values should be the same (e.g. study, subject, sample data)
                        fieldValues[mergeWith] = (fieldValues[mergeWith] == null || fieldValues[mergeWith] == "") ? fieldValues[row] : fieldValues[mergeWith]

                        // Remove the row from this list
                        fieldValues.remove(row);
                    }
                }
            }
        }

        // Remove sample id if required
        if (removeIdColumn)
            mergedData["Sample Data"].remove("id");

        return mergedData
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

            def headers = [[], []]

            columnData.each { category ->

                if (category.value.size()) {

                    // put category keys into first row separated by null values
                    // wherever there are > 1 columns per category
                    headers[0] += [category.key] + [null] * (category.value.size() - 1)

                    // put non-category column headers into 2nd row
                    headers[1] += category.value.collect { it.key }

                }

            }

            def d = []

            // add all column wise data into 'd'
            columnData.each { it.value.each { d << it.value } }

            // transpose d into row wise data and combine with header rows
            headers + d.transpose()
        } else []

    }

    /**
     * Export column wise data in Excel format to a stream.
     *
     * @param columnData Multidimensional map containing column data
     * @param outputStream Stream to write to
     * @param useOfficeOpenXML Flag to specify xlsx (standard) or xls output
     * @return
     */
    def exportColumnWiseDataToExcelFile(columnData, outputStream, useOfficeOpenXML = true) {

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
     * @param rowData List of lists containing for each row all cell values
     * @param outputStream Stream to write to
     * @param useOfficeOpenXML Flag to specify xlsx (standard) or xls output
     * @return
     */
    def exportRowWiseDataToExcelFile(rowData, outputStream, useOfficeOpenXML = true) {
        Workbook wb = useOfficeOpenXML ? new XSSFWorkbook() : new HSSFWorkbook()
        Sheet sheet = wb.createSheet()

        exportRowWiseDataToExcelSheet(rowData, sheet);

        wb.write(outputStream)
        outputStream.close()
    }

    /**
     * Export row wise data in CSV to a stream. All values are surrounded with
     * double quotes (" ").
     *
     * @param rowData List of lists containing for each row all cell values
     * @param outputStream Stream to write to
     * @return
     */
    def exportRowWiseDataToCSVFile(rowData, outputStream, outputDelimiter = '\t', locale = java.util.Locale.US) {

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

        outputStream.close()
    }

    /**
     * Export row wise data for multiple assays in Excel format (separate sheets) to a stream
     *
     * @param rowData List of structures with rowwise data for each assay
     * @param outputStream Stream to write to
     * @param useOfficeOpenXML Flag to specify xlsx (standard) or xls output
     * @return
     */
    def exportRowWiseDataForMultipleAssaysToExcelFile(assayData, outputStream, useOfficeOpenXML = true) {
        Workbook wb = useOfficeOpenXML ? new XSSFWorkbook() : new HSSFWorkbook()

        assayData.each { rowData ->
            Sheet sheet = wb.createSheet()

            exportRowWiseDataToExcelSheet(rowData, sheet);
        }

        wb.write(outputStream)
        outputStream.close()
    }

    /**
     * Export row wise data in Excel format to a given sheet in an excel workbook
     *
     * @param rowData List of lists containing for each row all cell values
     * @param sheet Excel sheet to append the
     * @return
     */
    def exportRowWiseDataToExcelSheet(rowData, Sheet sheet) {
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