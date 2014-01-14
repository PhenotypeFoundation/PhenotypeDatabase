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
import org.dbxp.sam.Feature
import org.dbxp.sam.Platform

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

		// Check whether the user has access to this file.
		// The user has access if:
		//	- the file is associated with a (part of a) study this user can read
		//	- the file is associated with a platform or feature, and the user is logged in
		def accessible = false
		def fileFound = false

		// First check whether the file is associated with a feature or platform
		[ "Feature", "Platform" ].find { table ->
			def query = "SELECT count(*) FROM " + table + " t JOIN t.templateFileFields file WHERE file = ?"
			def results = Feature.executeQuery( query, filename )
			
			if( results[ 0 ] > 0 ) {
				accessible = true
				fileFound = true
				return true	// Break from find loop
			}
		}
		
		// Afterwards, check whether the file is associated with some part of the study
		if( !fileFound ) {
			// Create a map of tables to search in, with the key being the table name
			// and the value is the HQL property in that table that contains the studyId it is
			// connected to
			def tables = [
				"Study": "t.id",
				"Subject": "t.parent.id",
				"Event": "t.parent.id",
				"SamplingEvent": "t.parent.id",
				"Sample": "t.parent.id",
				"Assay": "t.parent.id"
			]
				
			def studyId = null
			
			// Loop through each table/domain class and search for the file in the templateFileFields directly.
			// This direct approach speeds up the process significantly, compared to reading the properties of all
			// studies and looping over its file fields
			tables.find { tableName, idProperty ->
				def query = "SELECT " + idProperty + " FROM " + tableName + " t JOIN t.templateFileFields file WHERE file = ?"
				def ids = Study.executeQuery( query, filename )
				if( ids ) {
					studyId = ids[ 0 ]
					fileFound = true
					accessible = studies*.id.contains( studyId )
					
					// Break from find loop
					return true
				}
			}
		}
		
        if (!accessible) {
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