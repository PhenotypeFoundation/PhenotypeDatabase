/**
 * FileController
 *
 * Handles file uploads and downloads
 *
 * @author      Robert Horlings
 * @since	20100601
 * @package	dbnp.studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.studycapturing

import grails.plugins.springsecurity.Secured

class FileController {
    def fileService
    def authenticationService

    /**
     * Returns the file that is asked for or a 404 error if the file doesn't exist
     */
    def get = {
        def fileExists;
        def studies = Study.giveReadableStudies(authenticationService.getLoggedInUser())

		// Filename is not url decoded for some reason
		def coder = new org.apache.commons.codec.net.URLCodec()
		def filename = coder.decode(params.id)

		// Security check to prevent accessing files in other directories
		if( filename.contains( '..' ) ) {
			response.status = 500;
			render "Invalid filename given";
			return;
		}

        def fileMap = [:]
        studies.each() { study ->
            def studyFiles = []

            studyFiles.addAll(getFileFields(study))
            studyFiles.addAll(getFileFields(study.subjects))
            studyFiles.addAll(getFileFields(study.events))
            studyFiles.addAll(getFileFields(study.samplingEvents))
            //Files per sample slows down the process significantly and are unlikely to exist
            studyFiles.addAll(getFileFields(study.samples))
            studyFiles.addAll(getFileFields(study.assays))

            studyFiles.each() {
                fileMap.put(it, study.id)
            }
        }

        try {
            fileExists = fileService.fileExists( filename )
        } catch( FileNotFoundException e ) {
            fileExists = false;
        }
        if( !filename || !fileExists ) {
            response.status = 404;
            render( "File not found" );
            return
        }

        if (!studies.id.contains(fileMap.get(filename))) {
            response.status = 500;
            render "Not authorized to acces file";
            return
        }

        def file = fileService.get( filename );

        //response.setContentType("application/octet-stream")
        //response.setContentType( "image/jpeg" );

        // Return the file
        response.setHeader "Content-disposition", "attachment; filename=${filename}"
        response.outputStream << file.newInputStream()
        response.outputStream.flush()
    }

    /**
     * Uploads a file and returns the filename under which the file is saved
     */
    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def upload = {
        def file = request.getFile( params.get( 'field' ) );

        // If no file is uploaded, raise an error
        if( file == null ) {
            response.status = 500;
            render( "" );
        }

        // If an old file exists, delete it
        if( params.get( 'oldFile' ) ) {
            fileService.delete( params.get( 'oldFile' ) );
        }
        
        // Move the file to a upload dir
        def newfilename = fileService.moveFileToUploadDir( file, file.getOriginalFilename() );

        // Return data to the user
        if( newfilename ) {
            render( text: newfilename );
        } else {
            response.status = 500;
            render( "" );
        }
    }

    def getFileFields(Object part) {
        def fileList = []
            def fileFields = part.templateFileFields
            if(fileFields instanceof org.hibernate.collection.PersistentMap) {
               fileList = fileFields.values()
            } else if (part.size() > 0) {
                part.each { entity ->
                    fileList.addAll(entity.templateFileFields.values())
                }
            }
        return fileList
    }
}