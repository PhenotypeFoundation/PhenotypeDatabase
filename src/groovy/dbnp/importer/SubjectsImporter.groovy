package dbnp.importer

import dbnp.authentication.SecUser
import dbnp.studycapturing.*

/**
 * Defines the interface for an exporter
 */
public class SubjectsImporter implements Importer {
    /**
     * SecUser that is used for authorization
     */
    SecUser user
    
    /**
     * List with validation errors
     */
    protected List<ImportValidationError> errors = []
    
    /**
     * Returns an identifier that describes this importer
     */
    public String getIdentifier() {
        "Subjects"
    }
    
    /**
     * Returns true if this importer supports the given type
     */
    public boolean supportsType(String type) {
        type in [ "clinicaldata", "subjects" ]
    }
    
    /**
     * Returns a map of parameters that should be set for this importer
     */
    public List<ImporterParameter> getParameters() {
        [
            new ImporterParameter(name: 'study', label: 'Study', values: Study.giveWritableStudies(user))
        ]
    }
    
    /**
     * Method to access the data
     */
    public def getData() {
        null
    }
    
    /**
     * Returns a list of validation errors
     */
    public List<ImportValidationError> getValidationErrors() {
        [] + validationErrors
    }

    /**
     * Validates provided data.
     * @return  True if all objects were imported succesfully,
     *          false if the validation on any of the object has failed
     */
    public boolean validateData() {
        validationErrors = []
        return true
    }
    
    /**
     * Imports provided data. This method should skip objects that fail validation
     * but store the validation errors.
     * @return  True if all objects were imported succesfully, 
     *          false if the validation on any of the object has failed 
     */
    public boolean importData() {
        validationErrors = []
        return true
    }
}