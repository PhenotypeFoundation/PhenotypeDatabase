package dbnp.studycapturing

/**
 * The Template class describes a study template, which is basically an extension of the study capture entities
 * in terms of extra fields (described by classes that extend the TemplateField class).
 * At this moment, only extension of the study and subject entities is implemented.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Template implements Serializable {

	String name
	Class entity
	//nimble.User owner

	static hasMany = [fields: TemplateField]

	static constraints = {
		name(unique:['entity'])

	}

	def String toString() {
		return this.name;
	}

	/**
	 * Look up the type of a certain template subject field
	 * @param fieldName The name of the template field
	 * @return The type (static member of TemplateFieldType) of the field, or null of the field does not exist
	 */
	def TemplateFieldType getFieldType(String fieldName) {
		def field = fields.find {
			it.name == fieldName	
		}
		field?.type
	}

	def getFieldsByType(TemplateFieldType fieldType) {
		def result = fields.findAll {
			it.type == fieldType
		}
		return result;
	}
}
