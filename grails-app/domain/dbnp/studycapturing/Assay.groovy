package dbnp.studycapturing

class Assay {

    String name
    AssayType type
    String platform

    static hasMany = [samples : Sample]

    static constraints = {
    }
}
