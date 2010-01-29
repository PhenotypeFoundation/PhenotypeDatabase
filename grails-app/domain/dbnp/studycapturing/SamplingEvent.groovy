package dbnp.studycapturing

/**
 * The SamplingEvent class describes a sampling event, an event that also results in one or more samples.
 *
 * NOTE: according to documentation, super classes and subclasses share the same table.
 *       thus, there is actually no reason not to merge the sampling with the Event super class.
 *       this is especially true, since the subclass adds only one has-many relation ship.
 */

class SamplingEvent extends Event {

    static hasMany = [samples : Sample]

    static constraints = {
    }
}
