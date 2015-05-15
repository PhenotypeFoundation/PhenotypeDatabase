package dbnp.export

import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.*
import org.apache.poi.hssf.usermodel.*
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.DataFormatter

import dbnp.studycapturing.*
import org.dbnp.gdt.*
import dbnp.authentication.SecUser


/**
 * Exporter to export a single study to a SimpleTox excel file
 */
public class SimpleToxExporter implements Exporter {
    /**
     * SecUser that is used for authorization
     */
    SecUser user

    /**
     * Returns an identifier that describes this export
     */
    public String getIdentifier() { "SimpleTox" }
    
    /**
     * Returns the type of entitites to export. Could be Study or Assay
     */
    public String getType() { "Study" }
    
    /**
     * Returns whether this exporter supports exporting multiple entities at once
     * If so, the class should have a proper implementation of the exportMultiple method
     */
    public boolean supportsMultiple() { false }
    
    /**
     * Exports multiple entities to the outputstream
     */
    public void exportMultiple( def entities, OutputStream out ) { throw new UnsupportedOperationException( getIdentifier() + " exporter can not export multiple entities" ) }

    /**
     * Returns the content type for the export
     */
    public String getContentType( def entity ) {
        return "application/vnd.ms-excel"
    }

    /**
     * Returns a proper filename for the given entity
     */
    public String getFilenameFor( def study ) {
        return "" + study.title + "_SimpleTox.xls"
    }
    
    /**
     * Export a single entity to the outputstream in SimpleTox format
     */
    public void export( def studyInstance, OutputStream outStream ) {
        // the attributes list for the SimpleTox format
        def attributes_list = ["SubjectID","DataFile","HybName","SampleName","ArrayType","Label","StudyTitle","Array_ID",
            "Species"]

        // The first row contains the attributes names
        HSSFWorkbook wb = new HSSFWorkbook()
        HSSFSheet sheet = wb.createSheet()
        HSSFRow row     = sheet.createRow((short)0)
        for (i in 0..attributes_list.size()){
            row.createCell((short)i).setCellValue(attributes_list[i])
        }

        // Adding the next lines
        for (s in 1..studyInstance.samples.size()){
            // creating new line for every sample
            HSSFRow sub     = sheet.createRow((short)s)
            def sample = studyInstance.samples.getAt(s-1)

            writeMandatoryFields(sub,sample,studyInstance)

            try {
                // adding the subject domain + template properties
                writeSubjectProperties(sub,sample,row)

                // adding the samplingEvent domain + template properties
                writeSamplingEventProperties(sub,sample,row)

                // adding EventGroup domain + template properties
                //                writeEventGroupProperties(sub,sample,rows)

                // adding Sample domain + template properties
                writeSampleProperties(sub,sample,row)
            }
            catch (Exception e){
                //println "Error adding properties"
            }
        }

        wb.write( outStream );
    }

    private def writeMandatoryFields(sub,sample,study) {
        // adding subject name in row 1
        sample.parentSubject ? sub.createCell((short)0).setCellValue(sample.parentSubject.name) : "not defined"
        // adding sample in row 4
        sample.name!=null ? sub.createCell((short)3).setCellValue(sample.name) : "not defined"

        // adding label (EventGroup) in row 6
        if( sample.parentEvent ) {
            sub.createCell((short)5).setCellValue(sample.parentEvent.eventGroup.name)
        } else if( sample.parentSubjectEventGroup ) {
            sub.createCell((short)5).setCellValue(sample.parentSubjectEventGroup.eventGroup.name)
        } else {
            sub.createCell((short)5).setCellValue(" ")
        }

        // adding study title in row 7
        sub.createCell((short)6).setCellValue(study.title)
        // Species row 9
        //        sample.parentSubject.species.name!=null ? sub.createCell((short)8).setCellValue(sample.parentSubject.species.name) : "not defined"
        sample.parentSubject ? sub.createCell((short)8).setCellValue(sample.parentSubject.species.name) : "not defined"
    }

    // writing subject properties
    private def writeSubjectProperties(sub,sample,row) {
        if( sample.parentSubject ) {
            log.trace "----- SUBJECT -----"
            for (u in 0..sample.parentSubject.giveFields().unique().size()-1){
                TemplateField tf = sample.parentSubject.giveFields().getAt(u)
                log.trace tf.name
                row.createCell((short)9+u).setCellValue(tf.name)
                sample.parentSubject.getFieldValue(tf.name) ? sub.createCell((short)9+u).setCellValue(sample.parentSubject.getFieldValue(tf.name).toString()) : "not define"
            }
        } else {
            log.trace "------ NO SUBJECT FOR SAMPLE " + sample.name + "-----";
        }
    }

    // writing samplingEvent properties
    private def writeSamplingEventProperties(sub,sample,row){
        if( sample.parentEvent ) {
            log.trace "----- SAMPLING EVENT -----"
            for (t in 0..sample.parentEvent.event.giveFields().unique().size()-1){
                TemplateField tf =sample.parentEvent.event.giveFields().getAt(t)
                log.trace tf.name
                row.createCell((short)9+sample.parentSubject.giveFields().unique().size()+t).setCellValue("samplingEvent-"+tf.name)
                sample.parentEvent.event.getFieldValue(tf.name) ? sub.createCell((short)9+sample.parentSubject.giveFields().unique().size()+t).setCellValue(sample.parentEvent.event.getFieldValue(tf.name).toString()) : "not define"
            }
        } else {
            log.trace "------ NO SAMPLING EVENT FOR SAMPLE " + sample.name + "-----";
        }
    }

    // writing EventGroup properties
    private def writeEventGroupProperties(sub,sample,row){

    }

    // writing sample properties
    private def writeSampleProperties(sub,sample,row){
        log.trace "----- SAMPLE -----"
        for (v in 0..sample.giveFields().unique().size()-1){
            TemplateField tf =sample.giveFields().getAt(v)
            log.trace tf.name
            row.createCell((short)9+sample.parentSubject.giveFields().unique().size()+v+sample.parentEvent.event.giveFields().unique().size()).setCellValue("sample-"+tf.name)
            sample.getFieldValue(tf.name) ? sub.createCell((short)9+sample.parentSubject.giveFields().unique().size()+v+sample.parentEvent.event.giveFields().unique().size()).setCellValue(sample.getFieldValue(tf.name).toString()) : "not define"
        }
    }
    
}