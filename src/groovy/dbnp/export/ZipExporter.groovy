package dbnp.export

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream

import dbnp.authentication.SecUser

/**
 * Allows exporting multiple entities at once, in a zip file
 */
public class ZipExporter implements Exporter {
    /**
     * SecUser that is used for authorization
     */
    SecUser user
    
    // The actual exporter used to export each entity
    protected innerExporter
    
    public ZipExporter(Exporter innerExporter) {
        this.innerExporter = innerExporter
    }
    
    /**
     * Returns an identifier that describes this export
     */
    public String getIdentifier() { "Zip" }
    
    /**
     * Returns the type of entitites to export. This exporter is not 
     * suited to export any GSCF entities, but used more generic
     * 
     */
    public String getType() { null }
    
    /**
     * Returns whether this exporter supports exporting multiple entities at once
     * If so, the class should have a proper implementation of the exportMultiple method
     */
    public boolean supportsMultiple() { true }
    
    /**
     * Use the given parameters for exporting
     */
    public void setParameters(def parameters) {
        // Ignore the parameters for now
    }
    
    /**
     * Returns the content type for the export
     */
    public String getContentType( def entity ) {
        return "application/zip"
    }
    
    /**
     * Returns a proper filename for the given entity
     */
    public String getFilenameFor( def entity ) {
        return innerExporter.getIdentifier() + ".zip"
    }
    
    /**
     * Exports multiple entities to the outputstream
     */
    public void exportMultiple( def entities, OutputStream out, def shouldSkip = { "" } ) {
        // Create a ZIP file containing all the SimpleTox files
        ZipOutputStream zipFile = new ZipOutputStream( new BufferedOutputStream( out ) );
        BufferedWriter zipWriter = new BufferedWriter( new OutputStreamWriter( zipFile ) );

        // Loop through the given studies and export them
        for (entity in entities) {
            // The shouldSkip method returns an error message, if this file should be skipped
            def shouldSkipThisEntity = shouldSkip(entity)
            
            if( !shouldSkipThisEntity ) {
                try {
                    zipFile.putNextEntry( new ZipEntry( innerExporter.getFilenameFor(entity) ));
                    innerExporter.export(entity, zipFile);
                    zipWriter.flush();
                    zipFile.closeEntry();
                } catch( Exception e ) {
                    log.error "Error while writing entry to zip for entity " + entity, e;
                } finally {
                    // Always close zip entry
                    try {
                        zipWriter.flush();
                        zipFile.closeEntry();
                    } catch( Exception e ) {
                        log.error "Error while closing entry for zip for entity: " + entity, e;
                    }
                }
            } else {
                log.trace "Entity " + entity + " is not exported to zip file for type " + innerExporter.identifier + ", because: " + shouldSkipThisEntity

                // Add a text file with explanation in the zip file
                zipFile.putNextEntry( new ZipEntry( shouldSkipThisEntity.replaceAll("\\W+", "_") ) );
                zipFile.closeEntry();
            }
        }

        // Close zipfile and flush to the user
        zipFile.close();
        response.outputStream.flush();
    }
    
    /**
     * Export a single entity to the outputstream in SimpleTox format
     */
    public void export( def entity, OutputStream out ) {
        innerExporter.export( entity, out )
    }
}