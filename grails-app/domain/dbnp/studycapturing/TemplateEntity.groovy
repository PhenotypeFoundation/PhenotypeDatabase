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
		templateTermFields: Term,
		systemFields: TemplateField
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
	 * Find a field domain or template field by its name and return its description
	 * @param fieldsCollection the set of fields to search in, usually something like this.giveFields()
	 * @param fieldName The name of the domain or template field
	 * @return the TemplateField description of the field
	 * @throws NoSuchFieldException If the field is not found or the field type is not supported
	 */
	private static TemplateField getField(List<TemplateField> fieldsCollection, String fieldName) {
		// escape the fieldName for easy matching
		// (such escaped names are commonly used
		// in the HTTP forms of this application)
		String escapedLowerCaseFieldName = fieldName.toLowerCase().replaceAll("([^a-z0-9])","_")

		// Find the target template field, if not found, throw an error
		TemplateField field = fieldsCollection.find { it.name.toLowerCase().replaceAll("([^a-z0-9])", "_") == escapedLowerCaseFieldName }

		if (field) {
			return field
		}
		else {
			throw new NoSuchFieldException("Field ${fieldName} not recognized")
		}
	}

	/**
	 * Find a domain or template field by its name and return its value for this entity
	 * @param fieldName The name of the domain or template field
	 * @return the value of the field (class depends on the field type)
	 * @throws NoSuchFieldException If the field is not found or the field type is not supported
	 */
	def getFieldValue(String fieldName) {
		TemplateField field = getField(this.giveFields(),fieldName)
		if (isDomainField(field)) {
			return this[field.name]
		}
		else {
			return getStore(field.type)[fieldName]
		}
	}

	/**
	 * Check whether a given template field exists or not
	 * @param fieldName The name of the template field
	 * @return true if the given field exists and false otherwise
	 */
	boolean fieldExists(String fieldName) {
		// getField should throw a NoSuchFieldException if the field does not exist
		try {
			TemplateField field = getField(this.giveFields(),fieldName)
			// return true if exception is not thrown (but double check if field really is not null)
			if (field) {
				return true
			}
			else {
				return false
			}
		}
		// if exception is thrown, return false
		catch(NoSuchFieldException e) {
			return false
		}
	}

	/**
	 * Set a template/entity field value
	 * @param fieldName The name of the template or entity field
	 * @param value The value to be set, this should align with the (template) field type, but there are some convenience setters
	 */
	def setFieldValue(String fieldName, value) {
		// get the template field
		TemplateField field = getField(this.giveFields(),fieldName)

		// Convenience setter for template string list fields: find TemplateFieldListItem by name
		if (field.type == TemplateFieldType.STRINGLIST && value && value.class == String) {
			// Kees insensitive pattern matching ;)
			def escapedLowerCaseValue = value.toLowerCase().replaceAll("([^a-z0-9])", "_")
			value = field.listEntries.find {
				it.name.toLowerCase().replaceAll("([^a-z0-9])", "_") == escapedLowerCaseValue
			}
		}

		// Magic setter for dates: handle string values for date fields
		if (field.type == TemplateFieldType.DATE && value && value.class == String) {
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

		// Magic setter for ontology terms: handle string values
		if (field.type == TemplateFieldType.ONTOLOGYTERM && value && value.class == String) {
			// iterate through ontologies and find term
			field.ontologies.each() { ontology ->
				def term = ontology.giveTermByName(value)

				// found a term?
				if (term) {
					value = term
				}
			}
		}

		// Set the field value
		if (isDomainField(field)) {
			// got a value?
			if (value) {
				println "setting [" + ((super) ? super.class : '??') + "] domain field: [" + fieldName + "] ([" + value.toString() + "] of type [" + value.class + "])"

				// set value
				this[field.name] = value
			} else {
				println "removing [" + ((super) ? super.class : '??') + "] domain field: [" + fieldName + "]"

				// remove value
				this[field.name] = null
			}
		} else {
			// Caution: this assumes that all template...Field Maps are already initialized (as is done now above as [:])
			// If that is ever changed, the results are pretty much unpredictable (random Java object pointers?)!
			def store = getStore(field.type)
			if (!value && store[fieldName]) {
				println "removing [" + ((super) ? super.class : '??') + "] template field: [" + fieldName + "]"

				// remove the item from the Map (if present)
				store.remove(fieldName)
			} else if (value) {
				println "setting [" + ((super) ? super.class : '??') + "] template field: [" + fieldName + "] ([" + value.toString() + "] of type [" + value.class + "])"

				// set value
				store[fieldName] = value
			}
		}

		return this
	}

	/**
	 * Check if a given field is a domain field
	 * @param TemplateField		field instance
	 * @return boolean
	 */
	boolean isDomainField(TemplateField field) {
		return this.giveDomainFields()*.name.contains(field.name)
	}

	/**
	 * Check if a given field is a domain field
	 * @param String	field name
	 * @return boolean
	 */	boolean isDomainField(String fieldName) {
		return this.giveDomainFields()*.name.contains(fieldName)
	}

	/**
	 * Return all fields defined in the underlying template and the built-in
     * domain fields of this entity
	 */
	def List<TemplateField> giveFields() {
		return this.giveDomainFields() + this.giveTemplateFields();
	}

	/**
	 * Return all templated fields defined in the underlying template of this entity
	 */
	def List<TemplateField> giveTemplateFields() {
		return this.template.fields;
	}

	/**
	 * Return all relevant 'built-in' domain fields of the super class
	 * @return List with DomainTemplateFields
     * @see TemplateField
 	 */
	abstract List<TemplateField> giveDomainFields()

	/**
	 * Convenience method. Returns all unique templates used within a collection of TemplateEntities.
	 */
	static Set<Template> giveTemplates(Set<TemplateEntity> entityCollection) {
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