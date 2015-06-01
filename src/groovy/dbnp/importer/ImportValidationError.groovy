package dbnp.importer

/**
 * Defines a validation error during import
 */
public class ImportValidationError {
    public Integer code
    public String message
    public Integer line  = null 
    public Integer column = null
    
    public String toString() {
        return "[" + code + "] " + message + ( line ? " on line " + line : "" ) + ( column ? " for column " + column : "" )
    }
}