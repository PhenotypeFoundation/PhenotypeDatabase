package dbnp.studycapturing

class Event {

    Subject subject
    EventDescription eventDescription
    Date startTime
    Date endTime

    static constraints = {
    }

    def getDuration() {
        endTime - startTime
    }
}
