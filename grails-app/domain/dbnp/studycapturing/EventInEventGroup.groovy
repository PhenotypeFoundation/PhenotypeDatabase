package dbnp.studycapturing

class EventInEventGroup implements Serializable {
    Event event
    EventGroup eventGroup

    /**
     * Relative time of the event within the eventgroup
     */
    long startTime

    /**
     * duration of the event
     */
    long duration

    /**
     * Sets the startTime from an absolute date (number of seconds since 1970)
     */
    public void setAbsoluteStartTime(Number seconds) {
        startTime = seconds - eventGroup.parent.startDate.time / 1000
    }

    static belongsTo = [Event, EventGroup]

    static constraints = {
        duration(default: 0L)
    }
}
