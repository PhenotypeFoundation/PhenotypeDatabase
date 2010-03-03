package dbnp.studycapturing

/**
 * The SamplingEvent class describes a sampling event, an event that also results in one or more samples.
 *
 * NOTE: according to documentation, super classes and subclasses share the same table.
 *       thus, we could merge the sampling with the Event super class and include a boolean
 *       However, using a separate class makes it more clear in the code that Event and SamplingEvent are treated differently
 */

class SamplingEvent extends Event {

    static constraints = {
    }
}
