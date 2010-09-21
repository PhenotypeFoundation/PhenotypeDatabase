package dbnp.studycapturing

import dbnp.data.Term
import org.springframework.validation.FieldError

/**
 * The TemplateEntity domain Class is a superclass for all template-enabled study capture entities, including
 * Study, Subject, Sample and Event. This class provides functionality for storing the different TemplateField
 * values and returning the combined list of 'domain fields' and 'template fields' of a TemplateEntity.
 * For an explanation of those terms, see the Template class.
 *
 * @see dbnp.studycapturing.Template
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
abstract class TemplateEntity extends Identity {
	/** The actual template of this TemplateEntity instance */
	Template template

	// Maps for storing the different template field values
	Map templateStringFields	= [:]
	Map templateTextFields		= [:]
	Map templateStringListFields= [:]
	Map templateIntegerFields	= [:]
	Map templateFloatFields		= [:]
	Map templateDoubleFields	= [:]
	Map templateDateFields		= [:]
	Map templateBooleanFields	= [:]
	Map templateTemplateFields	= [:]
	Map templateModuleFields	= [:]
	Map templateLongFields		= [:]

	// N.B. If you try to set Long.MIN_VALUE for a reltime field, an error will occur
	// However, this will never occur in practice: this value represents 3 bilion centuries
	Map templateRelTimeFields	= [:] // Contains relative times in seconds
	Map templateFileFields		= [:] // Contains filenames
	Map templateTermFields		= [:]

	// define relationships
	static hasMany = [
		templateStringFields	: String,
		templateTextFields		: String,
		templateStringListFields: TemplateFieldListItem,
		templateIntegerFields	: int,
		templateFloatFields		: float,
		templateDoubleFields	: double,
		templateDateFields		: Date,
		templateTermFields		: Term,
		templateRelTimeFields	: long,
		templateFileFields		: String,
		templateBooleanFields	: boolean,
		templateTemplateFields	: Template,
		templateModuleFields	: AssayModule,
		templateLongFields		: long,
		systemFields			: TemplateField
	]

	static mapping = {
		// Specify that each TemplateEntity-subclassing entity should have its own tables to store TemplateField values.
		// This results in a lot of tables, but performance is presumably better because in most queries, only values of
		// one specific entity will be retrieved. Also, because of the generic nature of these tables, they can end up
		// containing a lot of records (there is a record for each entity instance for each property, instead of a record
		// for each instance as is the case with 'normal' straightforward database tables. Therefore, it's better to split
		// out the data to many tables.
		tablePerHierarchy false

		// Make sure that the text fields are really stored as TEXT, so that those Strings can have an arbitrary length.
		templateTextFields type: 'text'
	}

	// Inject the service for storing files (for TemplateFields of TemplateFieldType FILE).
	def fileService

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
				if (value && value.class != String) {
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
				if (value && value.class != String) {
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
				if (value && value.class != TemplateFieldListItem) {
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
				if (value && value.class != Integer) {
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
				if (value && value.class != Float) {
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
				if (value && value.class != Double) {
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
				if (value && value.class != Date) {
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
		templateRelTimeFields(validator: { fields, obj, errors ->
			def error = false
			fields.each { key, value ->
				if (value && value == Long.MIN_VALUE) {
					error = true
					errors.rejectValue(
						'templateRelTimeFields',
						'templateEntity.typeMismatch.reltime',
						[key, value] as Object[],
						'Value cannot be parsed for property {0}'
					)
				} else if (value && value.class != long) {
					try {
						fields[key] = (value as long)
					} catch (Exception e) {
						error = true
						errors.rejectValue(
							'templateRelTimeFields',
							'templateEntity.typeMismatch.reltime',
							[key, value.class] as Object[],
							'Property {0} must be of type long and is currently of type {1}'
						)
					}
				}
			}
			return (!error)
		})
		templateTermFields(validator: { fields, obj, errors ->
			def error = false
			fields.each { key, value ->
				if (value && value.class != Term) {
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
		templateFileFields(validator: { fields, obj, errors ->
			// note that we only use 'fields' and 'errors', 'obj' is
			// merely here because it's the way the closure is called
			// by the validator...

			// define a boolean
			def error = false

			// iterate through fields
			fields.each { key, value ->
				// check if the value is of proper type
				if (value && value.class != String) {
					// it's of some other type
					try {
						// try to cast it to the proper type
						fields[key] = (value as String)

						// Find the file on the system
						// if it does not exist, the filename can
						// not be entered

					} catch (Exception e) {
						// could not typecast properly, value is of improper type
						// add error message
						error = true
						errors.rejectValue(
							'templateFileFields',
							'templateEntity.typeMismatch.file',
							[key, value.class] as Object[],
							'Property {0} must be of type String and is currently of type {1}'
						)
					}
				}
			}

			// got an error, or not?
			return (!error)
		})
		templateBooleanFields(validator: { fields, obj, errors ->
			def error = false
			fields.each { key, value ->
				if (value) {
					fields[key] = true;
				} else {
					fields[key] = false;
				}
			}
			return (!error)
		})
		templateTemplateFields(validator: { fields, obj, errors ->
			def error = false
			fields.each { key, value ->
				if (value && value.class != Template) {
					try {
						fields[key] = (value as Template)
					} catch (Exception e) {
						error = true
						errors.rejectValue(
							'templateTemplateFields',
							'templateEntity.typeMismatch.template',
							[key, value.class] as Object[],
							'Property {0} must be of type Template and is currently of type {1}'
						)
					}
				}
			}
			return (!error)
		})
		templateModuleFields(validator: { fields, obj, errors ->
			def error = false
			fields.each { key, value ->
				if (value && value.class != AssayModule) {
					try {
						fields[key] = (value as AssayModule)
					} catch (Exception e) {
						error = true
						errors.rejectValue(
							'templateModuleFields',
							'templateEntity.typeMismatch.module',
							[key, value.class] as Object[],
							'Property {0} must be of type AssayModule and is currently of type {1}'
						)
					}
				}
			}
			return (!error)
		})
		templateLongFields(validator: { fields, obj, errors ->
			def error = false
			fields.each { key, value ->
				if (value && value.class != Long) {
					try {
						fields[key] = Long.parseLong(value.trim())
					} catch (Exception e) {
						error = true
						errors.rejectValue(
							'templateLongFields',
							'templateEntity.typeMismatch.long',
							[key, value.class] as Object[],
							'Property {0} must be of type Long and is currently of type {1}'
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
	public Map getStore(TemplateFieldType fieldType) {
		switch (fieldType) {
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
			case TemplateFieldType.RELTIME:
				return templateRelTimeFields
			case TemplateFieldType.FILE:
				return templateFileFields
			case TemplateFieldType.FLOAT:
				return templateFloatFields
			case TemplateFieldType.DOUBLE:
				return templateDoubleFields
			case TemplateFieldType.ONTOLOGYTERM:
				return templateTermFields
			case TemplateFieldType.BOOLEAN:
				return templateBooleanFields
			case TemplateFieldType.TEMPLATE:
				return templateTemplateFields
			case TemplateFieldType.MODULE:
				return templateModuleFields
			case TemplateFieldType.LONG:
				return templateLongFields
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
		String escapedLowerCaseFieldName = fieldName.toLowerCase().replaceAll("([^a-z0-9])", "_")

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

		if (isDomainField(fieldName)) {
			return this[fieldName]
		}
		else {
			TemplateField field = getField(this.giveTemplateFields(), fieldName)
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
			TemplateField field = getField(this.giveFields(), fieldName)
			// return true if exception is not thrown (but double check if field really is not null)
			if (field) {
				return true
			}
			else {
				return false
			}
		}
		// if exception is thrown, return false
		catch (NoSuchFieldException e) {
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
		TemplateField field = getField(this.giveFields(), fieldName)

		// Convenience setter for boolean fields
		if( field.type == TemplateFieldType.BOOLEAN && value && value.class == String ) {
			def lower = value.toLowerCase()
			if (lower.equals("true") || lower.equals("on") || lower.equals("x")) {
				value = true
			}
			else if (lower.equals("false") || lower.equals("off") || lower.equals("")) {
				value = false
			}
			else {
				throw new IllegalArgumentException("Boolean string not recognized: ${value} when setting field ${fieldName}")
			}
		}

		// Convenience setter for template string list fields: find TemplateFieldListItem by name
		if (field.type == TemplateFieldType.STRINGLIST && value && value.class == String) {
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
					parser += dateMatch[0][8] + 'HH:mm'
				}

				value = new Date().parse(parser, value)
			}
		}

		// Magic setter for relative times: handle string values for relTime fields
		//
		if (field.type == TemplateFieldType.RELTIME && value != null && value.class == String) {
			// A string was given, attempt to transform it into a timespan
			// If it cannot be parsed, set the lowest possible value of Long.
			// The validator method will raise an error
			//
			// N.B. If you try to set Long.MIN_VALUE itself, an error will occur
			// However, this will never occur: this value represents 3 bilion centuries
			try {
				value = RelTime.parseRelTime(value).getValue();
			} catch (IllegalArgumentException e) {
				value = Long.MIN_VALUE;
			}
		}

		// Sometimes the fileService is not created yet
		if (!fileService) {
			fileService = new FileService();
		}

		// Magic setter for files: handle values for file fields
		//
		// If NULL is given, the field value is emptied and the old file is removed
		// If an empty string is given, the field value is kept as was
		// If a file is given, it is moved to the right directory. Old files are deleted. If
		//   the file does not exist, the field is kept
		// If a string is given, it is supposed to be a file in the upload directory. If
		//   it is different from the old one, the old one is deleted. If the file does not
		//   exist, the old one is kept.
		if (field.type == TemplateFieldType.FILE) {
			def currentFile = getFieldValue(field.name);

			if (value == null) {
				// If NULL is given, the field value is emptied and the old file is removed
				value = "";
				if (currentFile) {
					fileService.delete(currentFile)
				}
			} else if (value.class == File) {
				// a file was given. Attempt to move it to the upload directory, and
				// afterwards, store the filename. If the file doesn't exist
				// or can't be moved, "" is returned
				value = fileService.moveFileToUploadDir(value);

				if (value) {
					if (currentFile) {
						fileService.delete(currentFile)
					}
				} else {
					value = currentFile;
				}
			} else if (value == "") {
				value = currentFile;
			} else {
				if (value != currentFile) {
					if (fileService.fileExists(value)) {
						// When a FILE field is filled, and a new file is set
						// the existing file should be deleted
						if (currentFile) {
							fileService.delete(currentFile)
						}
					} else {
						// If the file does not exist, the field is kept
						value = currentFile;
					}
				}
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
				// TODO: search ontology for the term online (it may still exist) and insert it into the Term cache
				// if not found, throw exception
				else {
					throw new IllegalArgumentException("Ontology term not recognized (not in the GSCF ontology cache): ${value} when setting field ${fieldName}")
				}
			}
		}

		// Magic setter for TEMPLATE fields
		if (field.type == TemplateFieldType.TEMPLATE && value && value.class == String) {
			value = Template.findByName(value)
		}

		// Magic setter for MODULE fields
		if (field.type == TemplateFieldType.MODULE && value && value.class == String) {
			value = AssayModule.findByName(value)
		}

		// Magic setter for LONG fields
		if (field.type == TemplateFieldType.LONG && value && value.class == String) {
			// TODO, check for invalids?
			value = Long.parseLong(value.trim())
		}

		// Set the field value
		if (isDomainField(field)) {
			// got a value?
			if (value) {
				println ".setting [" + ((super) ? super.class : '??') + "] ("+getIdentifier()+") domain field: [" + fieldName + "] ([" + value.toString() + "] of type [" + value.class + "])"
				this[field.name] = value
			} else {
				println ".unsetting [" + ((super) ? super.class : '??') + "] ("+getIdentifier()+") domain field: [" + fieldName + "]"

				// remove value. For numbers, this is done by setting
				// the value to 0, otherwise, setting it to NULL
				switch (field.type.toString()) {
					case ['INTEGER', 'FLOAT', 'DOUBLE', 'RELTIME', 'LONG']:
						this[field.name] = 0;
						break;
					case [ 'BOOLEAN' ]:
						this[field.name] = false;
						break;
					default:
						this[field.name] = null
				}
			}
		} else {
			// Caution: this assumes that all template...Field Maps are already initialized (as is done now above as [:])
			// If that is ever changed, the results are pretty much unpredictable (random Java object pointers?)!
			def store = getStore(field.type)

			// If some value is entered (or 0 or BOOLEAN false), then save the value
			// otherwise, it should not be present in the store, so
			// it is unset if it is.
			if (value || value == 0 || ( field.type == TemplateFieldType.BOOLEAN && value == false)) {
				println ".setting [" + ((super) ? super.class : '??') + "] ("+getIdentifier()+") template field: [" + fieldName + "] ([" + value.toString() + "] of type [" + value.class + "])"

				// set value
				store[fieldName] = value
			} else if (store[fieldName]) {
				println ".unsetting [" + ((super) ? super.class : '??') + "] ("+getIdentifier()+") template field: [" + fieldName + "]"

				// remove the item from the Map (if present)
				store.remove(fieldName)
			}
		}

		return this
	}

	/**
	 * Check if a given field is a domain field
	 * @param TemplateField field instance
	 * @return boolean
	 */
	boolean isDomainField(TemplateField field) {
		return isDomainField(field.name)
	}

	/**
	 * Check if a given field is a domain field
	 * @param String field name
	 * @return boolean
	 */
	boolean isDomainField(String fieldName) {
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
		return (this.template) ? this.template.fields : []
	}

	/**
	 * Look up the type of a certain template field
	 * @param String fieldName The name of the template field
	 * @return String The type (static member of TemplateFieldType) of the field, or null of the field does not exist
	 */
	TemplateFieldType giveFieldType(String fieldName) {
		def field = giveFields().find {
			it.name == fieldName
		}
		field?.type
	}

	/**
	 * Return all relevant 'built-in' domain fields of the super class. Should be implemented by a static method
	 * @return List with DomainTemplateFields
	 * @see TemplateField
	 */
	abstract List<TemplateField> giveDomainFields()

	/**
	 * Convenience method. Returns all unique templates used within a collection of TemplateEntities.
	 *
	 * If the collection is empty, an empty set is returned. If none of the entities contains
	 * a template, also an empty set is returned.
	 */
	static Collection<Template> giveTemplates(Collection<TemplateEntity> entityCollection) {
		def set = entityCollection*.template?.unique();

		// If one or more entities does not have a template, the resulting
		// set contains null. That is not what is meant.
		return set.findAll { it != null };
	}

	/**
	 * Convenience method. Returns the template used within a collection of TemplateEntities.
	 * @throws NoSuchFieldException when 0 or multiple templates are used in the collection
	 * @return The template used by all members of a collection
	 */
	static Template giveSingleTemplate(Collection<TemplateEntity> entityCollection) {
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