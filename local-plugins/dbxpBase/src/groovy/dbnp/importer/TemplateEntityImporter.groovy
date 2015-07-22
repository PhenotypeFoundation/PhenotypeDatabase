package dbnp.importer

import dbnp.authentication.SecUser
import dbnp.studycapturing.*
import org.dbnp.gdt.*
import grails.util.Holders

/**
 * Abstract importer class for template entities. Each column
 * in the imported file is being matched to a field in the template
 */
public abstract class TemplateEntityImporter<T extends TemplateEntity> extends AbstractImporter {
    def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
    
    /**
     * Returns a map of parameters that should be set for this importer
     */
    public List<ImporterParameter> getParameters(def settings = [:]) {
        [
            new ImporterParameter(name: 'template', label: 'Template', type: 'templates', values: Template.findAllByEntity(getEntity()))
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
     * Validates provided data.
     * @param   data            Matrix (List of lists) with the data that has been loaded from the excel/csv file
     * @param   parameters      Refers to a map with parameter values for the parameters needed by the importer
     * @return  True if all objects were imported succesfully,
     *          false if the validation on any of the object has failed
     */
    public boolean validateData(def data, def mapping, def parameters) {
        resetValidationErrors()
        
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
        
        // After that, validate each line. The header line is not needed anymore
        for( def lineNr = 1; lineNr < data.size(); lineNr++) {
            def line = data[lineNr]
            def object = createObject(line, mapping, parameters)
            
            if( !object.validate() ) {
                object.errors.allErrors.each {
                    errors << new ImportValidationError(
                        code: 2,
                        message: messageSource.getMessage(it, null),
                        line: lineNr
                    )
                }
            }
        }
        
        return !errors
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
        resetValidationErrors()
        
        // Now loop through each line and try to import the object.
        for( def lineNr = 1; lineNr < data.size(); lineNr++) {
            def line = data[lineNr]
            def object = createObject(line, mapping, parameters)
            
            if( object.validate() ) {
                object.save()
            } else {
                object.errors.allErrors.each {
                    errors << new ImportValidationError(
                        code: 2,
                        message: messageSource.getMessage(it, null),
                        line: lineNr
                    )
                }
            }
        }
        
        // Return true if no errors were found, false otherwise
        return !errors
    }
    
    /**
     * Creates an object, based on the specified parameters
     * @param   data            List with the data from one line in the file, used to create this specific object 
     * @param   mapping         Mapping from field number to object property name. The key in this map is the column number,
     *                                  the value is a map that contains 2 entries: 
     *                                          ignore (boolean)        Whether this column should be ignored
     *                                          field (map)             Map describing the field selected. The format 
     *                                                                  is the same as the output from getHeaderOptions 
     * @param   parameters      Refers to a map with parameter values for the parameters needed by the importer
     * @return  True if all objects were imported succesfully,
     *          false if the validation on any of the object has failed
     * @see     getHeaderOptions()
     */
    public T createObject(def data, def mapping, def parameters) {
        // Create an initial object
        def object = newInstance(template: getTemplate(parameters))
        
        // Loop through all columns
        data.eachWithIndex { cell, columnIndex ->
            // Retrieve the mapping
            def columnMapping = mapping[columnIndex.toString()]

            if( !columnMapping || columnMapping.ignore || !columnMapping.field?.id ) {
                log.debug( "Ignoring column " + columnIndex )
                return
            }
            
            // Determine where to store this value
            def fieldName = columnMapping.field.id
            log.debug( "Setting column " + columnIndex + " to field " + fieldName )
            
            // Store the field value
            storeField(object, fieldName, cell, columnIndex, parameters)
        }
        
        object
    }
    
    /**
     * Store the given value in a certain field on the object
     */
    protected boolean storeField(def object, String fieldName, def cell, def columnIndex, def parameters) {
        // TODO: Format and/or parse the value
        try {
            object.setFieldValue(fieldName, cell, true)
        } catch( Exception e ) {
            errors << new ImportValidationError(
                code: 3,
                message: e.getMessage(),
                column: columnIndex
            )
        }
    }

    /**
     * Retrieves a template selected by the user
     */
    protected Template getTemplate(parameters) {
        def templateId = parameters?.template?.isLong() ? parameters.template.toLong() : null
        def template
        
        // Load the template from the database
        if( templateId ) {
            template = Template.get(templateId)
        }
        
        if( !template ) {
            throw new IllegalArgumentException( "No template with the templateId " + templateId + " could be found." )
        }
        
        template
    }
    
    /**
     * Retrieves a list of domain and template fields, given the set of parameters
     */
    protected List getAllFields(parameters) {
        def template = getTemplate(parameters)
        
        // Create a list of domain fields and template fields to match against
        getDomainFields() + ( template.fields ?: [] )
    }
    
    /**
     * Returns a list of domain fields for the current template entity
     */
    public List getDomainFields() {
        getEntity().domainFields
    }
    
    /**
     * Returns a new instance of the correct type
     */
    public T newInstance(def parameters) {
        getEntity().newInstance(parameters)
    }
    
    /**
     * Returns an entity object for this TemplateEntity (T)
     */
    public abstract Class getEntity();
    
    /**
     * Returns the encoded entity name, as it is used for the template editor
     */
    public String getEncodedEntityName() {
        java.net.URLEncoder.encode(getEntity().name.bytes.encodeBase64().toString(),"UTF-8")
    }
}