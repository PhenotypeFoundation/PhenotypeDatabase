package dbnp.studycapturing

import dbnp.data.Term

/**
 * This domain class describes the subjects in a study.
 */
class Subject {

	String name
	Term species
	Map templateStringFields
	Map templateNumberFields
	Map templateStringListFields
	Map templateTermFields

	static hasMany = [
	        templateStringFields : String,
		templateNumberFields : float,
		templateStringListFields : String,
		templateTermFields : Term
	]
	
	static constraints = {
	}
}
