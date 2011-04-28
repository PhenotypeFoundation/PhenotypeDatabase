/**
 * ExporterController Controller
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
import org.dbnp.gdt.*

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

    def authenticationService

    /*
     * List of all studies for selection the study to export
     * Using the same code as 'list' into StudyController
     */
    def index = {

        def user = authenticationService.getLoggedInUser()
        def max = Math.min(params.max ? params.int('max') : 10, 100)

        def c = dbnp.studycapturing.Study.createCriteria()

        def studies = Study.giveReadableStudies(user, max);
        [studyInstanceList: studies, studyInstanceTotal: studies.count()]
    }

    def export = {
		def ids = params.list( 'ids' );
        def studies = []
		
		ids.each {
			if( it.toString().isLong() ) {
				def study = Study.get( Long.valueOf( it ) );
				if( study )
					studies << study
			}
		}
        
        if(studies.size()>1){

			// Send the right headers for the zip file to be downloaded
			response.setContentType( "application/zip" ) ;
			response.addHeader( "Content-Disposition", "attachment; filename=\"GSCF_SimpleToxStudies.zip\"" ) ;

			// Create a ZIP file containing all the SimpleTox files
			ZipOutputStream zipFile = new ZipOutputStream( new BufferedOutputStream( response.getOutputStream() ) );
			BufferedWriter zipWriter = new BufferedWriter( new OutputStreamWriter( zipFile ) );
			
			// Loop through the given studies and export them
			for (studyInstance in studies){
				if( studyInstance.samples?.size() ) {
					try {
						zipFile.putNextEntry( new ZipEntry( studyInstance.title + "_SimpleTox.xls" ));
						downloadFile(studyInstance, zipFile);
						zipWriter.flush();
						zipFile.closeEntry();
					} catch( Exception e ) {
						log.error "Error while writing excelfile for zip for study " + studyInstance?.title + ": " + e.getMessage();
					} finally {
						// Always close zip entry
						try {
							zipWriter.flush();
							zipFile.closeEntry();
						} catch( Exception e ) {
							log.error "Error while closing excelfile for zip for study: " + e.getMessage();
						}
					}
				} else {
					log.trace "Study " + studyInstance?.title + " doesn't contain any samples, so is not exported to simpleTox"
					
					// Add a text file with explanation in the zip file
					zipFile.putNextEntry(new ZipEntry( studyInstance.title + "_contains_no_samples.txt" ) );
					zipFile.closeEntry();
				}
			}
			
			// Close zipfile and flush to the user
			zipFile.close();
			response.outputStream.flush();
			
        } else {
            def studyInstance = studies.getAt(0)
            // make the file downloadable
            if ((studyInstance!=null) && (studyInstance.samples.size()>0)){
	            response.setHeader("Content-disposition", "attachment;filename=\"${studyInstance.title}_SimpleTox.xls\"")
	            response.setContentType("application/octet-stream")
                downloadFile(studyInstance, response.getOutputStream())
				response.getOutputStream().close()
            } else if( studyInstance.samples.size() == 0 ) {
				flash.message = "Given study doesn't contain any samples, so no excel file is created. Please choose another study.";
				redirect( action: 'index' );
			}
            else {
                flash.message= "Error while exporting the file, please try again or choose another study."
                redirect( action: 'index' )
            }

        }

    }
    /* 
     * the export method will create a SimpleTox format for the selected study 
     * and write the file to the given output stream
     */
    def downloadFile(studyInstance, OutputStream outStream) {
        // the attributes list for the SimpleTox format
        def attributes_list = ["SubjectID","DataFile","HybName","SampleName","ArrayType","Label","StudyTitle","Array_ID",
        "Species"]
        //println studyInstance.samples.size()
        //println "StudyInstance :" + studyInstance
                
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
//        sample.parentSubject.species.name!=null ? sub.createCell((short)8).setCellValue(sample.parentSubject.species.name) : "not defined"
        sample.parentSubject ? sub.createCell((short)8).setCellValue(sample.parentSubject.species.name) : "not defined"
    }

    // writing subject properties
    def writeSubjectProperties(sub,sample,row) {
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
    def writeSamplingEventProperties(sub,sample,row){
		if( sample.parentEvent ) {
	        log.trace "----- SAMPLING EVENT -----"
	        for (t in 0..sample.parentEvent.giveFields().unique().size()-1){
	            TemplateField tf =sample.parentEvent.giveFields().getAt(t)
	            log.trace tf.name
	            row.createCell((short)9+sample.parentSubject.giveFields().unique().size()+t).setCellValue("samplingEvent-"+tf.name)
	            sample.parentEvent.getFieldValue(tf.name) ? sub.createCell((short)9+sample.parentSubject.giveFields().unique().size()+t).setCellValue(sample.parentEvent.getFieldValue(tf.name).toString()) : "not define"
	        }
		} else {
			log.trace "------ NO SAMPLING EVENT FOR SAMPLE " + sample.name + "-----";
		}
    }

    // writing EventGroup properties
    def writeEventGroupProperties(sub,sample,row){
      
    }

    // writing sample properties
    def writeSampleProperties(sub,sample,row){
        log.trace "----- SAMPLE -----"
        for (v in 0..sample.giveFields().unique().size()-1){
            TemplateField tf =sample.giveFields().getAt(v)
            log.trace tf.name
            row.createCell((short)9+sample.parentSubject.giveFields().unique().size()+v+sample.parentEvent.giveFields().unique().size()).setCellValue("sample-"+tf.name)
            sample.getFieldValue(tf.name) ? sub.createCell((short)9+sample.parentSubject.giveFields().unique().size()+v+sample.parentEvent.giveFields().unique().size()).setCellValue(sample.getFieldValue(tf.name).toString()) : "not define"
        }
    }
}
