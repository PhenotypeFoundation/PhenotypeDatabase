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
        def templateId = parameters.template.isLong() ? parameters.template.toLong() : null
        def template
        
        // Load the template from the database
        if( templateId ) {
            template = Template.get(templateId)
        }
        
        if( !template ) {
            throw new IllegalArgumentException( "No template with the templateId " + templateId + " could be found." )
        }
        
        // Create a list of domain fields and template fields to match against
        def fields = Subject.domainFields + ( template.fields ?: [] )
         
        // Return a proper format
        fields.collect { [ id: it.name, name: it.name, description: it.comment ] }
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