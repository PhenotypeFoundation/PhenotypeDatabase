package dbnp.importer

import dbnp.authentication.SecUser
import dbnp.studycapturing.*
import org.dbnp.gdt.*
import grails.util.Holders

/**
 * Abstract importer class for template entities that belong to a study. Each column
 * in the imported file is being matched to a field in the template
 */
public abstract class StudyTemplateEntityImporter<T extends TemplateEntity> extends TemplateEntityImporter<T> {
    
    /**
     * Returns a map of parameters that should be set for this importer
     */
    public List<ImporterParameter> getParameters(def settings = [:]) {
        super.getParameters(settings) + 
        [
            new ImporterParameter(name: 'study', label: 'Study', type: 'select', values: Study.giveWritableStudies(user)),
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
    public T createObject(def data, def mapping, def parameters) {
        // Create an initial object
        def object = super.createObject(data, mapping, parameters)
        
        // Store the study
        object.parent = Study.get(parameters.study.toLong())
        
        object
    }
    
    /**
     * Returns the study selected by the user
     */
    public Study getStudy(def parameters) {
        if( !parameters.study || !parameters.study.isLong() ) 
            return null
            
        Study.get(parameters.study.toLong())
    }

}