package dbnp.studycapturing

/**
 * TemplateFieldListItem Domain Class
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class TemplateFieldListItem implements Serializable {
	String name

	static constraints = {
	}

	String toString() {
		return name;
	}
}
