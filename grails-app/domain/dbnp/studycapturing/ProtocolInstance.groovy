package dbnp.studycapturing

class ProtocolInstance {

    Protocol protocol

    static hasMany = [stringParameters : String, numberParameters : double, listParameters: long] 

    static constraints = {
    }
}
