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
     * Returns a map of parameters that should be set for this importer
     */
    public List<ImporterParameter> getParameters()

    /**
     * Returns a map of parameters that should be set for this importer
     * The parameters could be based on the initial settings
     */
    public List<ImporterParameter> getParameters(def settings)

    /**
     * Returns a list of validation errors, after validateData or importData has been called
     */
    public List<ImportValidationError> getValidationErrors()

    /**
     * Returns a link to a page where results of the import can be seen. 
     * @return a map with two keys: url and label
     */
    public Map getLinkToResults(def parameters)
    
    /**
     * Validates provided data.
     * @param   data            Matrix (List of lists) with the data that has been loaded from the excel/csv file
     * @param   parameters      Parameters provided by the user. This map includes keys:
     *                            upload        Parameters about the uploaded file. Should not be needed, as the file has been parsed already
     *                            parameter     Refers to a map with parameter values for the parameters needed by the importer
     * @return  True if all objects were imported succesfully,
     *          false if the validation on any of the object has failed
     */
    public boolean validateData(def data, def mapping, def parameters)
    
    /**
     * Imports provided data. This method should skip objects that fail validation
     * but store the validation errors.
     * @param   data            Matrix (List of lists) with the data that has been loaded from the excel/csv file
     * @param   parameters      Parameters provided by the user. This map includes keys:
     *                            upload        Parameters about the uploaded file. Should not be needed, as the file has been parsed already
     *                            parameter     Refers to a map with parameter values for the parameters needed by the importer
     * @return  True if all objects were imported succesfully, 
     *          false if the validation on any of the object has failed 
     */
    public boolean importData(def data, def mapping, def parameters)
}