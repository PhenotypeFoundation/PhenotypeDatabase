package org.dbxp.sam.importer

import dbnp.importer.*
import org.dbxp.sam.*

/**
 * Defines the interface for an exporter
 */
public class FeaturesImporter extends TemplateEntityImporter<Platform> {
    
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
            url: [ controller: 'feature', action: 'list', module: parameters.module],
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
     * Returns a map of parameters that should be set for this importer
     * As features belong to platforms, the user should select a platform as well
     */
    public List<ImporterParameter> getParameters(def settings = [:]) {
        super.getParameters(settings) +
        [
            new ImporterParameter(name: 'platform', label: 'Platform', type: 'select', values: Platform.findAllByPlatformtype(settings.module) ),
        ]
    }

    
}