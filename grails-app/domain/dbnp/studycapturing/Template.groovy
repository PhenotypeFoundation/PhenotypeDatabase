package dbnp.studycapturing

/**
 * The Template class describes a TemplateEntity template, which is basically an extension of the study capture entities
 * in terms of extra fields (which are described by classes that extend the TemplateField class).
 * Study, Subject, Sample and Event are all TemplateEntities.
 *
 * Within a Template, we have two different types of fields: 'domain fields' and 'template fields'.
 * The domain fields are TemplateFields which are given by the TemplateEntity itself and therefore always present
 * in any instance of that TemplateEntity. They are specified by implementing TemplateEntity.giveDomainFields()
 * The template fields are TemplateFields which are added specifically by the Template. They are specified
 * in the fields property of the Template object which is referenced by the TemplateEntity.template property.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Template implements Serializable {

	/** The name of the template */
	String name

	/** A string describing the template to other users */
	String description

	/** The target TemplateEntity for this template */
	Class entity

	/** The owner of the template. If the owner is not defined, it is a shared/public template */
	nimble.User owner

	/** The template fields which are the members of this template. This is a List to preserve the field order */
	List fields
	static hasMany = [fields: TemplateField]

	static constraints = {

		owner(nullable: true, blank: true)
		description(nullable: true, blank: true)

		fields(validator: { fields, obj, errors ->
			// 'obj' refers to the actual Template object

			// define a boolean
			def error = false

			// iterate through fields
			fields.each { field ->
				// check if the field entity is the same as the template entity
				if (!field.entity.equals(obj.entity)) {
					error = true
					errors.rejectValue(
						'fields',
						'templateEntity.entityMismatch',
						[field.name, obj.entity, field.entity] as Object[],
						'Template field {0} must be of entity {1} and is currently of entity {2}'
						)
				}
			}

			// got an error, or not?
			return (!error)
		})

		// outcommented for now due to bug in Grails / Hibernate
		// see http://jira.codehaus.org/browse/GRAILS-6020
		// This is to verify that the template name is unique with respect to the parent entity.
		// TODO: this probably has to change in the case of private templates of different users,
		// which can co-exist with the same name. See also TemplateField
		//	name(unique:['entity'])
	}

	/**
	 * overloaded toString method
	 * @return String
	 */
	def String toString() {
		return this.name;
	}

	/**
	 * Look up the type of a certain template subject field
	 * @param String fieldName The name of the template field
	 * @return String	The type (static member of TemplateFieldType) of the field, or null of the field does not exist
	 */
	def TemplateFieldType getFieldType(String fieldName) {
		def field = fields.find {
			it.name == fieldName
		}
		field?.type
	}

	/**
	 * get all field of a particular type
	 * @param Class fieldType
	 * @return Set < TemplateField >
	 */
	def getFieldsByType(TemplateFieldType fieldType) {
		def result = fields.findAll {
			it.type == fieldType
		}
		return result;
	}

	/**
	 * get all required fields
	 * @param Class fieldType
	 * @return Set < TemplateField >
	 */
	def getRequiredFields() {
		def result = fields.findAll {
			it.required == true
		}
		return result;
	}

	/**
	 * Checks whether this template is used by any object
	 *
	 * @returns		true iff this template is used by any object, false otherwise
	 */
	def inUse() {
		return (numUses() > 0 );
	}

	/**
	 * The number of objects that use this template
	 *
	 * @returns		the number of objects that use this template.
	 */
	def numUses() {
		// This template can only be used in objects of the right entity. Find objects of that
		// entity and see whether they use this method.
		//
		// Unfortunately, due to the grails way of creating classes, we can not use reflection for this
		def elements;
		switch( this.entity ) {
			case Event:
				elements = Event.findAllByTemplate( this ); break;
			case Sample:
				elements = Sample.findAllByTemplate( this ); break;
			case Study:
				elements = Study.findAllByTemplate( this ); break;
			case Subject:
				elements = Subject.findAllByTemplate( this ); break;
			default:
				return 0;
		}

		return elements.size();
	}

	/**
	 * overloading the findAllByEntity method to make it function as expected
	 * @param Class entity (for example: dbnp.studycapturing.Subject)
	 * @return ArrayList
	 */
	public static findAllByEntity(java.lang.Class entity) {
		def results = []
		// 'this' should not work in static context, so taking Template instead of this
		Template.findAll().each() {
			if (entity.equals(it.entity)) {
				results[results.size()] = it
			}
		}

		return results
	}
}
