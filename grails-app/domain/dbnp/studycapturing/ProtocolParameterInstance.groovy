package dbnp.studycapturing

// this class is now obsolete, should be deleted by Jahn when he's done with Event controller/views
class ProtocolParameterInstance {

    ProtocolParameter protocolParameter
    String value

    // to do: replace string by element in {String, double, long}
    // ways static hasMany = [stringParameterValues : String, numberParameterValues : double, listParameterValues: long]

    static constraints = {
    }
}
