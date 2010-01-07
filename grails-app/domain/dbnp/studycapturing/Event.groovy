package dbnp.studycapturing

class Event {

    String name
    Date startTime
    Date endTime
    Term classification
    ProtocolInstance protocol

    static hasMany = [subjects: Subject]

    static constraints = {
    }

    def getDuration() {
        endTime - startTime
    }
}
