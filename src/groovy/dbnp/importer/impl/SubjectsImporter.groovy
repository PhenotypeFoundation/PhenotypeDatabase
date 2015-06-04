package dbnp.importer.impl

import dbnp.importer.*
import dbnp.authentication.SecUser
import dbnp.studycapturing.*
import org.dbnp.gdt.*
import grails.util.Holders

/**
 * Defines the interface for an exporter
 */
public class SubjectsImporter extends StudyTemplateEntityImporter<Subject> {
    
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
     * Returns a link to the results page
     */
    public Map getLinkToResults(def parameters) {
        [
            url: [ controller: 'study', action: 'subjects', id: parameters.study],
            label: "Subjects for study " + getStudy(parameters)?.title
        ]
    }
    
    /**
     * Returns an entity object for this TemplateEntity (T)
     */
    public Class getEntity() {
        Subject
    }
}