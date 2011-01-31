package dbnp.studycapturing

/**
 * The PersonAffiliation class is an attribute of a Person, it represents an affiliation where she/he works for.
 * PersonAffiliation is an independent list of affiliations, and does not neccessarily belong to one Person.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class PersonAffiliation extends Identity {

	String institute
	String department

	String toString() { "${institute} / ${department}" }

	static constraints = {
	}
}
