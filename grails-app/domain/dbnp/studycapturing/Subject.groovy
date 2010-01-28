package dbnp.studycapturing

import dbnp.data.Term

/**
 * This domain class describes the subjects in a study.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Subject implements Serializable {
	static searchable = true
	String name
	Term species
	Map templateStringFields
	Map templateIntegerFields
	Map templateFloatFields
	Map templateTermFields

	static hasMany = [
		templateStringFields: String, // stores both STRING and STRINGLIST items (latter should be checked against the list)
		templateIntegerFields: int,
		templateFloatFields: float,
		templateTermFields: Term
	]

	static constraints = {
	}
}
