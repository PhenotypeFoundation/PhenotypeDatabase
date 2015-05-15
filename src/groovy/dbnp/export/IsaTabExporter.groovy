package dbnp.export

import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.*
import org.apache.poi.hssf.usermodel.*
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.DataFormatter

import dbnp.studycapturing.*
import org.dbnp.gdt.*

/**
 * Defines the interface for an exporter.
 * N.B. This IsaTabExporter is exactly the same as the SimpleToxExporter for now. 
 *      When this exporter is implemented properly, it should not extend SimpleToxExporter anymore, 
 *      but it should implement the Export method itself.
 */
public class IsaTabExporter extends SimpleToxExporter implements Exporter {
    /**
     * Returns an identifier that describes this export
     */
    public String getIdentifier() { "IsaTab" }
    
    /**
     * Returns the type of entitites to export. Could be Study or Assay
     */
    public String getType() { "Study" }
    
    /**
     * Returns whether this exporter supports exporting multiple entities at once
     * If so, the class should have a proper implementation of the exportMultiple method
     */
    public boolean supportsMultiple() { false }
    
    /**
     * Exports multiple entities to the outputstream
     */
    public void exportMultiple( def entities, OutputStream out ) { throw new UnsupportedOperationException( getIdentifier() + " exporter can not export multiple entities" ) }

    /**
     * Returns a proper filename for the given entity
     */
    public String getFilenameFor( def study ) {
        return "" + study.title + "_IsaTab.xls"
    }
}