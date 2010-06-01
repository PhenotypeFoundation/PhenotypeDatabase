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

class FileController {

    def fileService;

    /**
     * Returns the file that is asked for or a 404 error if the file doesn't exist
     */
    def get = {
        // Check whether the file exists
        def filename = params.id;
        def fileExists;
        try {
            fileExists = fileService.fileExists( filename )
        } catch( FileNotFoundException e ) {
            fileExists = false;
        }
        if( !filename || !fileExists ) {
            response.status = 404;
            render( "" );
            return;
        }
        def file = fileService.get( filename );

        //response.setContentType("application/octet-stream")
        //response.setContentType( "image/jpeg" );

        // Return the file
        response.outputStream << file.newInputStream()
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
            render( newfilename );
        } else {
            response.status = 500;
            render( "" );
        }
    }
}
