package dbnp.studycapturing

class SubjectGroup {

    Study study

    static hasMany = [ subjects : Subject]

    static constraints = {
    }
}
