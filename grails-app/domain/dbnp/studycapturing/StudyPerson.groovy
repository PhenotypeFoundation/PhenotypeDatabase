package dbnp.studycapturing

/**
 * Link table which couples studies with persons and the role they have within the study 
 */
class StudyPerson {

    Person person
    PersonRole role

    static constraints = {
    }
}
