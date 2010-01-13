package dbnp.studycapturing

/**
 * The SamplingEvent class describes a sampling event, an event that also results in one or more samples.
 */
class SamplingEvent extends Event {

    static hasMany = [samples : Sample]

    static constraints = {
    }
}
