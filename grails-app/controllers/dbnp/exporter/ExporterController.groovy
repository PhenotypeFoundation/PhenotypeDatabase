/**
 * ExporterController Controler
 *
 * Description of my controller
 *
 * @author  your email (+name?)
 * @since	2010mmdd
 * @package	???
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.exporter

import dbnp.studycapturing.*

import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.*
import org.apache.poi.hssf.usermodel.*
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.DataFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream
import javax.servlet.ServletOutputStream

import grails.plugins.springsecurity.Secured

class ExporterController {

    def AuthenticationService
    def ImporterService

    /*
     * List of all studies for selection the study to export
     * Using the same code as 'list' into StudyController
     */
    def index = {

        def user = AuthenticationService.getLoggedInUser()
        def max = Math.min(params.max ? params.int('max') : 10, 100)

        def c = dbnp.studycapturing.Study.createCriteria()

        def studies
        if( user == null ) {
            studies = c.list {
                maxResults(max)
                and {
                    eq( "published", true )
                    eq( "publicstudy", true )
                }
            }
        } else {
            studies = c.list {
                maxResults(max)
                or {
                    eq( "owner", user )
                    writers {
                        eq( "id", user.id )
                    }
                    and {
                        readers {
                            eq( "id", user.id )
                        }
                        eq( "published", true )
                    }
                }
            }
        }
        [studyInstanceList: studies, studyInstanceTotal: studies.count()]
    }

    def export = {

        def studies = []
        for ( j in dbnp.studycapturing.Study.list() ){
            if (params.containsKey(j.code)){
                studies.add(j)
            }
        }
        
        if(studies.size()>1){
            // Create a ZIP file containing all the SimpleTox files
            def files = []
            for (studyInstance in studies){
                downloadFile(studyInstance,false)
                files.add(new File("web-app/fileuploads/"+studyInstance.code+"_SimpleTox.xls"))
            }

            response.setContentType( "application/zip" ) ;
            response.addHeader( "Content-Disposition", "attachment; filename=\"GSCF_SimpleToxStudies.zip\"" ) ;

            // get a ZipOutputStream, so we can zip our files together
            ZipOutputStream outZip = new ZipOutputStream( response.getOutputStream() );

            // add SimpleTox files to the zip
            for (outFiles in files){

                FileInputStream inStream = null
                try
                {
                    // Add ZIP entry to output stream.
                    outZip.putNextEntry( new ZipEntry( outFiles.getName() ) ) ;

                    inStream = new FileInputStream( outFiles )

                    // Transfer bytes from the file to the ZIP file
                    byte[] buf = new byte[ 4096 ] ;
                    int len 
                    while( ( len = inStream.read( buf ) ) > 0 )
                    {
                        outZip.write( buf, 0, len ) 
                    }
                }
                catch( Exception ex ) {  }
                finally
                {
                    // Complete the entry
                    try{ outZip.closeEntry() } catch( Exception ex ) { }
                    try{ inStream.close() } catch( Exception ex ) { }
                }
                outFiles.delete()
            }
            outZip.flush() 
            outZip.close()
        }


        else {
            def studyInstance = studies.getAt(0)
            // make the file downloadable
            if ((studyInstance!=null) && (studyInstance.samples.size()>0)){
                downloadFile(studyInstance,true)
            }
            else {
                flash.message= "Error while exporting the file, please try again or choose another file"
                redirect(action:index)
            }

        }

    }
    /* 
     * the export method will create a SimpleTox format for the selected study
     */
    def downloadFile(studyInstance, boolean dl) {
        // the attributes list for the SimpleTox format
        def attributes_list = ["SubjectID","DataFile","HybName","SampleName","ArrayType","Label","StudyTitle","Array_ID",
        "Species"]
        println studyInstance.samples.size()
        println "StudyInstance :" + studyInstance
                
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
                println "Error adding properties"
            }
        }

        // Make the file downlodable
        if(dl) {
            println "Creation for downloading the file "+studyInstance.title+"_SimpleTox.xls"
            response.setHeader("Content-disposition", "attachment;filename=\"${studyInstance.code}_SimpleTox.xls\"")
            response.setContentType("application/octet-stream")
            wb.write(response.outputStream)
            response.outputStream.close()
        }

        // Create the file and save into ZIP
        if(!dl){
            FileOutputStream fileOut = new FileOutputStream("web-app/fileuploads/"+studyInstance.code+"_SimpleTox.xls", true)
            wb.write(fileOut)
            fileOut.close()
        }
    }

    def writeMandatoryFields(sub,sample,study) {
        // adding subject name in row 1
        sample.parentSubject ? sub.createCell((short)0).setCellValue(sample.parentSubject.name) : "not defined"
        // adding sample in row 4
        sample.name!=null ? sub.createCell((short)3).setCellValue(sample.name) : "not defined"
        // adding label (EventGroup) in row 6
        for (ev in EventGroup.list()){
            if(sample.parentSubject){
                if ( (sample.parentSubject.name) && (ev.subjects.name.contains(sample.parentSubject.name))) {
                    sub.createCell((short)5).setCellValue(ev.name)
                    break
                }
                else {
                    sub.createCell((short)5).setCellValue(" ")
                }}
            else {
                sub.createCell((short)5).setCellValue(" ")
            }
        }
        // adding study title in row 7
        sub.createCell((short)6).setCellValue(study.title)
        // Species row 9
        sample.parentSubject.species.name!=null ? sub.createCell((short)8).setCellValue(sample.parentSubject.species.name) : "not defined"
    }

    // writing subject properties
    def writeSubjectProperties(sub,sample,row) {
        println "----- SUBJECT -----"
        for (u in 0..sample.parentSubject.giveFields().unique().size()-1){
            TemplateField tf = sample.parentSubject.giveFields().getAt(u)
            println tf.name
            row.createCell((short)9+u).setCellValue(tf.name)
            sample.parentSubject.getFieldValue(tf.name) ? sub.createCell((short)9+u).setCellValue(sample.parentSubject.getFieldValue(tf.name).toString()) : "not define"
        }
    }

    // writing samplingEvent properties
    def writeSamplingEventProperties(sub,sample,row){
        println "----- SAMPLING EVENT -----"
        for (t in 0..sample.parentEvent.giveFields().unique().size()-1){
            TemplateField tf =sample.parentEvent.giveFields().getAt(t)
            println tf.name
            row.createCell((short)9+sample.parentSubject.giveFields().unique().size()+t).setCellValue("samplingEvent-"+tf.name)
            sample.parentEvent.getFieldValue(tf.name) ? sub.createCell((short)9+sample.parentSubject.giveFields().unique().size()+t).setCellValue(sample.parentEvent.getFieldValue(tf.name).toString()) : "not define"
        }
    }

    // writing EventGroup properties
    def writeEventGroupProperties(sub,sample,row){
      
    }

    // writing sample properties
    def writeSampleProperties(sub,sample,row){
        println "----- SAMPLE -----"
        for (v in 0..sample.giveFields().unique().size()-1){
            TemplateField tf =sample.giveFields().getAt(v)
            println tf.name
            row.createCell((short)9+sample.parentSubject.giveFields().unique().size()+v+sample.parentEvent.giveFields().unique().size()).setCellValue("sample-"+tf.name)
            sample.getFieldValue(tf.name) ? sub.createCell((short)9+sample.parentSubject.giveFields().unique().size()+v+sample.parentEvent.giveFields().unique().size()).setCellValue(sample.getFieldValue(tf.name).toString()) : "not define"
        }
    }
}
