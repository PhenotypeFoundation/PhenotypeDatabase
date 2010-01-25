package dbnp.studycapturing

class ProtocolParameterInstance {

    ProtocolParameter protocolParameter
    String value

    // to do: replace string by element in {String, double, long}
    // ways static hasMany = [stringParameterValues : String, numberParameterValues : double, listParameterValues: long]

    static constraints = {
    }
}
