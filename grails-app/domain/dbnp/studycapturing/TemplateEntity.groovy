package dbnp.studycapturing

import dbnp.data.Term

class TemplateEntity {

	Map templateStringFields
	Map templateIntegerFields
	Map templateFloatFields
	Map templateDoubleFields
	Map templateDateFields
	Map templateTermFields

	static hasMany = [
		templateStringFields: String, // stores both STRING and STRINGLIST items (latter should be checked against the list)
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
		TemplateFieldType fieldType = template.getSubjectFieldType(fieldName)
		if (!fieldType) throw new NoSuchFieldException("Field name ${fieldName} not recognized")
		switch(fieldType) {
			case [TemplateFieldType.STRING, TemplateFieldType.STRINGLIST]:
				return templateStringFields[fieldName]
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
		this.properties.each { println it}
		if (this.properties.containsKey(fieldName)) {
			this[fieldName] = value
		}
		else if (templateStringFields.containsKey(fieldName) && value.class == String) {
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
			println "Field ${fieldName} not found"
		}
	}

}
