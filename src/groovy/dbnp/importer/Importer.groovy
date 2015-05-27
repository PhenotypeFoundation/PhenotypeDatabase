package dbnp.importer

import dbnp.authentication.SecUser

/**
 * Defines the interface for an exporter
 */
public interface Importer {
    /**
     * Set the user into the exporter to make sure authorization is handled properly
     */
    public void setUser(SecUser user)

    /**
     * Returns an identifier that describes this importer
     */
    public String getIdentifier()
    
    /**
     * Returns true if this importer supports the given type
     */
    public boolean supportsType(String type)
    
    /**
     * Method to access the data
     */
    public def getData()
    
    /**
     * Returns a list of validation errors
     */
    public List<ImportValidationError> getValidationErrors()

    /**
     * Validates provided data.
     * @return  True if all objects were imported succesfully,
     *          false if the validation on any of the object has failed
     */
    public boolean validateData()
    
    /**
     * Imports provided data. This method should skip objects that fail validation
     * but store the validation errors.
     * @return  True if all objects were imported succesfully, 
     *          false if the validation on any of the object has failed 
     */
    public boolean importData()
}