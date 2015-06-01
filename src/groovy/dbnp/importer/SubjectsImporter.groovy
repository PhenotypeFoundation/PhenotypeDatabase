package dbnp.importer

import dbnp.authentication.SecUser
import dbnp.studycapturing.*
import org.dbnp.gdt.*

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
     * Returns true if this importer supports the given type.
     * Can be used to filter the available importers on a certain type
     */
    public boolean supportsType(String type) {
        type in [ "clinicaldata", "subjects" ]
    }
    
    /**
     * Returns a map of parameters that should be set for this importer
     */
    public List<ImporterParameter> getParameters() {
        [
            new ImporterParameter(name: 'study', label: 'Study', type: 'select', values: Study.giveWritableStudies(user)),
            new ImporterParameter(name: 'template', label: 'Template', type: 'select', values: Template.findAllByEntity(Subject))
        ]
    }
    
    /**
     * Returns a list of header options to match the headers against.
     * Each item should have an id and a name. A description can be specified with extra information
     * 
     * @param parameters        Map with settings for the parameters specified in the getParameters method
     * @return 
     * @see getParameters()
     */
    public List getHeaderOptions(def parameters) {
        // Create a list of domain fields and template fields to match against
        def fields = getAllFields(parameters)
         
        // Return a proper format
        fields.collect { [ id: it.name, name: it.name, description: it.comment ] }
    }

    /**
     * Returns a list of validation errors
     */
    public List<ImportValidationError> getValidationErrors() {
        [] + errors
    }

    /**
     * Validates provided data.
     * @param   data            Matrix (List of lists) with the data that has been loaded from the excel/csv file
     * @param   parameters      Refers to a map with parameter values for the parameters needed by the importer
     * @return  True if all objects were imported succesfully,
     *          false if the validation on any of the object has failed
     */
    public boolean validateData(def data, def mapping, def parameters) {
        errors = []
        
        // First create a list of required fields and see whether they are mapped.
        // If not, not a single entity will be imported
        def fields = getAllFields(parameters)
        def mappingValues = mapping.values()
        def notMappedRequiredFields = fields.findAll { it.required }.findAll { field ->
            // Filter on required fields that have not been mapped
            !mappingValues.find { fieldMapping -> fieldMapping?.field?.id == field.name }
        }
        
        if( notMappedRequiredFields ) {
            notMappedRequiredFields.each { 
                errors << new ImportValidationError(
                    code: 1,
                    message: "The field " + it.name + " is a required field in this template, and must be mapped."
                )
            }
            
            return false;
        }
        
        
        return true
    }
    
    /**
     * Imports provided data. This method should skip objects that fail validation
     * but store the validation errors.
     * @param   data            Matrix (List of lists) with the data that has been loaded from the excel/csv file
     * @param   parameters      Refers to a map with parameter values for the parameters needed by the importer
     * @return  True if all objects were imported succesfully, 
     *          false if the validation on any of the object has failed 
     */
    public boolean importData(def data, def mapping, def parameters) {
        errors = []
        return true
    }
    
    /**
     * Retrieves a list of domain and template fields, given the set of parameters
     */
    protected List getAllFields(parameters) {
        def templateId = parameters?.template?.isLong() ? parameters.template.toLong() : null
        def template
        
        // Load the template from the database
        if( templateId ) {
            template = Template.get(templateId)
        }
        
        if( !template ) {
            throw new IllegalArgumentException( "No template with the templateId " + templateId + " could be found." )
        }
        
        // Create a list of domain fields and template fields to match against
        Subject.domainFields + ( template.fields ?: [] )
    }
}