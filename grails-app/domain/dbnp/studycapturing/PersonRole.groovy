package dbnp.studycapturing

/**
 * The role of a person, as specified in a StudyPerson relation.
 * Person roles form an independent 'roles list' and are therefore not coupled to a specific StudyPerson relation with belongsTo.
 * Generally, there will only be a few PersonRoles such as PI, lab analyst etc.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class PersonRole extends nl.grails.plugins.gdt.Identity {

	/** The name of the role, such as Project Leader or PI */
	String name

	static constraints = {
	}
}
