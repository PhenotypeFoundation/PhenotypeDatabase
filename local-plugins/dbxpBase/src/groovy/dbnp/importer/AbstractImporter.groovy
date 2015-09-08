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
    
    /**
     * Returns a list of header options to match the headers against.
     * Each item should have an id and a name. A description can be specified with extra information
     *
     * @param parameters        Map with settings for the parameters specified in the getParameters method
     * @return
     * @see getParameters()
     */
    public abstract List getHeaderOptions(def parameters);
    
    /**
     * Returns whether the header options can only be selected for a single header.
     */
    public boolean headerMappingIsUnique() { true }
}