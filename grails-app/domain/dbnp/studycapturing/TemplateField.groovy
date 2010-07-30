package dbnp.studycapturing

import dbnp.data.Ontology

/**
 * A TemplateField is a specification for either a 'domain field' of a subclass of TemplateEntity or a
 * 'template field' for a specific Template. See the Template class for an explanation of these terms.
 * The TemplateField class contains all information which is needed to specify what kind of data can be stored
 * in this particular field, such as the TemplateFieldType, the name, the ontologies from which terms can derive
 * in case of an ONTOLOGYTERM field, the list entries in case of a STRINGLIST fields, and so on.
 * The actual values of the template fields are stored in instances of subclasses of the TemplateEntity class.
 * For example, if there exists a Study template with a 'description' TemplateField as a member of Template.fields,
 * the actual description for each Study would be stored in the inherited templateStringFields map of that Study instance.
 *
 * One TemplateField can belong to many Templates, but they have to be the same entity as the TemplateField itself.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class TemplateField implements Serializable {

	/** The name of the TemplateField, by which it is represented to the user.  */
	String name

	/** The type of this TemplateField, such as STRING, ONTOLOGYTERM etc. */
	TemplateFieldType type

	/** The entity for which this TemplateField is meant. Only Templates for this entity can contain this TemplateField */
	Class entity

	/** The unit of the values of this TemplateField (optional) */
	String unit

	/** The help string which is shown in the user interface to describe this template field (optional, TEXT) */
	String comment

	/** The different list entries for a STRINGLIST TemplateField. This property is only used if type == TemplateFieldType.STRINGLIST */
	List listEntries

	/** Indicates whether this field is required to be filled out or not */
	boolean required

	/** Indicates whether this field is the preferred identifier for the resulting templated entity.
		This is for example used when importing to match entries in the database against the ones that are being imported. */
	boolean preferredIdentifier

	static hasMany = [
		listEntries: TemplateFieldListItem,	// to store the entries to choose from when the type is 'item from predefined list'
		ontologies: Ontology				// to store the ontologies to choose from when the type is 'ontology term'
	]

	static constraints = {

		// outcommented for now due to bug in Grails / Hibernate
		// see http://jira.codehaus.org/browse/GRAILS-6020
		// This is to verify that TemplateField names are unique within templates of each super entity
		// TODO: this probably has to change in the case of private templates of different users,
		// which can co-exist with the same name. See also Template
		// name(unique:['entity'])

		name(nullable: false, blank: false)
		type(nullable: false, blank: false)
		entity(nullable: false, blank: false)
		unit(nullable: true, blank: true)
		comment(nullable: true, blank: true)
		required(default: false)
		preferredIdentifier(default: false)
	}

	static mapping = {
		// Make sure the comments can be Strings of arbitrary length
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

	/**
	 * overloading the findAllByEntity method to make it function as expected
	 * @param Class entity (for example: dbnp.studycapturing.Subject)
	 * @return ArrayList
	 */
	public static findAllByEntity(java.lang.Class entity) {
		def results = []
		// 'this' should not work in static context, so taking Template instead of this
		TemplateField.findAll().each() {
			if (entity.equals(it.entity)) {
				results[results.size()] = it
			}
		}

		return results
	}

	/**
	 * Checks whether this template field is used in a template
	 *
	 * @returns		true iff this template field is used in a template (even if the template is never used), false otherwise
	 */
	def inUse() {
		return numUses() > 0;
	}

	/**
	 * The number of templates that use this template
	 *
	 * @returns		the number of templates that use this template.
	 */
	def numUses() {
		def templates = Template.findAll();
		def elements;
		if( templates && templates.size() > 0 ) {
			elements = templates.findAll { template -> template.fields.contains( this ) };
		} else {
			return 0;
		}

		return elements.size();
	}


}