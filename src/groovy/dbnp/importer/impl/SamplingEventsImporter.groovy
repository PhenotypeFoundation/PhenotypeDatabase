package dbnp.importer.impl

import dbnp.importer.*
import dbnp.authentication.SecUser
import dbnp.studycapturing.*
import org.dbnp.gdt.*
import grails.util.Holders

/**
 * Importer to store sampling events
 */
public class SamplingEventsImporter extends StudyTemplateEntityImporter<SamplingEvent> {
    
    /**
     * Returns an identifier that describes this importer
     */
    public String getIdentifier() {
        "Sampling Events"
    }
    
    /**
     * Returns true if this importer supports the given type.
     * Can be used to filter the available importers on a certain type
     */
    public boolean supportsType(String type) {
        type in [ "study", "clinicaldata", "samplingevents" ]
    }
    
    /**
     * Returns a link to the results page
     */
    public Map getLinkToResults(def parameters) {
        [
            url: [ controller: 'study', action: 'design', id: parameters.study],
            label: "view design for study " + getStudy(parameters)?.title
        ]
    }

    /**
     * Returns a link to the edit page
     */
    public Map getLinkToEdit(def parameters) {
        [
                url: [ controller: 'studyEditDesign', id: parameters.study],
                label: "edit design for study " + getStudy(parameters)?.title
        ]
    }
    
    /**
     * Returns an entity object for this TemplateEntity (T)
     */
    public Class getEntity() {
        SamplingEvent
    }
}
