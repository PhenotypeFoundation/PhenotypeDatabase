package dbnp.studycapturing

/**
 * This class describes groupings in the subjects of a study.
 */
class SubjectGroup {

    String name 
    static hasMany = [ subjects : Subject]

    static constraints = {
    }
}
