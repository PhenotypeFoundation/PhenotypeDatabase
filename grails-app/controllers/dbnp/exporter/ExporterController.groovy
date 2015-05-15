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

    /*
     * List of all studies for selection the study to export
     * Using the same code as 'list' into StudyController
     */
    def index = {
        def exporterFactory = new ExporterFactory()
        
        // If a type is already given, use that type
        def format = params.get('format')
        def formats = []
        if( format ) {
            if(exporterFactory.getExporter(format)?.type != "Study") {
                flash.message = "The specified output type " + format + " is not supported. Please select one of the supported output types.";
                redirect(action: 'index')
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

    def error = {
        [errorText: params.errorText]
    }

    def export = {
        def ids = params.list( 'ids' )
        def tokens = params.list( 'tokens' )
        def exportType = params.format
        def user = authenticationService.getLoggedInUser()
        def studies = []

        // Determine the exporter for the given type
        def factory = new ExporterFactory()
        def exporter = factory.getExporter(exportType)
        
        if( !exporter ) {
            redirect(action: 'error', params: [errorText: "Please select a valid export type. Valid types are " + factory.getExportersForType( "Study" )*.identifier ] );
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
            redirect( action: 'index', params: [ format: exportType ] );
            return
        }

        if(studies.size() > 1){
            def zipExporter = new ZipExporter( exporter )

            // Send the right headers for the zip file to be downloaded
            response.setContentType( "application/zip" ) ;
            response.addHeader( "Content-Disposition", "attachment; filename=\"" + zipExporter.getFilenameFor(null) + "\"" ) ;

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
                response.setHeader("Content-disposition", "attachment;filename=\"" + exporter.getFilenameFor(studyInstance) + "\"")
                response.setContentType("application/octet-stream")
                exporter.export( studyInstance, response.getOutputStream() )
            } else if( studyInstance.getSampleCount() == 0 ) {
                flash.message = "Given study doesn't contain any samples, so no excel file is created. Please choose another study.";
                redirect( action: 'index', params: [ format: exportType ] );
            } else {
                flash.message= "Error while exporting the file, please try again or choose another study."
                redirect( action: 'index', params: [ format: exportType ] )
            }
        }
        
        response.outputStream.flush();
    }
}
