package dbnp.importer

/**
 * Defines a parameter to be set for importing
 */
public class ImporterParameter {
    String name
    String label
    
    // Type could be text, select, checkbox or hidden
    String type = "text"
    
    def values
}