package dbnp.studycapturing

import grails.test.*

/**
 * FileServiceTests Test
 *
 * Description of my test
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
class FileServiceTests extends GrailsUnitTestCase {
    def tmpFile;
    def fileService;

    protected void setUp() {
        super.setUp()

        fileService = new FileService();

        // Override uploadDir method because the applicationContext is not
        // available in testcases
        fileService.metaClass.getUploadDir = {
            return new File( System.properties['base.dir'] + File.separator + 'web-app' + File.separator + 'fileuploads' );
        }

        // Create two files
        println( fileService.getUploadDir() );

        new File( fileService.getUploadDir(), 'FileServiceTest.txt' ).createNewFile();
        new File( fileService.getUploadDir(), 'FileServiceTest2.txt' ).createNewFile();
        new File( fileService.getUploadDir(), 'FileServiceTest3' ).createNewFile();

        // Make sure the file 'nonExistent.txt' does not exist
        def f = new File( fileService.getUploadDir(), 'nonExistent.txt' );
        if( f.exists() ) {
            tmpFile = new File( fileService.getUploadDir(), 'nonExistent-' . System.currentTimeMillis() . '.txt' );
            f.transferTo( tmpFile );
        }

    }

    protected void tearDown() {
        super.tearDown()
        new File( fileService.getUploadDir(), 'FileServiceTest.txt' ).delete();
        new File( fileService.getUploadDir(), 'FileServiceTest2.txt' ).delete();
        new File( fileService.getUploadDir(), 'FileServiceTest3' ).delete();

        // If file existed, move back
        if( tmpFile ) {
            tmpFile.transferTo( new File( fileService.getUploadDir(), 'nonExistent.txt' ) );
        }

    }

    void testGetUploadDir() {
        assert fileService.getUploadDir() instanceof File;
        assert fileService.getUploadDir().exists();
        assert fileService.getUploadDir().isDirectory();

        println fileService.getUploadDir();
    }

    void testFileExists() {
        assert fileService.fileExists( 'FileServiceTest.txt' );
        assert fileService.fileExists( 'FileServiceTest3' );
        assert !fileService.fileExists( 'nonExistent.txt' );
    }

    void testMoveFileToUploadDir() {
        // Move file
        fileService.moveFileToUploadDir( new File( fileService.getUploadDir(), 'FileServiceTest2.txt' ) );
        assert !fileService.fileExists( "FileServiceTest2.txt" );
        assert fileService.fileExists( "FileServiceTest20.txt" );

        fileService.moveFileToUploadDir( new File( fileService.getUploadDir(), 'FileServiceTest20.txt' ), "FileServiceTest2.txt" );
        assert fileService.fileExists( "FileServiceTest2.txt" );
        assert !fileService.fileExists( "FileServiceTest20.txt" );
    }

    void testUniqueFilename() {
        assert fileService.fileExists( "FileServiceTest2.txt" );
        assert !fileService.fileExists( fileService.getUniqueFilename( "FileServiceTest2.txt" ) )

        assert !fileService.fileExists( 'nonExistent.txt' );
        assert fileService.getUniqueFilename( 'nonExistent.txt' ) == 'nonExistent.txt'
    }

}
