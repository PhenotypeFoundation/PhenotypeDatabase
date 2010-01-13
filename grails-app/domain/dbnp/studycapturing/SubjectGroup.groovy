package dbnp.studycapturing

/**
 * This class describes groupings in the subjects of a study.
 */
class SubjectGroup {

    static hasMany = [ subjects : Subject]

    static constraints = {
    }
}
