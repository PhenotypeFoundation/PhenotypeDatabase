package dbnp.studycapturing

/**
 * The Event class describes an actual event, as it has happened to a certain subject. Often, the same event occurs
 * to multiple subjects at the same time. That is why the actual description of the event is factored out into a more
 * general EventDescription class. Events that also lead to sample(s) should be instantiated as SamplingEvents.
 */
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
