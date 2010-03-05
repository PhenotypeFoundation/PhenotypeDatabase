package dbnp.studycapturing

import dbnp.data.Term
import org.codehaus.groovy.runtime.NullObject

class TemplateEntity {

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
		template(nullable: true)

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
			if (templateStringFields.containsKey(fieldName) && value.class == String) {
				this.templateStringFields[fieldName] = value
			}
			if (templateStringListFields.containsKey(fieldName) && value.class == TemplateFieldListItem) {
				// TODO: check if item really belongs to the list under fieldName
				this.templateStringListFields[fieldName] = value
			}
			if (templateTextFields.containsKey(fieldName) && value.class == String) {
				this.templateTextFields[fieldName] = value
			}
			else if (templateIntegerFields.containsKey(fieldName) && value.class == Integer) {
				this.templateIntegerFields[fieldName] = value
			}
			else if (templateFloatFields.containsKey(fieldName) && value.class == Float) {
				this.templateFloatFields[fieldName] = value
			}
			else if (templateDoubleFields.containsKey(fieldName) && value.class == Double) {
				this.templateDoubleFields[fieldName] = value
			}
			else if (templateDateFields.containsKey(fieldName) && value.class == Date) {
				this.templateDateFields[fieldName] = value
			}
			else if (templateTermFields.containsKey(fieldName) && value.class == Term) {
				this.templateTermFields[fieldName] = value
			}
			else {
				throw new NoSuchFieldException("Field ${fieldName} not found in class properties or template fields")
			}
		}
	}



	def Set<TemplateField> giveFields() {
		return this.template.fields;
	}

	@Override
	void setTemplate(Template newTemplate) {

		// Contrary to expectation, this method does not cause an infinite loop but calls the super method
		// whereas super.setTemplate(newTemplate) leads to errors concerning NullObject values
		this.template = newTemplate

		// TODO: initialize all template fields with the necessary keys and null values

		println "Setting template " + newTemplate
		if (template != null) {

			// Loop over all template field types and
			// That is inpossible in Java if the Maps are not yet set because the pointer is evaluated here

			// So this code is quite dangerous stuff:

			/*TemplateFieldType.list().each() { fieldType ->
				Set<TemplateFieldType> fields = template.getFieldsByType(fieldType)
				println fieldType
				println "before: " + getStore(fieldType)
				initFields(getStore(fieldType),fieldType.getDefaultValue(),fields)
				println "after: " + getStore(fieldType)

			}
			println "SF:" + templateStringListFields*/


			/*def type = TemplateFieldType.STRING
			//<T extends Annotation> T = type.getTypeClass().class
			def stringFields = template.getFieldsByType(TemplateFieldType.STRING)
			if (stringFields.size() > 0) {
				templateStringFields = new HashMap<String,String>(stringFields.size())
				stringFields.each {
					templateStringFields.put(it.name,TemplateFieldType.STRING.getDefaultValue())
				}
				println templateStringFields
			}
			stringFields = template.getFieldsByType(TemplateFieldType.INTEGER)
			println stringFields*.name
			if (stringFields.size() > 0) {
				templateIntegerFields = new HashMap<String,Integer>(stringFields.size())
				stringFields.each {
					templateIntegerFields.put(it.name,TemplateFieldType.INTEGER.getDefaultValue())
				}
			}*/
		}
	}

	// Private function to initialize template field collections
	private <T> void initFields(Map fieldCollection, T defaultValue, Set<TemplateFieldType> fields) {
		if (fields.size() > 0) {
			fieldCollection = new HashMap<String,T>(fields.size());
			fields.each {
				fieldCollection.put(it.name,defaultValue);
			}
			println fieldCollection
		}
	}

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
