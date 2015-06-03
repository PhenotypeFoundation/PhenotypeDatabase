package dbnp.importer

import dbnp.authentication.SecUser
import dbnp.studycapturing.*
import org.dbnp.gdt.*
import grails.util.Holders

/**
 * Defines the interface for an exporter
 */
public class SubjectsImporter extends StudyTemplateEntityImporter<Subject> {
    def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
    
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
            label: "Subjects for study " + Study.get(parameters.study.toLong()).title
        ]
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
        
        if( !super.validateData(data, mapping, parameters) ) {
            return false
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
        
        // Return true if no errors were found, false otherwise
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
     * Returns a new instance of the correct type
     */
    public Subject newInstance(def parameters) {
        new Subject(parameters)
    }
    
    /**
     * Returns a list of domain fields for the current template entity
     */
    public List getDomainFields() {
        Subject.domainFields
    }
}