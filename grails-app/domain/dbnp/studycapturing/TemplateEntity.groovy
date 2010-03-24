package dbnp.studycapturing

import dbnp.data.Term

/**
 * TemplateEntity Domain Class
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
abstract class TemplateEntity implements Serializable {

	Template template

	Map templateStringFields = [:]
	Map templateTextFields = [:]
	Map templateStringListFields = [:]
	Map templateIntegerFields = [:]
	Map templateFloatFields = [:]
	Map templateDoubleFields = [:]
	Map templateDateFields = [:]
	Map templateTermFields = [:]

	static hasMany = [
		templateStringFields: String,
		templateTextFields: String,
		templateStringListFields: TemplateFieldListItem,
		templateIntegerFields: int,
		templateFloatFields: float,
		templateDoubleFields: double,
		templateDateFields: Date,
		templateTermFields: Term
	]

	static constraints = {
		template(nullable: true, blank: true)

	}

	static mapping = {
		tablePerHierarchy false

		templateTextFields type: 'text'
	}

	private Map getStore(TemplateFieldType fieldType) {
		switch(fieldType) {
			case TemplateFieldType.STRING:
				return templateStringFields
			case TemplateFieldType.TEXT:
				return templateTextFields
			case TemplateFieldType.STRINGLIST:
				return templateStringListFields
			case TemplateFieldType.INTEGER:
				return templateIntegerFields
			case TemplateFieldType.DATE:
				return templateDateFields
			case TemplateFieldType.FLOAT:
				return templateFloatFields
			case TemplateFieldType.DOUBLE:
				return templateDoubleFields
			case TemplateFieldType.ONTOLOGYTERM:
				return templateTermFields
		        default:
				throw new NoSuchFieldException("Field type ${fieldType} not recognized")
		}
	}

	/**
	 * Find a template field by its name and return its value for this entity
	 * @param fieldName The name of the template field
	 * @return the value of the field (class depends on the field type)
	 * @throws NoSuchFieldException If the field is not found or the field type is not supported
	 */
	def getFieldValue(String fieldName) {
		TemplateFieldType fieldType = template.getFieldType(fieldName)
		if (!fieldType) throw new NoSuchFieldException("Field name ${fieldName} not recognized")
		getStore(fieldType)[fieldName]
	}

	/**
	 * Set a template/entity field value
	 * @param fieldName The name of the template or entity field
	 * @param value The value to be set, this should align with the (template) field type, but there are some convenience setters
	 *
	 */
	def setFieldValue(String fieldName, value) {

		// First, search if there is an entity property with the given name, and if so, set that
		if (this.properties.containsKey(fieldName)) {
			this[fieldName] = value
		}
		// If not the found, then it is a template field, so check if there is a template
		else if (template == null) {
			throw new NoSuchFieldException("Field ${fieldName} not found in class properties: template not set")
		}
		// If there is a template, check the template fields
		else {
			// Find the target template field, if not found, throw an error
			TemplateField field = this.template.fields.find { it.name == fieldName}
			if (field == null) {
				throw new NoSuchFieldException("Field ${fieldName} not found in class properties or template fields")
			}
			// Set the value of the found template field
			else {
				// Convenience setter for template string list fields: find TemplateFieldListItem by name
				if (field.type == TemplateFieldType.STRINGLIST && value.class == String) {
					value = field.listEntries.find { it.name == value }
				}

				// Convenience setter for dates: handle string values for date fields
				if (field.type == TemplateFieldType.DATE && value.class == String) {
					// a string was given, attempt to transform it into a date instance
					// and -for now- assume the dd/mm/yyyy format
					def dateMatch = value =~ /^([0-9]{1,})([^0-9]{1,})([0-9]{1,})([^0-9]{1,})([0-9]{1,})((([^0-9]{1,})([0-9]{1,2}):([0-9]{1,2})){0,})/
					if (dateMatch.matches()) {
						// create limited 'autosensing' datetime parser
						// assume dd mm yyyy  or dd mm yy
						def parser = 'd' + dateMatch[0][2] + 'M' + dateMatch[0][4] + (((dateMatch[0][5] as int) > 999) ? 'yyyy' : 'yy')

						// add time as well?
						if (dateMatch[0][7] != null) {
							parser += dateMatch[0][6] + 'HH:mm'
						}

						value = new Date().parse(parser, value)
					}
				}

				// Set the field value
				// Caution: this assumes that all template...Field Maps are already initialized (as is done now above as [:])
				// If that is ever changed, the results are pretty much unpredictable (random Java object pointers?)!
				getStore(field.type)[fieldName] = value
				return this
			}
		}
	}


	/**
	* Return all templated fields defined in the underlying template of this entity
	*/
	def Set<TemplateField> giveFields() {
		return this.template.fields;
	}

	/**
	 * Return all relevant 'built-in' domain fields of the super class
	 * @return key-value pairs describing the built-in fields, with the names as keys and type (as TemplateFieldType) as values
 	 */
	def giveDomainFields() {
		def fieldSet = [:];
		if (super.hasProperty('name')) {
			fieldSet['name'] = TemplateFieldType.STRING;
		}
		return fieldSet;
	}

	// See revision 237 for ideas about initializing the different templateField Maps
	// with tailored Maps that already contain the neccessary keys
	/**
	 * Convenience method. Returns all unique templates used within a collection of TemplateEntities.
	 */
	static List<Template> giveTemplates(Set<TemplateEntity> entityCollection) {
		return entityCollection*.template.unique();
	}

	/**
	 * Convenience method. Returns the template used within a collection of TemplateEntities.
	 * @throws NoSuchFieldException when 0 or multiple templates are used in the collection
	 * @return The template used by all members of a collection
	 */
	static Template giveSingleTemplate(Set<TemplateEntity> entityCollection) {
		def templates = giveTemplates(entityCollection);
		if (templates.size() == 0) {
			throw new NoSuchFieldException("No templates found in collection!")
		}
		else if (templates.size() == 1) {
			return templates[0];
		}
		else {
			throw new NoSuchFieldException("Multiple templates found in collection!")
		}
	}

	def validate() {
		return super.validate()
	}
}
