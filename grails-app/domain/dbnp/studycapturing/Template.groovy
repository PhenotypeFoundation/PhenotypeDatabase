package dbnp.studycapturing

/**
 * The Template class describes a study template, which is basically an extension of the study capture entities
 * in terms of extra fields (described by classes that extend the TemplateField class).
 * At this moment, only extension of the subject entity is implemented.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Template implements Serializable {
    String name
    //nimble.User owner

    static hasMany = [subjectFields : TemplateSubjectField]
  
    static constraints = {
        name(unique:true)
    }

    def String toString() {
        return this.name;
    }
}
