package dbnp.studycapturing

/**
 * Instances of this class describe an extra (template) field for the Subject entity.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

class TemplateStudyField extends TemplateField {
	static constraints = {
		name(unique: true)
	}

	def String toString() {
		super.toString()
	}
}