package dbnp.studycapturing

import dbnp.data.Term
import org.springframework.validation.FieldError

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

	static mapping = {
		tablePerHierarchy false

		templateTextFields type: 'text'
	}	

	/**
	 * Constraints
	 *
	 * All template fields have their own custom validator. Note that there
	 * currently is a lot of code repetition. Ideally we don't want this, but
	 * unfortunately due to scope issues we cannot re-use the code. So make
	 * sure to replicate any changes to all pieces of logic! Only commented
	 * the first occurrence of the logic, please refer to the templateStringFields
	 * validator if you require information about the validation logic...
	 */
	static constraints = {
		template(nullable: true, blank: true)
		templateStringFields(validator: { fields, obj, errors ->
			// note that we only use 'fields' and 'errors', 'obj' is
			// merely here because it's the way the closure is called
			// by the validator...

			// define a boolean
			def error = false

			// iterate through fields
			fields.each { key, value ->
				// check if the value is of proper type
				if ( value && value.class != String ) {
					// it's of some other type
					try {
						// try to cast it to the proper type
						fields[key] = (value as String)
					} catch (Exception e) {
						// could not typecast properly, value is of improper type
						// add error message
						error = true
						errors.rejectValue(
							'templateStringFields',
							'templateEntity.typeMismatch.string',
							[key, value.class] as Object[],
							'Property {0} must be of type String and is currently of type {1}'
						)
					}
				}
			}

			// got an error, or not?
			return (!error)
		})
		templateTextFields(validator: { fields, obj, errors ->
			def error = false
			fields.each { key, value ->
				if ( value && value.class != String ) {
					try {
						fields[key] = (value as String)
					} catch (Exception e) {
						error = true
						errors.rejectValue(
							'templateTextFields',
							'templateEntity.typeMismatch.string',
							[key, value.class] as Object[],
							'Property {0} must be of type String and is currently of type {1}'
						)
					}
				}
			}
			return (!error)
		})
		templateStringListFields(validator: { fields, obj, errors ->
			def error = false
			fields.each { key, value ->
				if ( value && value.class != TemplateFieldListItem ) {
					try {
						fields[key] = (value as TemplateFieldListItem)
					} catch (Exception e) {
						error = true
						errors.rejectValue(
							'templateIntegerFields',
							'templateEntity.typeMismatch.templateFieldListItem',
							[key, value.class] as Object[],
							'Property {0} must be of type TemplateFieldListItem and is currently of type {1}'
						)
					}
				}
			}
			return (!error)
		})
		templateIntegerFields(validator: { fields, obj, errors ->
			def error = false
			fields.each { key, value ->
				if (value && value.class != Integer ) {
					try {
						fields[key] = (value as Integer)
					} catch (Exception e) {
						error = true
						errors.rejectValue(
							'templateIntegerFields',
							'templateEntity.typeMismatch.integer',
							[key, value.class] as Object[],
							'Property {0} must be of type Integer and is currently of type {1}'
						)
					}
				}
			}
			return (!error)
		})
		templateFloatFields(validator: { fields, obj, errors ->
			def error = false
			fields.each { key, value ->
				if ( value && value.class != Float ) {
					try {
						fields[key] = (value as Float)
					} catch (Exception e) {
						error = true
						errors.rejectValue(
							'templateFloatFields',
							'templateEntity.typeMismatch.float',
							[key, value.class] as Object[],
							'Property {0} must be of type Float and is currently of type {1}'
						)
					}
				}
			}
			return (!error)
		})
		templateDoubleFields(validator: { fields, obj, errors ->
			def error = false
			fields.each { key, value ->
				if ( value && value.class != Double ) {
					try {
						fields[key] = (value as Double)
					} catch (Exception e) {
						error = true
						errors.rejectValue(
							'templateDoubleFields',
							'templateEntity.typeMismatch.double',
							[key, value.class] as Object[],
							'Property {0} must be of type Double and is currently of type {1}'
						)
					}
				}
			}
			return (!error)
		})
		templateDateFields(validator: { fields, obj, errors ->
			def error = false
			fields.each { key, value ->
				if ( value && value.class != Date ) {
					try {
						fields[key] = (value as Date)
					} catch (Exception e) {
						error = true
						errors.rejectValue(
							'templateDateFields',
							'templateEntity.typeMismatch.date',
							[key, value.class] as Object[],
							'Property {0} must be of type Date and is currently of type {1}'
						)
					}
				}
			}
			return (!error)
		})
		templateTermFields(validator: { fields, obj, errors ->
			def error = false
			fields.each { key, value ->
				if ( value && value.class != Term ) {
					try {
						fields[key] = (value as Term)
					} catch (Exception e) {
						error = true
						errors.rejectValue(
							'templateTermFields',
							'templateEntity.typeMismatch.term',
							[key, value.class] as Object[],
							'Property {0} must be of type Term and is currently of type {1}'
						)
					}
				}
			}
			return (!error)
		})
	}

	/**
	 * Get the proper templateFields Map for a specific field type
	 * @param TemplateFieldType
	 * @return pointer
	 * @visibility private
	 * @throws NoSuchFieldException
	 */
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
	 * Check whether a given template field exists or not
	 * @param fieldName The name of the template field
	 * @return true if the given field exists and false otherwise
	 */
	def fieldExists(String fieldName) {
		TemplateFieldType fieldType = template.getFieldType(fieldName)

                // If the field is found, a TemplateFieldType is returned
                // otherwise null
                if (fieldType) {
                    return true
                } else {
                    return false
                }
	}

	/**
	 * Set a template/entity field value
	 * @param fieldName The name of the template or entity field
	 * @param value The value to be set, this should align with the (template) field type, but there are some convenience setters
	 */
	def setFieldValue(String fieldName, value) {
		// First, search if there is an entity property with the given name, and if so, set that
		if (this.properties.containsKey(fieldName)) {
			this[fieldName] = value			
		} else if (template == null) {
			// not the found, then it is a template field, so check if there is a template
			throw new NoSuchFieldException("Field ${fieldName} not found in class properties: template not set")
		} else {
			// there is a template, check the template fields
			// Find the target template field, if not found, throw an error
			TemplateField field = this.template.fields.find { it.name == fieldName }

			if (field == null) {
				// no such field
				throw new NoSuchFieldException("Field ${fieldName} not found in class template fields")
			} else {
				// Set the value of the found template field
				// Convenience setter for template string list fields: find TemplateFieldListItem by name
				if (field.type == TemplateFieldType.STRINGLIST && value && value.class == String) {
					// Kees insensitive pattern matching ;)
					value = field.listEntries.find { it.name ==~ /(?i)($value)/ }
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
				def store = getStore(field.type)
				if (!value && store[ fieldName ]) {
					println "removing " + ((super) ? super.class : '??') + " template field: " + fieldName

					// remove the item from the Map (if present)
					store.remove( fieldName )
				} else if (value) {
					println "setting " + ((super) ? super.class : '??') + " template field: " + fieldName + " ([" + value.toString() + "] of type [" + value.class + "])"

					// set value
					store[ fieldName ] = value
				}
				return this
			}
		}
	}

	/**
	* Return all templated fields defined in the underlying template of this entity
	*/
	def List<TemplateField> giveFields() {
		return this.template.fields;
	}

	/**
	 * Return all relevant 'built-in' domain fields of the super class
	 * @return key-value pairs describing the built-in fields, with the names as keys and type (as TemplateFieldType) as values
 	 */
	abstract Map giveDomainFields()
	/*def giveDomainFields() {
		def fieldSet = [:];
		if (super.hasProperty('name')) {
			fieldSet['name'] = TemplateFieldType.STRING;
		}
		return fieldSet;
	}*/

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
		} else if (templates.size() == 1) {
			return templates[0];
		} else {
			throw new NoSuchFieldException("Multiple templates found in collection!")
		}
	}
}
