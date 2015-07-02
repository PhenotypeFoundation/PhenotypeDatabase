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
        assayService.exportRowWiseDataToCSVFile(rowData, out, outputDelimiter, java.util.Locale.US)
    }

    /**
     * Returns the content type for the export
     */
    public String getContentType( def entity ) {
        return "application/vnd.ms-excel"
    }

    /**
     * Returns a proper filename for the given entity
     */
    public String getFilenameFor( def entity ) {
        if( entity instanceof Collection ) {
            return "multiple_assays.xls"
        } else {
            return entity.name + ".xls"
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
        
        // Determine the fields to export
        def fieldMaps = assays.collect { assay -> assayService.collectAssayTemplateFields(assay, null) }
        def fieldMap = assayService.mergeFieldMaps( fieldMaps )

        // Get the samples and sort them; this will be the sort order to use for
        // both retrieving the assay data and the measurements
        def samples = assays[0].samples.toList().sort({it.name})

        // First retrieve the subject/sample/event/assay data from GSCF, as it is the same for each list
        data = assayService.collectAssayData(assays[0], fieldMap, [], samples)
        
        assays.each{ assay ->
            def moduleMeasurementData

            try {
                moduleMeasurementData = apiService.getPlainMeasurementData(assay, user)
                data[ "Module measurement data: " + assay.name ] = apiService.organizeSampleMeasurements((Map)moduleMeasurementData, samples)
            } catch (GroovyCastException gce) {
                //This module probably does not support the 'getPlainMeasurementData' method, try it the old way.
                moduleMeasurementData = assayService.requestModuleMeasurements(assay, [], samples)
                data[ "Module measurement data: " + assay.name ] = moduleMeasurementData
            } catch (e) {
                moduleMeasurementData = ['error' : [
                        'Module error, module not available or unknown assay']
                    * samples.size() ]
                e.printStackTrace()
            }
        }

        assayService.convertColumnToRowStructure(data)
    }

}
