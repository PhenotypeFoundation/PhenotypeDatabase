package dbnp.studycapturing

/**
 * Enum describing the type of a templated field.
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
public enum TemplateFieldType {
	STRING('String'),
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
		[STRING, INTEGER, FLOAT, DOUBLE, STRINGLIST, ONTOLOGYTERM, DATE]
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