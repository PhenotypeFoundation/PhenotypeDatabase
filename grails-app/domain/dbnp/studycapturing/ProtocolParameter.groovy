package dbnp.studycapturing

class ProtocolParameter {

    enum ParameterType { STRING, NUMBER, LIST }

    String name
    ParameterType type
    
    static constraints = {
    }
}
