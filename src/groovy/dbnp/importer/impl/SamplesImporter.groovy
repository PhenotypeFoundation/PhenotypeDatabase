package dbnp.importer.impl

import dbnp.importer.*
import dbnp.authentication.SecUser
import dbnp.studycapturing.*
import org.dbnp.gdt.*
import grails.util.Holders

/**
 * Defines the interface for an exporter
 */
public class SamplesImporter extends StudyTemplateEntityImporter<Sample> {
    
    /**
     * Returns a map of parameters that should be set for this importer
     */
    public List<ImporterParameter> getParameters(def settings = [:]) {
        // Also add the checkbox attach samples to subjects
        super.getParameters(settings) +
        [
            new ImporterParameter(name: 'attachToSubjects', label: 'Attach to subjects', type: 'checkbox'),
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
        def options = super.getHeaderOptions(parameters)
        
        // If the user wants to attach samples to subjects, allow the user
        // to also choose 'subject name' from the options
        if( parameters.attachToSubjects ) {
            options.add 0, [ id: '#subjectName', name: '[Subject name]', description: 'Subject to attach this sample to' ]
        }
        
        options
    }
    
    /**
     * Store the given value in a certain field on the object
     */
    protected boolean storeField(def sample, String fieldName, def cell, def columnIndex, def parameters) {
        println "StoreField Sample"
        
        // Handle the special identifier '#subjectName', which denotes
        // that this value should be used to lookup the subject
        if( fieldName == '#subjectName' && parameters.attachToSubjects) {
            // Find a subject with the given name
            def studyId = parameters.study
            
            def subject = Subject.where {
                parent.id == studyId
                name == cell 
            }.find()
            
            if( subject ) {
                sample.parentSubject = subject
            } else {
                errors << new ImportValidationError(
                    code: 4,
                    message: "No subject could be found with name " + subject + ". The sample is imported but not attached to a subject",
                    column: columnIndex
                )
            }
        } else {
            // Set the field value itself 
            println "  Store field from super method"
            super.storeField sample, fieldName, cell, columnIndex, parameters
        }
        
    }

    
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
        type in [ "study", "clinicaldata", "samples" ]
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