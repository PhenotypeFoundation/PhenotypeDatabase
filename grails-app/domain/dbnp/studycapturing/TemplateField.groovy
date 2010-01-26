package dbnp.studycapturing

/**
 * This is the superclass for template fields. Normally, this class will not be instantiated.
 */
abstract class TemplateField {
    String name
    TemplateFieldType type
    String unit

    static constraints = {
	    name(unique:true)
	    unit(nullable:true, blank:true)
    }

	def String toString() {
		return name
	}
}
