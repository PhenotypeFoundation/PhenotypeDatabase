package dbnp.studycapturing

/**
 * Enum describing the type of a TemplateField.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
public enum TemplateFieldType implements Serializable {
	STRING('String'),									// string
	TEXT('Long string'),								// text
	INTEGER('Integer number'),							// integer
	FLOAT('Floating-point number'),						// float
	DOUBLE('Double precision floating-point number'),	// double
	STRINGLIST('List of items'),						// string list
	ONTOLOGYTERM('Ontology Reference'),					// ontology reference
	DATE('Date'),										// date
	RELTIME('Relative time'),							// relative date, e.g. days since start of study
	FILE('File'),										// file
	BOOLEAN('Boolean'),									// boolean
	TEMPLATE('Template'),								// template
	MODULE('Module'),									// third party connected module,
	LONG('Long number')									// long
    // TODO: add a timezone-aware date type to use for study start date

    String name

	TemplateFieldType(String name) {
		this.name = name
	}

	static list() {
		[STRING, TEXT, INTEGER, FLOAT, DOUBLE, STRINGLIST, ONTOLOGYTERM, DATE, RELTIME, FILE, BOOLEAN, TEMPLATE, MODULE, LONG]
	}

	def getDefaultValue() {
		switch (this) {
			case [STRING, TEXT]:
				return ""
			case INTEGER:
				return Integer.MIN_VALUE
			case FLOAT:
				return Float.NaN
			case DOUBLE:
				return Double.MIN_VALUE
			case STRINGLIST:
				return null
			case ONTOLOGYTERM:
				return null
			case DATE:
				return null
			case RELTIME:
				return null
			case FILE:
				return ""
			case BOOLEAN:
				return false
			case TEMPLATE:
				return null
			case MODULE:
				return null
			case LONG:
				return Long.MIN_VALUE
			default:
				throw new NoSuchFieldException("Field type ${fieldType} not recognized")
		}
	}
}