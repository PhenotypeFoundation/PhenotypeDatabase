package dbnp.studycapturing

/**
 * TemplateFieldListItem Domain Class. Used to represent the list items in a STRINGLIST TemplateField.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class TemplateFieldListItem implements Serializable {

	// A TemplateFieldListItem always belongs to one TemplateField of TemplateFieldType STRINGLIST
	static belongsTo = [parent : TemplateField]

	/** The caption of the list item */
	String name

	static constraints = {
	}

        static mapping = {
                name column:"templatefieldlistitemname"
	}

	String toString() {
		return name;
	}
}
