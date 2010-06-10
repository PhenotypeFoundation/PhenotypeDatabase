package dbnp.studycapturing
import java.lang.reflect.Method

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
	String description
	Class entity
	//nimble.User owner
	List fields
	static hasMany = [fields: TemplateField]

	static constraints = {
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
