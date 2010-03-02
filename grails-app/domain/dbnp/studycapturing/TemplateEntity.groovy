package dbnp.studycapturing

import dbnp.data.Term
import org.codehaus.groovy.runtime.NullObject

class TemplateEntity {

	Template template

	Map templateStringFields
	Map templateStringListFields
	Map templateIntegerFields
	Map templateFloatFields
	Map templateDoubleFields
	Map templateDateFields
	Map templateTermFields

	static hasMany = [
		templateStringFields: String,
		templateStringListFields: TemplateFieldListItem,
		templateIntegerFields: int,
		templateFloatFields: float,
		templateDoubleFields: double,
		templateDateFields: Date,
		templateTermFields: Term
	]

	static constraints = {
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
		switch(fieldType) {
			case [TemplateFieldType.STRING, TemplateFieldType.STRINGLIST]:
				return templateStringFields[fieldName]
			case [TemplateFieldType.STRINGLIST]:
				return templateStringListFields[fieldName]
			case TemplateFieldType.INTEGER:
				return templateIntegerFields[fieldName]
			case TemplateFieldType.DATE:
				return templateDateFields[fieldName]
			case TemplateFieldType.FLOAT:
				return templateFloatFields[fieldName]
			case TemplateFieldType.DOUBLE:
				return templateDoubleFields[fieldName]
			case TemplateFieldType.ONTOLOGYTERM:
				return templateTermFields[fieldName]
		        default:
				throw new NoSuchFieldException("Field type ${fieldType} not recognized")
		}
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
				this.templateStringFields[fieldName] = value
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
		/*if (template == null || template instanceof NullObject) {} else{ // negation doesn't seem to work well
			def stringFields = template.getFieldsByType(TemplateFieldType.STRINGLIST)
			println stringFields*.name
			if (stringFields.size() > 0) {
				templateStringFields = new HashMap<String,String>()
				templateStringFields.keyset.add stringFields*.name;
				println templateStringFields
			}
		}*/
	}

}
