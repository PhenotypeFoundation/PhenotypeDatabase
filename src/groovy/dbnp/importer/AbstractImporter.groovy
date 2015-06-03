package dbnp.importer

import dbnp.authentication.SecUser
import dbnp.studycapturing.*
import org.dbnp.gdt.*
import grails.util.Holders

/**
 * Abstract importer class for all importers, defining some basic functionality
 */
public abstract class AbstractImporter implements Importer {
    
    /**
     * SecUser that is used for authorization
     */
    SecUser user
    
    /**
     * List with validation errors
     */
    protected List<ImportValidationError> errors = []
    
    /**
     * Returns a list of validation errors
     */
    public List<ImportValidationError> getValidationErrors() {
        [] + errors
    }
    
    /**
     * Resets the validation errors
     */
    public void resetValidationErrors() {
        errors = []
    }
}