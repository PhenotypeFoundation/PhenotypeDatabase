package dbnp.export

import dbnp.authentication.SecUser

/**
 * Defines the interface for an exporter
 */
public interface Exporter {
    /**
     * Set the user into the exporter to make sure authorization is handled properly
     */
    public void setUser(SecUser user)

    /**
     * Returns an identifier that describes this export
     */
    public String getIdentifier()
    
    /**
     * Returns the type of entitites to export. Could be Study or Assay 
     * If null is returned, the exporter is not included in lists, but can still be used 
     * in code
     */
    public String getType()
    
    /**
     * Returns whether this exporter supports exporting multiple entities at once
     * If so, the class should have a proper implementation of the exportMultiple method
     */
    public boolean supportsMultiple()

    /**
     * Returns a proper filename for the given entity
     */
    public String getFilenameFor( def entity )
    
    /**
     * Returns the content type for the export
     */
    public String getContentType( def entity )

    /**
     * Export a single entity to the outputstream
     */
    public void export( def entity, OutputStream out )
    
    /**
     * Exports multiple entities to the outputstream
     */
    public void exportMultiple( def entities, OutputStream out )

}