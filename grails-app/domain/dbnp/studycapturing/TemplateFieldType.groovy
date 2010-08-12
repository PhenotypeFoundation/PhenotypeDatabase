package dbnp.studycapturing

/**
 * Enum describing the type of a TemplateField.
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
public enum TemplateFieldType implements Serializable {
	STRING('String'),
	TEXT('Long string'),
	INTEGER('Integer number'),
	FLOAT('Floating-point number'),
	DOUBLE('Double precision floating-point number'),
	STRINGLIST('List of items'),
	ONTOLOGYTERM('Ontology Reference'),
	DATE('Date'),
	RELTIME('Relative time'), // relative date, e.g. days since start of study
	FILE('File'),
	BOOLEAN('Boolean'),
	TEMPLATE('Template')
    // TODO: add a timezone-aware date type to use for study start date

    String name

	TemplateFieldType(String name) {
		this.name = name
	}

	static list() {
		[STRING, TEXT, INTEGER, FLOAT, DOUBLE, STRINGLIST, ONTOLOGYTERM, DATE, RELTIME, FILE, BOOLEAN, TEMPLATE]
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
				return false;
			case TEMPLATE:
				return null;
			default:
				throw new NoSuchFieldException("Field type ${fieldType} not recognized")
		}
	}
}