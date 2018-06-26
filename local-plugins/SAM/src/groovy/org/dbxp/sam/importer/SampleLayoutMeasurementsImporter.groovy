package org.dbxp.sam.importer

import dbnp.importer.*
import org.dbxp.sam.*
import org.dbnp.gdt.AssayModule
import dbnp.studycapturing.Assay
import dbnp.studycapturing.Sample
import groovy.sql.Sql

import grails.util.Holders

/**
 * Defines the interface for an exporter
 */
public class SampleLayoutMeasurementsImporter extends AbstractImporter {
    def dataSource = Holders.grailsApplication.mainContext.getBean('dataSource')
     
    /**
     * Returns an identifier that describes this importer
     */
    public String getIdentifier() {
        "Measurements (sample layout)"
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
            [ id: 'sample', name: "Sample name" ]
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
        
        // Check if the right columns are selected
        def mappedFields = mapping.values()
        
        // Determine sample and measurement columns
        def sampleColumn = null
        def measurementColumns = [:]
        
        mapping.each { columnIndex, fieldMapping ->
            if( fieldMapping?.field?.id == "sample" ) 
                sampleColumn = columnIndex.toInteger()
            else if( fieldMapping?.field?.id )
                measurementColumns[ columnIndex.toInteger() ] = Feature.get(fieldMapping.field.id.toLong())
        }
        
        // Check whether a sample column and at least one measurement column is chosen 
        if( sampleColumn == null ) {
            errors << new ImportValidationError(
                code: 4,
                message: "A column with the sample names is required to import data."
            )
        }
        
        if( !measurementColumns ) {
            errors << new ImportValidationError(
                code: 5,
                message: "Please select at least one column with measurements to import"
            )
        }

        // If we ran into errors already, there is no need to continue
        if( errors ) {
            return false
        }
             
        // Afterwards, check for each line if the sample could be found and a measurement is given for each column
        def assay = Assay.get(parameters.assay)
        def sampleNames = Sample.executeQuery( "SELECT sample.name FROM Assay assay INNER JOIN assay.samples sample WHERE assay.id = :assayId", [ assayId: assay.id ])
        
        // The header line is not used as measurements, so the first line is skipped
        for( def lineNr = 1; lineNr < data.size(); lineNr++) {
            def line = data[lineNr]
            
            // Check sample name
            def requiredSampleName = line[sampleColumn]
            
            if( !sampleNames.contains(requiredSampleName) ) {
                errors << new ImportValidationError(
                    code: 6,
                    message: "A sample with sample name '" + requiredSampleName + "' could not be found in the specified assay.",
                    line: lineNr,
                    column: sampleColumn
                )
            }
            
            // Check whether all measurements are given
            measurementColumns.each { columnIndex, feature ->
                def value = line[columnIndex]
                if( !isValue(value) ) {
                    errors << new ImportValidationError(
                        code: 7,
                        message: "No measurement given for sample '" + requiredSampleName + "' and feature '" + feature.name + "'.",
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

        // Check if the right columns are selected
        def mappedFields = mapping.values()
        
        // Determine sample and measurement columns
        def sampleColumn = null
        def measurementColumns = [:]
        
        mapping.each { columnIndex, fieldMapping ->
            if( fieldMapping?.field?.id == "sample" )
                sampleColumn = columnIndex.toInteger()
            else if( fieldMapping?.field?.id )
                measurementColumns[ columnIndex.toInteger() ] = Feature.get(fieldMapping.field.id.toLong())
        }
        
        // Check whether a sample column and at least one measurement column is chosen
        if( sampleColumn == null ) {
            errors << new ImportValidationError(
                code: 4,
                message: "A column with the sample names is required to import data."
            )
        }
        
        if( !measurementColumns ) {
            errors << new ImportValidationError(
                code: 5,
                message: "Please select at least one column with measurements to import"
            )
        }

        // If we ran into errors already, there is no need to continue
        if( errors ) {
            return false
        }
        
        // Retrieve Assay
        def assay = Assay.get(parameters.assay)
        
        // Retrieve all Samples for this Assay and create a map of it by name
        def sampleData = Sample.executeQuery("SELECT sample FROM Assay assay INNER JOIN assay.samples sample WHERE assay.id = :assayId", [ assayId: assay.id ])
        def sampleMap = sampleData.collectEntries { [(it.name): it] }
        
        // Retrieve all SAMSamples for this Assay and create a map of it by name
        def samSampleData = SAMSample.executeQuery( "SELECT samsample, sample.name from SAMSample samsample INNER JOIN samsample.parentSample sample WHERE samsample.parentAssay = :assay", [ assay: assay ] )
        def samSampleMap = samSampleData.collectEntries { [(it[1]): it[0]] }
        
        // Now loop through each line and try to import the data.
        def sql = new Sql(dataSource)
        try {
            def sample
            def samSample

            // Start a SQL batch statement
            sql.withBatch( 250, "INSERT INTO measurement (id, version, comments, feature_id, sample_id, value) VALUES (nextval('hibernate_sequence'), 0, :comments, :featureId, :sampleId, :value)" ) { preparedStatement ->
                
                // The header line is not used as measurements, so the first line is skipped
                for( def lineNr = 1; lineNr < data.size(); lineNr++) {
                    def line = data[lineNr]

                    // Try to look up the SAMSample by the Sample name
                    def requiredSampleName = line[sampleColumn]
                    samSample = samSampleMap[requiredSampleName]

                    if (!samSample) {

                        // No SAMSample yet, but to create it, we need the Sample
                        sample = sampleMap[requiredSampleName]
                        if (!sample) {
                            errors << new ImportValidationError(
                                    code: 6,
                                    message: "A sample with sample name '" + requiredSampleName + "' could not be found in the specified assay.",
                                    line: lineNr,
                                    column: sampleColumn
                            )

                            // Skip this line
                            continue
                        }

                        // Create a new SAMSample
                        samSample = new SAMSample(parentSample: sample, parentAssay: assay)
                        samSample.save()
                    }
                    
                    // Now import each measurement
                    measurementColumns.each { columnIndex, feature ->
                        def value = line[columnIndex] 
                        if( !isValue(value) ) {
                            errors << new ImportValidationError(
                                code: 7,
                                message: "No measurement given for sample '" + requiredSampleName + "' and feature '" + feature.name + "'.",
                                line: lineNr,
                                column: columnIndex
                            )
                            
                            // Skip this measurement
                            return
                        }
                        
                        // Convert the value to a number, if necessary
                        if( value instanceof String && value.replace(',','.').isDouble() ) {
                            value = value.replace(',','.').toDouble()
                        }

                        // Distinguish between numeric and text values
                        if (value instanceof Double || value instanceof Integer) {
                            preparedStatement.addBatch( [featureId: feature.id, sampleId: samSample.id, value: value ] )
                        }
                        else if (value instanceof String) {
                            preparedStatement.addBatch( [featureId: feature.id, sampleId: samSample.id, comments: value ] )
                        }
                        else {
                            errors << new ImportValidationError(
                                code: 8,
                                message: "Invalid measurement given for sample '" + requiredSampleName + "' and feature '" + feature.name + "': " + value + ".",
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
    
    protected boolean isValue(def value) {
        def noValue = ( value == null || ( value instanceof String && value == "" ) )
        !noValue
    }
}
