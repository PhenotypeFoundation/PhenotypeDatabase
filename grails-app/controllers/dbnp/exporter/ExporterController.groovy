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

    /* 
     * the export method will create a SimpleTox format for the selected study
     */
    def export = {
        //def studyInstance
        def studies = []

        // the attributes list for the SimpleTox format
        def attributes_list = ["SubjectID","DataFile","HybName","SampleName","ArrayType","Label","StudyTitle","Array_ID",
        "Species"]

        // Get the selected study
        for ( j in dbnp.studycapturing.Study.list() ){
            if (params.containsKey(j.title)){
//                studyInstance = j
                    studies.add(j)
            }
        }

//        def studies = params*.key.collect{ Study.findByTitle(it) }

        println "STUDIES : "+studies

        for (studyInstance in studies) {


            if (studyInstance!=null){

                println "StudyInstance :" +studyInstance

        HSSFWorkbook wb = new HSSFWorkbook()
        println " WORKBOOK : "+wb
        // The first row contains the attributes names
        HSSFSheet sheet = wb.createSheet()
        HSSFRow row     = sheet.createRow((short)0)
        for (i in 0..attributes_list.size()){
            row.createCell((short)i).setCellValue(attributes_list[i])
        }

        // Adding the next lines
        for (s in 1..studyInstance.samples.size()){
            try {
            // creating new line for every sample
            HSSFRow sub     = sheet.createRow((short)s)
            def sample = studyInstance.samples.getAt(s-1)
            
            writeMandatoryFields(sub,sample,studyInstance)

            // adding the subject domain + template properties
            writeSubjectProperties(sub,sample,row)

            // adding the samplingEvent domain + template properties
            writeSamplingEventProperties(sub,sample,row)
            
            // adding samples domain + template properties
            TemplateField sf = sample.giveFields().getAt(s)
            //println studyInstance.samples.getAt(s-1).getFieldValue(sf.name)

            // adding Event domaine + template properties

            }
            catch (Exception e){
                println "Error creating file"
            }
        }

        // Make the file downloadable
        response.setHeader("Content-disposition", "attachment;filename=\"${studyInstance.title}_SimpleTox.xls\"")
        response.setContentType("application/octet-stream")
        wb.write(response.outputStream)
        response.outputStream.close()
            }
        }
    }

    def writeMandatoryFields(sub,sample,study) {

        try {
        // adding subject name in row 1
        sub.createCell((short)0).setCellValue(sample.parentSubject.name)
        // adding sample in row 4
        sub.createCell((short)3).setCellValue(sample.name)
        // adding label (EventGroup) in row 6
        for (ev in EventGroup.list()){
            if (ev.subjects.name.contains(sample.parentSubject.name)) {
                sub.createCell((short)5).setCellValue(ev.name)
                break
            }
            else {
                sub.createCell((short)5).setCellValue(" ")
            }
        }
        // adding study title in row 7
        sub.createCell((short)6).setCellValue(study.title)
        // Species row 9
        sub.createCell((short)8).setCellValue(sample.parentSubject.species.name)
        }
        catch (Exception e){
            println "Error during Mandatory Fields"
        }
    }

    // writing subject properties
    def writeSubjectProperties(sub,sample,row) {
        try {
        for (u in 0..sample.parentSubject.giveFields().unique().size()-1){
            TemplateField tf = sample.parentSubject.giveFields().getAt(u)
            row.createCell((short)9+u).setCellValue(tf.name)
            sample.parentSubject.getFieldValue(tf.name) ? sub.createCell((short)9+u).setCellValue(sample.parentSubject.getFieldValue(tf.name).toString()) : "not define"
        }
        }
        catch (Exception e){
            println "Error during Subject Properties"
        }
    }

    // writing samplingEvent properties
    def writeSamplingEventProperties(sub,sample,row){
        try {
        for (t in 0..sample.parentEvent.giveFields().unique().size()-1){
            TemplateField tf =sample.parentEvent.giveFields().getAt(t)
            row.createCell((short)9+sample.parentSubject.giveFields().unique().size()+t).setCellValue(tf.name)
            sample.parentEvent.getFieldValue(tf.name) ? sub.createCell((short)9+sample.parentSubject.giveFields().unique().size()+t).setCellValue(sample.parentEvent.getFieldValue(tf.name).toString()) : "not define"
        }
        }
        catch (Exception e) {
            println "Error during Sampling Event properties"
        }
    }
 
}
