package dbnp.studycapturing

/**
 * This class describes groupings in the subjects of a study.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class SubjectGroup implements Serializable {
	String name
	static hasMany = [subjects: Subject]

	static constraints = {
	}
}
