package org.dbxp.sam.importer

import dbnp.importer.*
import org.dbxp.sam.*

/**
 * Defines the interface for an exporter
 */
public class FeaturesImporter extends TemplateEntityImporter<Feature> {
    
    /**
     * Returns an identifier that describes this importer
     */
    public String getIdentifier() {
        "Features"
    }
    
    /**
     * Returns true if this importer supports the given type.
     * Can be used to filter the available importers on a certain type
     */
    public boolean supportsType(String type) {
        type in [ "SAM", "features" ]
    }
    
    /**
     * Returns a link to the results page
     */
    public Map getLinkToResults(def parameters) {
        [
            url: [ controller: 'feature', action: 'list', params: [module: parameters.module]],
            label: "Features"
        ]
    }
    
    /**
     * Returns an entity object for this TemplateEntity (T)
     */
    public Class getEntity() {
        Feature
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
    public Feature createObject(def data, def mapping, def parameters) {
        // Create an initial object
        def object = super.createObject(data, mapping, parameters)
        
        // Store the platform
        object.platform = Platform.get(parameters.platform.toLong())
        
        object
    }
    
    /**
     * Returns a map of parameters that should be set for this importer
     * As features belong to platforms, the user should select a platform as well
     */
    public List<ImporterParameter> getParameters(def settings = [:]) {

        super.getParameters(settings) +
        [
            new ImporterParameter(name: 'platform', label: 'Platform', type: 'select', values: Platform.findAllByPlatformtype(settings.module) ),
            new ImporterParameter(name: 'module', label: 'Module', type: 'hidden' ),
        ]
    }

    
}