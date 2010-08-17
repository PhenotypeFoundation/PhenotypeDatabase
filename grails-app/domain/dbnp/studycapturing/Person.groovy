package dbnp.studycapturing

/**
 * The Person class represents a person who is related to one ore more studies, such as a PI, a lab analyst etc.
 * Those people do not neccessarily have an account in GSCF, the Study/Persons/Affiliations administration
 * is independent of GSCF usernames and accounts.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Person extends Identity {
	String title
	String gender
	String lastName
	String prefix
	String firstName
	String initials
	String email
	String fax
	String phone
	String mobile
	String address

	static hasMany = [affiliations: PersonAffiliation]

	static constraints = {
		title(nullable: true, blank: true)
		gender(nullable: true, blank: true)
		firstName(nullable: true, blank: true)
		initials(nullable: true, blank: true)
		prefix(nullable: true, blank: true)
		lastName(nullable: true, blank: true)
		email(nullable: true, blank: true)
		fax(nullable: true, blank: true)
		phone(nullable: true, blank: true)
		address(nullable: true, blank: true)
		mobile(nullable: true, blank: true)
	}
}
