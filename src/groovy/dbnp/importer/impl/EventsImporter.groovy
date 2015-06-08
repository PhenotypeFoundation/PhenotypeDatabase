package dbnp.importer.impl

import dbnp.importer.*
import dbnp.authentication.SecUser
import dbnp.studycapturing.*
import org.dbnp.gdt.*
import grails.util.Holders

/**
 * Importer to store events
 */
public class EventsImporter extends StudyTemplateEntityImporter<Event> {
    
    /**
     * Returns an identifier that describes this importer
     */
    public String getIdentifier() {
        "Events"
    }
    
    /**
     * Returns true if this importer supports the given type.
     * Can be used to filter the available importers on a certain type
     */
    public boolean supportsType(String type) {
        type in [ "study", "clinicaldata", "events" ]
    }
    
    /**
     * Returns a link to the results page
     */
    public Map getLinkToResults(def parameters) {
        [
            url: [ controller: 'study', action: 'design', id: parameters.study],
            label: "Design for study " + getStudy(parameters)?.title
        ]
    }
    
    /**
     * Returns an entity object for this TemplateEntity (T)
     */
    public Class getEntity() {
        Event
    }
}