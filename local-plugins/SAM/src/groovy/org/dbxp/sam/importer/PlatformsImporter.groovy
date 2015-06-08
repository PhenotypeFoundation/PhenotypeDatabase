package org.dbxp.sam.importer

import dbnp.importer.*
import org.dbxp.sam.*

/**
 * Defines the interface for an exporter
 */
public class PlatformsImporter extends TemplateEntityImporter<Platform> {
    
    /**
     * Returns an identifier that describes this importer
     */
    public String getIdentifier() {
        "Platforms"
    }
    
    /**
     * Returns true if this importer supports the given type.
     * Can be used to filter the available importers on a certain type
     */
    public boolean supportsType(String type) {
        type in [ "SAM", "platforms" ]
    }
    
    /**
     * Returns a link to the results page
     */
    public Map getLinkToResults(def parameters) {
        [
            url: [ controller: 'platform', action: 'list', module: parameters.module],
            label: "Platforms"
        ]
    }
    
    /**
     * Returns an entity object for this TemplateEntity (T)
     */
    public Class getEntity() {
        Platform
    }
    
    /////////////////////////////////////////////////////////////////////////
    // 
    // Methods below handle the module parameter and make sure that it is stored
    //
    ///////////////////////////////////////////////////////////////////////// 
    
    /**
     * Returns a map of parameters that should be set for this importer
     */
    public List<ImporterParameter> getParameters(def settings = [:]) {
        super.getParameters(settings) +
        [
            new ImporterParameter(name: 'module', label: 'Module', type: 'hidden' ),
        ]
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
    public Platform createObject(def data, def mapping, def parameters) {
        // Create an initial object
        def object = super.createObject(data, mapping, parameters)
        
        // Store the platform type, which is provided in the parameters
        object.platformtype = parameters.module
        
        object
    }
    
    /**
     * Returns a list of domain fields for the current template entity.
     * However, the platformtype should be filled with the module, which is entered
     * in the parameters. Therefor, the platformtype is not available for the user to choose
     */
    public List getDomainFields() {
        getEntity().domainFields.findAll { it.name != 'platformtype' }
    }
    
}