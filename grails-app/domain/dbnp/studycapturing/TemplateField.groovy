package dbnp.studycapturing

/**
 * This is the superclass for template fields. Normally, this class will not be instantiated.
 */
abstract class TemplateField {
    String name
    TemplateFieldType type

    static constraints = {
    }

	def String toString() {
		return name
	}
}
