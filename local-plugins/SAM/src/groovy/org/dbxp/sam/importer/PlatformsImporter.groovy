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
        type in [ "sam", "platforms" ]
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
}