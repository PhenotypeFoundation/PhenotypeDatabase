package dbnp.studycapturing

class ProtocolParameter {

    String name
    ProtocolParameterType type
    String unit
    String description
    Term reference

    static hasMany = [listEntries : String]

    static constraints = {
    }
}
