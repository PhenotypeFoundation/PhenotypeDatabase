package dbnp.studycapturing

import dbnp.data.Term

/**
 * Class describing the available protocols in the database, and their respective protocol parameters
 * Concrete instances of protocol application (and parameter values) should be stored as ProtocolInstance
 * For the moment, there is one global Protocol store. From user experience, it should become clear if this store
 * has to remain global or should be bound to specific templates, users, user groups or even studies.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Protocol implements Serializable {
    String name
    Term   reference
    
    static hasMany = [parameters : ProtocolParameter, compounds: Compound]
    static constraints = {
        reference(nullable: true, blank: true)
    }

	/**
	 * overloaded toString method
	 * @return String
	 */
	def String toString() {
		return this.name;
	}
}