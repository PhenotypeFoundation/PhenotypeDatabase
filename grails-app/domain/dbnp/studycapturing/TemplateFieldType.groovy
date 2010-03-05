package dbnp.studycapturing

/**
 * Enum describing the type of a templated field.
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
public enum TemplateFieldType implements Serializable  {
	STRING('String'),
	TEXT('Long string'),
	INTEGER('Integer number'),
	FLOAT('Floating-point number'),
	DOUBLE('Double precision floating-point number'),
	STRINGLIST('List of items'),
	ONTOLOGYTERM('Ontology Reference'),
	DATE('Date')

	String name

	TemplateFieldType(String name) {
		this.name = name
	}

	static list() {
		[STRING, TEXT, INTEGER, FLOAT, DOUBLE, STRINGLIST, ONTOLOGYTERM, DATE]
	}

	def getDefaultValue() {
		switch(this) {
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
			default:
				throw new NoSuchFieldException("Field type ${fieldType} not recognized")
		}
	}

	// It would be nice to see the description string in the scaffolding,
	// and the following works, but then the item cannot be saved properly.
	// TODO: find a way to display the enum description but save the enum value in the scaffolding
	/*
	def String toString() {
		  return this.name
	}
	*/
}