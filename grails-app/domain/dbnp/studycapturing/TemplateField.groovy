package dbnp.studycapturing

import dbnp.data.Ontology

/**
 * This is the class for template fields. These should be part of one or more templates via Template.fields
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class TemplateField implements Serializable {
	String name
	TemplateFieldType type
    Class entity
	String unit
	String comment // help string for the user interface
	List listEntries
	boolean required
	boolean preferredIdentifier

	static hasMany = [
		listEntries: TemplateFieldListItem,	// to store the entries to choose from when the type is 'item from predefined list'
		ontologies: Ontology					// to store the ontologies to choose from when the type is 'ontology term'
	]

	static constraints = {
		// TODO: verify that TemplateField names are unique within templates of each super entity
		name(nullable: false, blank: false)
		type(nullable: false, blank: false)
		entity(nullable: false, blank: false)
		unit(nullable: true, blank: true)
		comment(nullable: true, blank: true)
		required(default: false)
		preferredIdentifier(default: false)
	}

	static mapping = {
		// TODO: this doesn't seem to work in Postgres
		comment type: 'text'
	}

	String toString() {
		return name
	}

	/**
	 * return an escaped name which can be used in business logic
	 * @return String
	 */
	def String escapedName() {
		return name.toLowerCase().replaceAll("([^a-z0-9])", "_")
	}
}