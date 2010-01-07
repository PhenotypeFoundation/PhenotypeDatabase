package dbnp.studycapturing

/**
 * Class describing the available protocols in the database, and their respective protocol parameters
 * Concrete instances of protocol application (and parameter values) should be stored as ProtocolInstance
 */
class Protocol {

    String name
    Term reference
    
    static hasMany = [parameters : ProtocolParameter]

    static constraints = {
    }
}
