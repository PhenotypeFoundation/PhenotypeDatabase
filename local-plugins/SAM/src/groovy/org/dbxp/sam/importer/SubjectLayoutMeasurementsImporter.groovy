package org.dbxp.sam.importer

import dbnp.importer.*
import org.dbxp.sam.*
import org.dbnp.gdt.AssayModule
import org.dbnp.gdt.RelTime
import dbnp.studycapturing.*
import groovy.sql.Sql

import grails.util.Holders

/**
 * Defines the interface for an exporter
 */
public class SubjectLayoutMeasurementsImporter extends AbstractImporter {
    def dataSource = Holders.grailsApplication.mainContext.getBean('dataSource')
     
    /**
     * Returns an identifier that describes this importer
     */
    public String getIdentifier() {
        "Measurements (subject layout)"
    }
    
    /**
     * Returns true if this importer supports the given type.
     * Can be used to filter the available importers on a certain type
     */
    public boolean supportsType(String type) {
        type in [ "SAM", "measurements" ]
    }
    
    /**
     * Returns a map of parameters that should be set for this importer
     */
    public List<ImporterParameter> getParameters(def settings = [:]) {
        // Retrieve a list of writable assays for the current module
        def assayModule = AssayModule.findByName(settings.module)
        def assays = Assay.findAllByModule(assayModule).findAll { it.parent.canWrite(user) }
        
        [
            new ImporterParameter(name: 'assay', label: 'Assay', type: 'select', values: assays ),
            new ImporterParameter(name: 'platform', label: 'Platform', type: 'select', values: Platform.findAllByPlatformtype(settings.module) ),
            new ImporterParameter(name: 'module', label: 'Module', type: 'hidden' ),
        ]
    }
   
    /**
     * Returns a link to the results page
     */
    public Map getLinkToResults(def parameters) {
        [
            url: [ controller: 'SAMAssay', action: 'show', id: parameters.assay, params: [module: parameters.module]],
            label: "Assay measurements"
        ]
    }

    /**
     * Returns a list of header options to match the headers against.
     * Each item should have an id (String) and a name. A description can be specified with extra information
     *
     * @param parameters        Map with settings for the parameters specified in the getParameters method
     * @return  
     * @see getParameters()
     */
    public List getHeaderOptions(def parameters) {
        // The user could choose from the features belonging to the selected platform
        // The user could also choose a column to contain the sample name
        def options = [
            [ id: 'subject', name: "Subject name" ]
        ]
        
        // Create a list of domain fields and template fields to match against
        def platformId = parameters.platform
        def features = Feature.findAll {
            platform.id == platformId
        }
         
        // Create a proper format
        options += features.collect { 
            [ id: it.id.toString(), name: it.name ] 
        }
        
        options
    }
    
    /**
     * Returns whether the header options can only be selected for a single header.
     */
    public boolean headerMappingIsUnique() { false }
    
    /**
     * Validates provided data.
     * @param   data            Matrix (List of lists) with the data that has been loaded from the excel/csv file
     * @param   parameters      Parameters provided by the user. This map includes keys:
     *                            upload        Parameters about the uploaded file. Should not be needed, as the file has been parsed already
     *                            parameter     Refers to a map with parameter values for the parameters needed by the importer
     * @return  True if all objects were imported succesfully,
     *          false if the validation on any of the object has failed
     */
    public boolean validateData(def data, def mapping, def parameters) {
        resetValidationErrors()

        def metadata = getMetadataFromData(data, mapping, parameters)
        def uniqueTimepoints = metadata.timepointColumns.values()*.value.unique()
        
        def subjectMapping = getSubjectMapping(parameters.assay as Long)

        def sampleMap = getSamples( subjectMapping.values(), metadata, parameters.assay as Long).groupBy({it[1]}, {it[2]})
        
        // The first two lines are not used as measurements, so these lines are skipped
        for( def lineNr = 2; lineNr < data.size(); lineNr++) {
            def line = data[lineNr]

            // Check subject name and cast to String
            def requiredSubjectName = line[metadata.subjectColumn].toString()
            
            if( !subjectMapping.containsKey(requiredSubjectName) ) {
                errors << new ImportValidationError(
                    code: 6,
                    message: "A subject with subject name '" + requiredSubjectName + "' could not be found in the specified assay.",
                    line: lineNr,
                    column: metadata.subjectColumn
                )
                
                // Without a subject, we can't check the sample existence at all, so we'll continue
                continue
            }
            
            // Check whether there is a sample for every timepoint
            def subjectId = subjectMapping[requiredSubjectName]
            def timepointsAvailable = sampleMap[subjectId].keySet()
            
            // First check whether there are duplicates for a single timepoint. If so, the system cannot distinguish
            def duplicates = sampleMap[subjectId].findAll{ it.key in uniqueTimepoints && it.value.size() > 1 }
            if( duplicates ) {
                errors << new ImportValidationError(
                    code: 13,
                    message: "Multiple samples were found for subject for subject '" + requiredSubjectName + "' and timepoints " + duplicates.keySet().collect { new RelTime(it) }.join( ", " ) + ". The importer cannot distinguish between those samples. Importing the data will lead to undetermined behavior.",
                    line: lineNr,
                )
            }
            
            // Check whether all measurements are given
            metadata.measurementColumns.each { columnIndex, feature ->
                def timepoint = metadata.timepointColumns[columnIndex]
                
                // Skip column if no timepoint is present, as there has been a warning about that already
                if(!timepoint)
                    return
                
                def hasSampleForTimepoint = timepoint && timepointsAvailable.contains(timepoint.value)
                def valueIsValid = isValue(line[columnIndex])
                
                if( valueIsValid && !hasSampleForTimepoint ) {
                    errors << new ImportValidationError(
                        code: 12,
                        message: "No sample was found for subject '" + requiredSubjectName + "' and timepoint '" + timepoint + "'",
                        line: lineNr,
                        column: columnIndex
                    )
                } else if( !valueIsValid && hasSampleForTimepoint) {
                    errors << new ImportValidationError(
                        code: 7,
                        message: "No measurement given for subject '" + requiredSubjectName + "', timepoint '" + timepoint + "' and feature '" + feature.name + "'",
                        line: lineNr,
                        column: columnIndex
                    )
                }
            }
        }
        
        return !errors
    }
    
    /**
     * Imports provided data. This method should skip objects that fail validation
     * but store the validation errors.
     * @param   data            Matrix (List of lists) with the data that has been loaded from the excel/csv file
     * @param   parameters      Parameters provided by the user. This map includes keys:
     *                            upload        Parameters about the uploaded file. Should not be needed, as the file has been parsed already
     *                            parameter     Refers to a map with parameter values for the parameters needed by the importer
     * @return  True if all objects were imported succesfully,
     *          false if the validation on any of the object has failed
     */
    public boolean importData(def data, def mapping, def parameters) {
        resetValidationErrors()
        
        def metadata = getMetadataFromData(data, mapping, parameters)
        def subjectMapping = getSubjectMapping(parameters.assay as Long)
        
        // Create a list of samples to be used, grouped by subjectName and timepoint
        def samples = getSamples( subjectMapping.values(), metadata, parameters.assay as Long)
        def groupedSamples = samples.groupBy({it[1]}, {it[2]})
        
        // Retrieve some data to use for importing
        def assay = Assay.get(parameters.assay)
        
        // Determine all samples for which we don't have a samSample
        def samplesWithoutSamSample= Sample.executeQuery( "SELECT sample FROM Assay assay INNER JOIN assay.samples sample WHERE assay.id = :assayId AND NOT EXISTS( FROM SAMSample samsample WHERE samsample.parentSample = sample AND samsample.parentAssay = assay)", [ assayId: assay.id ])
        
        log.debug "# samples without samsample for assay " + assay + " : " + samplesWithoutSamSample.size()
        samplesWithoutSamSample.each { sample ->
            def samSample = new SAMSample(parentSample: sample, parentAssay: assay)
            samSample.save()
        }
        
        // Retrieve all samsamples
        def samSampleData = SAMSample.executeQuery( "SELECT samsample.id, sample.id from SAMSample samsample INNER JOIN samsample.parentSample sample WHERE samsample.parentAssay = :assay", [ assay: assay ] )
        
        // Create a map of samSamples by sample name
        def samSampleMap = samSampleData.groupBy { it[1] }.collectEntries { k, v -> [ (k): v[0][0] ] }
        
        // Now loop through each line and try to import the data.
        def sql = new Sql(dataSource)
        try {
            def sample 
            def samSample
            
            // Start a SQL batch statement
            sql.withBatch( 250, "INSERT INTO measurement (id, version, comments, feature_id, sample_id, value) VALUES (nextval('hibernate_sequence'), 0, :comments, :featureId, :sampleId, :value)" ) { preparedStatement ->
                
                // The first two lines are not used as measurements, so these lines are skipped
                for( def lineNr = 2; lineNr < data.size(); lineNr++) {
                    def line = data[lineNr]

                    // Check subject name
                    def requiredSubjectName = line[metadata.subjectColumn].toString()
                    
                    if( !subjectMapping.containsKey(requiredSubjectName) ) {
                        errors << new ImportValidationError(
                            code: 6,
                            message: "A subject with subject name '" + requiredSubjectName + "' could not be found in the specified assay.",
                            line: lineNr,
                            column: metadata.subjectColumn
                        )
                        
                        // Without a subject, we can't check the sample existence at all, so we'll continue
                        continue
                    }
                    
                    // Now import each measurement
                    metadata.measurementColumns.each { columnIndex, feature ->
                        def value = line[columnIndex] 
                        
                        // Now find the appropriate sample object
                        def timepoint = metadata.timepointColumns[columnIndex] 
                        def subjectId = subjectMapping[requiredSubjectName]
                        def sampleId = groupedSamples[subjectId][timepoint?.value]?.get(0)?.getAt(0)
    
                        // If no timepoint was found for this column, the user has been warned before. Skipping this cell
                        if( !timepoint ) {
                            return
                        }
                        
                        // If we have a sample, we know that there is also a SAMSample object
                        // as we have created it before. If there is no sample for this subject/timepoint combination
                        // and the user wants to import data here, return an error
                        if( value && !sampleId ) {
                            errors << new ImportValidationError(
                                code: 12,
                                message: "No sample was found for subject '" + requiredSubjectName + "' and timepoint '" + timepoint + ".",
                                line: lineNr,
                                column: columnIndex
                            )
                            
                            return
                        } else if( !isValue(value) && sampleId) {
                            errors << new ImportValidationError(
                                code: 7,
                                message: "No measurement given for subject '" + requiredSubjectName + "', timepoint '" + timepoint + "' and feature '" + feature.name + "'.",
                                line: lineNr,
                                column: columnIndex
                            )
                            return
                        } else if( !isValue(value) && !sampleId ) {
                            // No value and no sample found
                            return
                        }
                        
                        def samSampleId = samSampleMap[sampleId]
                        
                        // Convert the value to a number, if necessary
                        if( value instanceof String && value.isDouble() ) {
                            value = value.toDouble()
                        }

                        // Distinguish between numeric and text values
                        if (value instanceof Double || value instanceof Integer) {
                            preparedStatement.addBatch( [featureId: feature.id, sampleId: samSampleId, value: value ] )
                        }
                        else if (value instanceof String) {
                            preparedStatement.addBatch( [featureId: feature.id, sampleId: samSampleId, comments: value ] )
                        }
                        else {
                            errors << new ImportValidationError(
                                code: 8,
                                message: "Invalid measurement given for subject '" + requiredSubjectName + "', timepoint '" + timepoint + "' and feature '" + feature.name + "': " + value + ".",
                                line: lineNr,
                                column: columnIndex
                            )
                        }
                    }
                }
                
            } // withBatch
        } catch( Exception e ) {
            log.error "An exception occurred while importing measurements", e
            errors << new ImportValidationError(
                code: 9,
                message: "An error occurred while importing your data: " + e.getMessage(),
            )
            return false
        } finally {
            if( sql )
                sql.close()
        }
        
        // Return true if no errors were found, false otherwise
        return !errors
    }
    
    /**
     * Retrieves metadata from the provided data
     * 
     * Also checks for the validity of the data. Errors could be given in the 
     * errors property
     * 
     * @return Map
     * @see errors
     */
    protected def getMetadataFromData(def data, def mapping, def parameters) {
        if( data.size() < 3 ) {
            errors << new ImportValidationError(
                code: 8,
                message: "To use subject layout, at least 3 rows are required: a header row with feature names, a header row with timepoints and 1 or more rows with data."
            )
        }
                
        // Determine subject and measurement columns
        def subjectColumn = null
        def measurementColumns = [:]
        def timepointColumns = [:]
        
        mapping.each { columnIndex, fieldMapping ->
            if( fieldMapping?.field?.id == "subject" )
                subjectColumn = columnIndex.toInteger()
            else if( fieldMapping?.field?.id )
                measurementColumns[ columnIndex.toInteger() ] = Feature.get(fieldMapping.field.id.toLong())
        }
        
        // Determine the timepoints in the second row for each mapped column
        measurementColumns.each { columnIndex, feature ->
            def givenTimepoint = data[1][columnIndex]
            
            if( !givenTimepoint ) {
                errors << new ImportValidationError(
                    code: 9,
                    message: "Each mapped column should have a timepoint in the second row, to distinguish between multiple samples for a subject.",
                    line: 1,
                    column: columnIndex
                )
                
                return  // continue looping
            }
            
            // Try to parse the time
            try {
                if( givenTimepoint instanceof Double || givenTimepoint instanceof Float ) {
                    givenTimepoint = givenTimepoint.toLong()
                }
                
                def reltime = new RelTime(givenTimepoint)
                timepointColumns[columnIndex] = reltime
            } catch( IllegalArgumentException e ) {
                errors << new ImportValidationError(
                    code: 10,
                    message: "The timepoint '" + givenTimepoint + "' is invalid. Please use the format '#w #d #h #m #s'",
                    line: 1,
                    column: columnIndex
                )
                
                return  // continue looping
            } catch( Exception e ) {
                errors << new ImportValidationError(
                    code: 15,
                    message: "The timepoint '" + givenTimepoint + "' is invalid. Please use the format '#w #d #h #m #s'",
                    line: 1,
                    column: columnIndex
                )
                
                return  // continue looping
            }
        }
        
        // Check whether a sample column and at least one measurement column is chosen
        if( subjectColumn == null ) {
            errors << new ImportValidationError(
                code: 4,
                message: "A column with the subject names is required to import data."
            )
        }
        
        if( !measurementColumns ) {
            errors << new ImportValidationError(
                code: 5,
                message: "Please select at least one column with measurements to import"
            )
        }

        // Return the data
        [
            subjectColumn: subjectColumn,
            measurementColumns: measurementColumns,
            timepointColumns: timepointColumns
        ]
    }

    /**
     * Returns a mapping from subjectname to subject id for the  given assay    
     */
    protected def getSubjectMapping(assayId) {
        // Afterwards, check for each line if the subject could be found and a measurement is given for each column
        def subjectData = Subject.executeQuery( "SELECT subject.id, subject.name FROM Subject subject WHERE EXISTS(FROM Assay assay JOIN assay.samples as sample WHERE assay.id = :assayId AND sample.parentSubject = subject)", [ assayId: assayId ] )
        
        // Create mapping from name to ID
        def subjectMapping = subjectData.collectEntries { [ (it[1]): it[0] ] }
        
        if( !subjectMapping ) {
            errors << new ImportValidationError(
                code: 11,
                message: "The selected assay doesn't have samples with associated subject. Please review the study design.",
                line: 0,
            )
        }
        

        subjectMapping
    }
    
    /**
     * Returns a list of data with each value being a list with (sampleId, subjectId, timepoint) 
     */
    def getSamples( def subjectIds, def metadata, def assayId ) {
        // Create a list of samples to be used, grouped by subjectName and timepoint
        Sample.executeQuery(
            "SELECT sample.id, sample.parentSubject.id, sample.parentSubjectEventGroup.startTime + sample.parentEvent.startTime " +
                 "FROM Assay assay INNER JOIN assay.samples sample " +
                 "WHERE assay.id = :assayId",
            [assayId: assayId ]
        )
    }
    
    protected boolean isValue(def value) {
        def noValue = ( value == null || ( value instanceof String && value == "" ) )
        !noValue
    }

}
