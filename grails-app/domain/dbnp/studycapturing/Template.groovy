package dbnp.studycapturing

/**
 * The Template class describes a study template, which is basically an extension of the study capture entities
 * in terms of extra fields (described by classes that extend the TemplateField class).
 * At this moment, only extension of the subject entity is implemented.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Template implements Serializable {
	String name
	//nimble.User owner

	static hasMany = [studyFields: TemplateStudyField, subjectFields: TemplateSubjectField]

	static constraints = {
		name(unique: true)
	}

	def String toString() {
		return this.name;
	}

	/**
	 * Look up the type of a certain template subject field
	 * @param fieldName The name of the template field
	 * @return The type (static member of TemplateFieldType) of the field, or null of the field does not exist
	 */
	def TemplateFieldType getSubjectFieldType(String fieldName) {
		def field = subjectFields.find {
			it.name == fieldName	
		}
		field?.type
	}
}
