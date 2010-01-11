package dbnp.studycapturing

class SamplingEvent extends Event {

    static hasMany = [samples : Sample]

    static constraints = {
    }
}
