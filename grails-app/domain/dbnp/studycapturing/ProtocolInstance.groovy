package dbnp.studycapturing

class ProtocolInstance {

    Protocol protocol

    // TODO: check how the values can be indexed so that they can be mapped to their respective parameters (should we should use maps here?) 
    static hasMany = [stringParameterValues : String, numberParameterValues : double, listParameterValues: long]

    static constraints = {
    }
}
