package dbnp.studycapturing

import dbnp.data.Term

/**
 * Class describing the available protocols in the database, and their respective protocol parameters
 * Concrete instances of protocol application (and parameter values) should be stored as ProtocolInstance
 * For the moment, there is one global Protocol store. From user experience, it should become clear if this store
 * has to remain global or should be bound to specific templates, users, user groups or even studies.
 */
class Protocol {

    String name
    Term reference
    
    static hasMany = [parameters : ProtocolParameter]

    static constraints = {
    }
}
