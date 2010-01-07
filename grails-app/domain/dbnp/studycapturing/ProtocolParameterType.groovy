package dbnp.studycapturing

public enum ProtocolParameterType {
    STRING('String'),
    NUMBER('Number'),
    STRINGLIST('List')

    String name

    ProtocolParameterType(String name) {
     this.name = name
    }

    static list() {
     [STRING, NUMBER, STRINGLIST]
    }

    /*def String toString() {
        return this.name
    }*/

}