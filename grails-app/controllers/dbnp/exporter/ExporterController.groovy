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
import dbnp.export.*

import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class ExporterController {
    def authenticationService

    def index = {
        redirect( action: "studies" )
    }
    
    /*
     * List of all studies for selection the study to export
     * Using the same code as 'list' into StudyController
     */
    def studies = {
        def exporterFactory = new ExporterFactory()
        
        // If a type is already given, use that type
        def format = params.get('format')
        def formats = []
        if( format ) {
            if(exporterFactory.getExporter(format)?.type != "Study") {
                flash.message = "The specified output type " + format + " is not supported. Please select one of the supported output types.";
                redirect(action: 'studies')
            }
            formats = [format]
        } else {
            formats = exporterFactory.getExportersForType( "Study" )*.identifier
        }

        def user = authenticationService.getLoggedInUser()
        def max = Math.min(params.max ? params.int('max') : 10, 100)
        def offset = params.offset ? params.int( 'offset' ) : 0
        def studies = Study.giveReadableStudies(user, max, offset);

        [studyInstanceList: studies, studyInstanceTotal: Study.countReadableStudies( user ), formats: formats, format: format]
    }
    
    /*
     * List of all studies for selection the study to export
     * Using the same code as 'list' into StudyController
     */
    def assays = {
        def exporterFactory = new ExporterFactory()
        
        // If a type is already given, use that type
        def format = params.get('format')
        def formats = []
        if( format ) {
            if(exporterFactory.getExporter(format)?.type != "Assay") {
                flash.message = "The specified output type " + format + " is not supported. Please select one of the supported output types.";
                redirect(action: 'assays')
            }
            formats = [format]
        } else {
            formats = exporterFactory.getExportersForType( "Assay" )*.identifier
        }

        def user = authenticationService.getLoggedInUser()
        def studies = Study.giveReadableStudies(user);

        [studies: studies, formats: formats, format: format]
    }
    
    def exportAssays = {
        def ids = params.list( 'assayId' )
        def exportType = params.format
        def user = authenticationService.getLoggedInUser()
        def assays = []

        // Determine the exporter for the given type
        def factory = new ExporterFactory()
        def exporter = factory.getExporter(exportType, user)
        
        if( !exporter ) {
            redirect(action: 'assays', params: [errorText: "Please select a valid export type. Valid types are " + factory.getExportersForType( "Study" )*.identifier ] );
        }
        
        // Retrieve a list of studies
        ids.each {
            if( it.toString().isLong() ) {
                def assay = Assay.get( Long.valueOf( it ) );
                if( assay )
                    assays << assay
            }
        }

        // Filter on readable studies
        assays = assays.findAll { it && it.parent.canRead(user) }
        
        if( assays.size() == 0 ) {
            flash.message = "Please select one or more assays that you have access to";
            redirect( action: 'assays', params: [ format: exportType ] );
            return
        }
        
        if( assays.size() == 1 ) {
            def assay = assays[0]
            addDownloadHeaders(exporter, assay)
            exporter.export(assay, response.getOutputStream() )
        } else {
            // If multiple assays should be exported, but it is not supported by this exporter,
            // warn the user
            if( !exporter.supportsMultiple() ) {
                flash.message = "The " + exporter.identifier + " exporter doesn't support exporting multiple assays. Please select a single assay or another exporter.";
                redirect( action: 'assays' );
            }
            
            addDownloadHeaders(exporter, assays)
            exporter.exportMultiple(assays, response.getOutputStream() )
        }
        
        response.outputStream.flush();
    }
        
    def exportStudies = {
        def ids = params.list( 'ids' )
        def tokens = params.list( 'tokens' )
        def exportType = params.format
        def user = authenticationService.getLoggedInUser()
        def studies = []

        // Determine the exporter for the given type
        def factory = new ExporterFactory()
        def exporter = factory.getExporter(exportType, user)
        
        if( !exporter ) {
            redirect(action: 'studies', params: [errorText: "Please select a valid export type. Valid types are " + factory.getExportersForType( "Study" )*.identifier ] );
        }
        
        // Retrieve a list of studies
        ids.each {
            if( it.toString().isLong() ) {
                def study = Study.get( Long.valueOf( it ) );
                if( study )
                    studies << study
            }
        }

        // Also accept tokens for defining studies
        tokens.each {
            def study = Study.findWhere(UUID: it)
            if( study )
                studies << study;
        }
        
        // Filter on readable studies
        studies = studies.findAll { it && it.canRead(user) }
        
        if( studies.size() == 0 ) {
            flash.message = "Please select one or more studies";
            redirect( action: 'studies', params: [ format: exportType ] );
            return
        }

        if(studies.size() > 1){
            def zipExporter = new ZipExporter( exporter )
            zipExporter.user = user
            
            // Send the right headers for the zip file to be downloaded
            addDownloadHeaders(zipExporter, studies)
            
            zipExporter.exportMultiple( studies, response.getOutputStream(), { study ->
                if( study.getSampleCount() == 0 )
                    return "Study " + study.title + " doesn't contain any samples, so it is not exported";
                else
                    return ""
            })
        } else {
            def studyInstance = studies.getAt(0)
            
            // make the file downloadable
            if ((studyInstance!=null) && (studyInstance.getSampleCount() > 0)){
                addDownloadHeaders(exporter, studyInstance)
                exporter.export( studyInstance, response.getOutputStream() )
            } else if( studyInstance.getSampleCount() == 0 ) {
                flash.message = "Given study doesn't contain any samples, so no excel file is created. Please choose another study.";
                redirect( action: 'studies', params: [ format: exportType ] );
            } else {
                flash.message= "Error while exporting the file, please try again or choose another study."
                redirect( action: 'studies', params: [ format: exportType ] )
            }
        }
        
        response.outputStream.flush();
    }
    
    protected def addDownloadHeaders( Exporter exporter, def entity ) {
        response.setHeader("Content-disposition", "attachment;filename=\"" + exporter.getFilenameFor(entity) + "\"")
        response.setContentType(exporter.getContentType(entity))
    }
}
