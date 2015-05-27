package dbnp.importer

/**
 * Defines a validation error during import
 */
public class ImportValidationError {
    public int code
    public String message
    public int line
    public int column = null
}