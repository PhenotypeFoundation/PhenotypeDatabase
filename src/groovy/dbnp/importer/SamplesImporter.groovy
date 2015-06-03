package dbnp.importer

import dbnp.authentication.SecUser
import dbnp.studycapturing.*
import org.dbnp.gdt.*
import grails.util.Holders

/**
 * Defines the interface for an exporter
 */
public class SamplesImporter extends StudyTemplateEntityImporter<Sample> {
    
    /**
     * Returns an identifier that describes this importer
     */
    public String getIdentifier() {
        "Samples"
    }
    
    /**
     * Returns true if this importer supports the given type.
     * Can be used to filter the available importers on a certain type
     */
    public boolean supportsType(String type) {
        type in [ "clinicaldata", "samples" ]
    }
    
    /**
     * Returns a link to the results page
     */
    public Map getLinkToResults(def parameters) {
        [
            url: [ controller: 'study', action: 'samples', id: parameters.study],
            label: "Samples for study " + getStudy(parameters)?.title
        ]
    }
    
    /**
     * Returns an entity object for this TemplateEntity (T)
     */
    public Class getEntity() {
        Sample
    }
}