/**
 *  GDT, a plugin for Grails Domain Templates
 *  Copyright (C) 2011 Jeroen Wesbeek
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  $Author$
 *  $Rev$
 *  $Date$
 */
package org.dbnp.gdt

/**
 * The TemplateEntity domain Class is a superclass for all template-enabled study capture entities, including
 * Study, Subject, Sample and Event. This class provides functionality for storing the different TemplateField
 * values and returning the combined list of 'domain fields' and 'template fields' of a TemplateEntity.
 * For an explanation of those terms, see the Template class.
 *
 * @see package org.dbnp.gdt.Template
 *
 * Revision information:
 * $Rev: 1284 $
 * $Author: work@osx.eu $
 * $Date: 2010-12-20 15:48:26 +0100 (Mon, 20 Dec 2010) $
 */
abstract class TemplateEntity extends Identity {
	def gdtService

	// allow the usage of searchable, set to
	// false by default
	static searchable = false

	// The actual template of this TemplateEntity instance
	Template template

	// Maps for storing the different template field values
	// are dynamically injected at compile time, e.g.:
	//			Map templateLongFields = [:]
	// @see org.dbnp.gdt.ast.TemplateEntityASTTransformation
    //
    // as of 20130118 we have removed the AST transformations
    // see ticket https://github.com/PhenotypeFoundation/GSCF/issues/64
    // therefore the maps are now hardcoded and not injected anymore
    Map templateStringFields		        = [:]
    Map templateTextFields			        = [:]
    Map templateExtendableStringListFields	= [:]
    Map templateStringListFields	        = [:]
    Map templateDoubleFields		        = [:]
    Map templateDateFields			        = [:]
    Map templateBooleanFields		        = [:]
    Map templateTemplateFields		        = [:]
    Map templateModuleFields		        = [:]
    Map templateLongFields			        = [:]
    Map templateRelTimeFields		        = [:] // Contains relative times in seconds
    Map templateFileFields			        = [:] // Contains filenames
    Map templateTermFields			        = [:]
    // end addition

	/**
	 * define relationships, note that this is dynamically
	 * extended at compile time, e.g.:
	 * 		templateLongFields: Long
	 * @see org.dbnp.gdt.ast.TemplateEntityASTTransformation
	 */
	// static hasMany = [:]
    // as of 20130118 we have removed the AST transformations
    // see ticket https://github.com/PhenotypeFoundation/GSCF/issues/64
    // therefore the the hasmany map is not being injected anymore
    static hasMany = [
            templateStringFields		        : String,
            templateTextFields			        : String,
            templateExtendableStringListFields  : TemplateFieldListItem,
            templateStringListFields	        : TemplateFieldListItem,
            templateDoubleFields		        : double,
            templateDateFields			        : Date,
            templateTermFields			        : Term,
            templateRelTimeFields		        : long,
            templateFileFields			        : String,
            templateBooleanFields		        : boolean,
            templateTemplateFields		        : Template,
            templateModuleFields		        : AssayModule,
            templateLongFields			        : long
//            ,
//            systemFields				        : TemplateField
    ]

    /**
	 * define what properties should be fuzzy searchable
	 * see org.dbnp.gdt.FuzzyStringMatchController and Service
	 */
	static fuzzyStringMatchable = [ ]

	// remember required fields when
	// so we can validate is the required
	// template fields are set
	Template requiredFieldsTemplate	= null
	Set requiredFields				= []

	/**
	 * Get the required fields for the defined template, currently
	 * this method is called in custom validators below but it's
	 * best to call it in a template setter method. But as that
	 * involves a lot of refactoring this implementation will do
	 * fine for now.
	 *
	 * Another possible issue might be that if the template is
	 * updated after the required fields are cached in the object.
	 *
	 * @return Set 	requiredFields
	 */
	final Set getRequiredFields() {
		// check if template is set
		if (template && !template.equals(requiredFieldsTemplate)) {
			// template has been set or was changed, fetch
			// required fields for this template
			requiredFields			= template.getRequiredFields()
			requiredFieldsTemplate	= template
		} else if (!template) {
			// template is not yet set, or was reset
			requiredFieldsTemplate	= null
			requiredFields			= []
		}


		// return the required fields
		return requiredFields
	}

	// overload transients from Identity and append requiredFields vars
	static transients	= [ "identifier", "iterator", "maximumIdentity", "requiredFields", "requiredFieldsTemplate", "searchable" ]

	// define the mapping
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

	/**
	 * Constraints
	 * note that this is dynamically extended at compile time for every
	 * templateField. E.g.: templateLongFields(validator: TemplateLongField.validator)
	 * @see org.dbnp.gdt.ast.TemplateEntityASTTransformation
	 */
    // as of 20130118 we have removed the AST transformations
    // see ticket https://github.com/PhenotypeFoundation/GSCF/issues/64
    // therefore constraints are not automatically being injected anymore
    static constraints = {
		template(nullable: true, blank: true)
		requiredFields(nullable: true)
		requiredFieldsTemplate(nullable: true)	
        // addition as of 20130118
        templateStringFields                (validator	: TemplateStringField.validator)
        templateTextFields                  (validator	: TemplateTextField.validator)
        templateExtendableStringListFields  (validator	: TemplateExtendableStringListField.validator)
        templateStringListFields            (validator	: TemplateStringListField.validator)
        templateDoubleFields                (validator	: TemplateDoubleField.validator)
        templateDateFields                  (validator	: TemplateDateField.validator)
        templateRelTimeFields               (validator	: TemplateRelTimeField.validator)
        templateTermFields                  (validator	: TemplateOntologyTermField.validator)
        templateFileFields                  (validator	: TemplateFileField.validator)
        templateBooleanFields               (validator	: TemplateBooleanField.validator)
        templateTemplateFields              (validator	: TemplateTemplateField.validator)
        templateModuleFields                (validator	: TemplateModuleField.validator)
        templateLongFields                  (validator	: TemplateLongField.validator)
        // end addition
	}

	/**
	 * Get the proper templateFields Map for a specific field type
	 * @param TemplateFieldType
	 * @return pointer
	 * @visibility private
	 * @throws NoSuchFieldException
	 */
	public Map getStore(TemplateFieldType fieldType) {
		try {
			return this."template${fieldType.casedName}Fields"
		} catch (Exception e) {
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
	public static TemplateField getField(List<TemplateField> fieldsCollection, String fieldName) throws NoSuchFieldException {
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
	public getFieldValue(String fieldName) throws NoSuchFieldException {
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
		setFieldValue(fieldName, value, false)
	}
	def setFieldValue(String fieldName, value, Boolean throwException) throws Exception {
		// get the template field
		def TemplateField field	= getField(this.giveFields(), fieldName)
		def templateFieldClass	= gdtService.getTemplateFieldTypeByCasedName(field.type.casedName)
		def currentValue		= getFieldValue( fieldName )

		// cast the value to the proper type, if required
		try {
			value = templateFieldClass.castValue(field, value, currentValue)
			// println " -> ${value} (${value?.class})"
		} catch (Exception e) {
			// the value could not be cast, keep the value as-is
			def errorMessage = "Error casting ${field.name} of type ${field.type.casedName} with value ${value} (${value?.class}) :: " + e.getMessage()
			if (throwException) {
			    // propagate the exception to the calling code
				throw new Exception(errorMessage)
			} else if (log) {
				// do not propagate the exception as the dynamic validators will notify
				// the user the value was wrong
				log.error errorMessage
			} else {
				// do not propagate the exception as the dynamic validators will notify
				// the user the value was wrong
				println errorMessage
			}
		}

		// set the field value
		if (isDomainField(field)) {
			// domain field
			this[field.name] = value
		} else {
			// template field
			def templateFieldStore = getStore(field.type)

			if (value || value == 0) {
				templateFieldStore[fieldName] = value
			} else {
				templateFieldStore.remove(fieldName)
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

	def TemplateField getField( fieldName ) {
		return getField(this.giveFields(), fieldName);
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
		set = set.findAll { it != null };

		// Sort the list so we always have the same order
		set = set.sort{ a, b ->
			a == null || b == null || a.equals(b) ? 0 :
			a.name < b.name ? -1 :
			a.name > b.name ?  1 :
			a.id < b.id ? -1 : 1
		}

		return set
	}

	/**
	 * Convenience method. Returns the template used within a collection of TemplateEntities.
	 * @throws NoSuchFieldException when 0 or multiple templates are used in the collection
	 * @return The template used by all members of a collection
	 */
	static Template giveSingleTemplate(Collection<TemplateEntity> entityCollection) throws NoSuchFieldException {
		def templates = giveTemplates(entityCollection);
		if (templates.size() == 0) {
			throw new NoSuchFieldException("No templates found in collection!")
		} else if (templates.size() == 1) {
			return templates[0];
		} else {
			throw new NoSuchFieldException("Multiple templates found in collection!")
		}
	}

    /**
     * Returns a Class object given by the entityname, but only if it is a subclass of TemplateEntity
	 *
	 * @return A class object of the given entity, null if the entity is not a subclass of TemplateEntity
	 * @throws ClassNotFoundException
     */
    static Class parseEntity( String entityName ) {
		if( entityName == null )
			return null

        // Find the templates
        def entity = Class.forName(entityName, true, Thread.currentThread().getContextClassLoader())

        // succes, is entity an instance of TemplateEntity?
        if (entity?.superclass =~ /TemplateEntity$/ || entity?.superclass?.superclass =~ /TemplateEntity$/) {
            return entity;
        } else {
            return null;
        }
    }

	/**
	 * set template
	 * @param template
	 * @void
	 */
	public void setTemplate(Template template) {
		// got a template?
		if (this.template) {
			if( template ) {
				// yes, are we setting a different template?
				if (this.template != template) {
					// make sure the non-overlapping fields are unset
					this.template.fields.findAll { !(it in template.fields) }.each {
						this.setFieldValue(it.name,null)
					}
				}
			} else {
				// Unset all fields, since the template is removed
				this.template.fields.each {
					this.setFieldValue(it.name,null)
				}
			}
		}

		// set template
		this.template = template
	}
}
