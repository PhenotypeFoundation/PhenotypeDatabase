package dbnp.export

import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.*
import org.apache.poi.hssf.usermodel.*
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.DataFormatter

import grails.util.Holders
import org.codehaus.groovy.runtime.typehandling.GroovyCastException

import dbnp.studycapturing.*
import org.dbnp.gdt.*
import dbnp.authentication.SecUser


/**
 * This exporter allows exporting assay data to an excel file. 
 * Multiple assays are supported, although all assays are supposed
 * to belong to the same study 
 */
public class AssayDataExporter implements Exporter {
    /**
     * SecUser that is used for authorization
     */
    SecUser user

    /**
     * Map with export parameters. Can be overridden using the setParameters call
     */
    Map exportParameters = [
        'decimal': '.'
    ]
    
    /**
     * Returns an identifier that describes this export
     */
    public String getIdentifier() { "AssayData" }

    /**
     * Returns the type of entitites to export. Could be Study or Assay
     */
    public String getType() { "Assay" }

    /**
     * Returns whether this exporter supports exporting multiple entities at once
     * If so, the class should have a proper implementation of the exportMultiple method
     */
    public boolean supportsMultiple() { true }

    /**
     * Exports multiple entities to the outputstream
     */
    public void exportMultiple( def assays, OutputStream out ) {
        def rowData = collectAssayData(assays)
        def outputDelimiter = "\t"

        def assayService = Holders.grailsApplication.getMainContext().getBean("assayService")
        
        // Use the parameters
        def exportLocale
        switch( exportParameters.decimal ) {
            case ',': exportLocale = new java.util.Locale( "nl" ); break;
            case '.':
            default: exportLocale = java.util.Locale.US; break;
        }
        
        assayService.exportRowWiseDataToCSVFile(rowData, out, outputDelimiter, exportLocale)
    }

    /**
     * Returns the content type for the export
     */
    public String getContentType( def entity ) {
        return "text/tab-separated-values"
    }

    /**
     * Returns a proper filename for the given entity
     */
    public String getFilenameFor( def entity ) {
        if( entity instanceof Collection ) {
            return "multiple_assays.tsv"
        } else {
            return entity.name + ".tsv"
        }
    }

    /**
     * Export a single entity to the outputstream in SimpleTox format
     */
    public void export( def assay, OutputStream outStream ) {
        exportMultiple( [assay], outStream )
    }


    def collectAssayData(assays) {
        def ctx = Holders.grailsApplication.getMainContext()
        def assayService = ctx.getBean("assayService")
        def apiService = ctx.getBean("apiService")

        // collect the assay data according to user selection
        def data = []

        // Determine associated IDs
        def ids = [:]
        
        // Determine the fields to export
        def fieldMaps = assays.collect { assay ->
            ids[assay.id] = assayService.getAssociatedIds(assay, assay.samples)
            assayService.collectAssayTemplateFields(assay, ids[assay.id]) 
        }
        def fieldMap = assayService.mergeFieldMaps( fieldMaps )

        // Extract the features, as they are not needed in the rest of the calculations
        def features = fieldMap.remove( 'Features' )
        
        println "Start finding samples for the given assay"
        
        // Get the samples and sort them; this will be the sort order to use for
        // both retrieving the assay data and the measurements
        def firstAssay = assays[0]
        def samples = firstAssay.samples.toList().sort({it.name})

        println "Start collecting actual assay data"
        
        // First retrieve the subject/sample/event/assay data from GSCF, as it is the same for each list
        data = assayService.collectAssayData(firstAssay, fieldMap, samples, ids[firstAssay.id] )

        assays.each{ assay ->
            def moduleMeasurementData
            println "Collecting data for " + assay
            try {
                moduleMeasurementData = apiService.getMeasurementData(assay, user).sort()
                data[ "Module Measurement Data: " + assay.name ] = apiService.organizeSampleMeasurements((Map)moduleMeasurementData, samples)
            } catch (e) {
                moduleMeasurementData = ['error' : [
                        'Module error, module not available or unknown assay']
                    * samples.size() ]
                e.printStackTrace()
            }
            
            println "Finished collecting data for " + assay
        }
        
        println "  Collected assay data - start converting to columns"
        
        // Convert the data into a proper structure
        def rowStructuredData = assayService.convertColumnToRowStructure(data)
        
        println "  Add feature metadata"
        // Add feature data to the structure
        assayService.addFeatureMetadata( rowStructuredData, features )
    }
    
    /**
     * Use the given parameters for exporting
     * 
     * Please note: only the parameters for which a default is already set in this file are used
     */
    public void setParameters(def parameters) {
        exportParameters.each { k, v ->
            if( parameters.containsKey(k) )
                exportParameters[k] = parameters[k]
        }
    }

}
