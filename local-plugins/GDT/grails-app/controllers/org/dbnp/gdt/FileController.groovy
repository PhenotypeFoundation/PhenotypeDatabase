package org.dbnp.gdt

import grails.converters.JSON

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
 * $Rev: 1654 $
 * $Author: robert@isdat.nl $
 * $Date: 2011-03-21 10:34:04 +0100 (Mon, 21 Mar 2011) $
 */

import org.dbnp.gdt.FileService

class FileController {
    def fileService;

    /**
     * Returns the file that is asked for or a 404 error if the file doesn't exist
     */
    def get = {
        def fileExists;

		// Filename is not url decoded for some reason
		def coder = new org.apache.commons.codec.net.URLCodec()
		def filename = coder.decode(params.id)

		// Security check to prevent accessing files in other directories
		if( filename.contains( '..' ) ) {
			response.status = 500;
			render "Invalid filename given";
			return;
		}

        try {
            fileExists = fileService.fileExists( filename )
        } catch( FileNotFoundException e ) {
            fileExists = false;
        }
        if( !filename || !fileExists ) {
            response.status = 404;
            render( "File not found" );
            return;
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
            return newfilename;
        } else {
            response.status = 500;
            render( "" );
        }
    }
}
