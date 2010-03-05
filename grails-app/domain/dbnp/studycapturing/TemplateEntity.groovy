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
class TemplateEntity implements Serializable {

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
	 * Find a template field by its name and return its value for this subject
	 * @param fieldName The name of the template subject field
	 * @return the value of the field (class depends on the field type)
	 * @throws NoSuchFieldException If the field is not found or the field type is not supported
	 */
	def getFieldValue(String fieldName) {
		TemplateFieldType fieldType = template.getFieldType(fieldName)
		if (!fieldType) throw new NoSuchFieldException("Field name ${fieldName} not recognized")
		getStore(fieldType)[fieldName]
	}

	def setFieldValue(String fieldName, value) {
		if (this.properties.containsKey(fieldName)) {
			this[fieldName] = value
		}
		else
		if (template == null) {
			throw new NoSuchFieldException("Field ${fieldName} not found in class properties")
		}
		else {
			TemplateField field = this.template.fields.find { it.name == fieldName} 
			if (field == null) {
				throw new NoSuchFieldException("Field ${fieldName} not found in class properties or template fields")
			}
			else {
				if (field.type == TemplateFieldType.STRINGLIST && value.class == String) {
					// Convenience setter: find template item by name
					value = field.listEntries.find { it.name == value }
				}
				// Caution: this assumes that all template...Field Maps are already initialized
				// Otherwise, the results are pretty much unpredictable!
				getStore(field.type)[fieldName] = value
				return this
			}
		}
	}



	def Set<TemplateField> giveFields() {
		return this.template.fields;
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

}
