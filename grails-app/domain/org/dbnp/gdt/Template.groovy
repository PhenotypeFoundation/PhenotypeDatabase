package org.dbnp.gdt

import groovy.sql.Sql

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
 * $Rev: 1349 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-07 16:08:48 +0100 (Fri, 07 Jan 2011) $
 */
class Template extends Identity {
	def gdtService

	/** The name of the template */
	String name

	/** A string describing the template to other users */
	String description

	/** The target TemplateEntity for this template */
	Class entity

	/** The owner of the template. If the owner is not defined, it is a shared/public template */
	//SecUser owner
	Object owner
	Long owner_id

	Date dateCreated
	Date lastUpdated

	static transients = [ "identifier", "iterator", "maximumIdentity", "owner", "gdtService" ]

	/** The template fields which are the members of this template. This is a List to preserve the field order */
	List fields

	static hasMany = [fields: TemplateField]
    static mapping = {}

	def setOwner = { owner ->
		owner_id = owner.id
	}

	// constraints
	static constraints = {
		owner_id(nullable: true, blank: true)
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
		// name(unique:['entity'])

	}

	/**
	 * class constructor
	 * @return
	 */
	public Template() {
		super()
	}

	/**
	 * Creates a clone of the given other template and also copies its owner
	 * @param	otherTemplate
	 */
	public Template( Template otherTemplate ) {
		// create a new template
		this()

		// copy base values
		this.name			= otherTemplate.name + " (Copy)"
		this.description	= otherTemplate.description
		this.entity			= otherTemplate.entity

		// copy template fields
		this.fields			= []
		otherTemplate.fields.each {
			this.fields.add( it )
		}
	}

	/**
	 * clone template
	 * @param otherTemplate
	 * @param owner_id
	 * @return
	 */
	public Template( Template otherTemplate, Long owner_id ) {
		// clone template
		this( otherTemplate )

		// set owner
		this.owner_id = owner_id
	}

	/**
	 * overloaded toString method
	 * @return String
	 */
	def String toString() {
		return this.name;
	}

	/**
	 * Check whether the contents of the other template and the current template are equal.
	 * For this check the name, description and owner don't matter. Also, the order of
	 * template fields doesn't matter
	 *
	 * @return	true iff this template and the other template are used for the same entity and
	 *			the template contain the same template fields
	 */
	public boolean contentEquals( Template otherTemplate ) {
		if( otherTemplate == this )
			return true

		if( otherTemplate == null )
			return false

		if( otherTemplate.entity != this.entity )
			return false

		// Check all template fields
		def size1 = this.fields?.size() ?: 0
		def size2 = otherTemplate.fields?.size() ?: 0
		if( size1 != size2 ) {
			return false
		}

		if( otherTemplate.fields != null && this.fields != null ) {
			for( def field in this.fields ) {
				def fieldFound = false;
				for( def otherField in otherTemplate.fields ) {
					if( otherField.contentEquals( field ) ) {
						fieldFound = true;
						break
					}
				}

				if( !fieldFound ) {
					return false
				}
			}
		}

		// If all tests pass, the objects are content-equal
		return true
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
	 * get all required fields
	 * @param Class fieldType
	 * @return Set < TemplateField >
	 */
	def getRequiredFieldsByType(TemplateFieldType fieldType) {
		def result = fields.findAll {
			(it.required == true && it.type == fieldType)
		}
		return result;
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

	/**
	 * Create a new template based on the parsed XML object.
	 *
	 * @see grails.converters.XML#parse(java.lang.String)
	 * @throws IllegalArgumentException
	 * @throws ClassNotFoundException
	 *
	 */
	public static parse(Object xmlObject, loggedInUser) {
		def t = new Template();
		t.name = xmlObject?.name?.text()
		t.description = xmlObject?.description?.text()

		// Check whether a correct entity is given. The parseEntity method might
		// throw a ClassNotFoundException, but that is OK, it should be thrown if the class
		// doesn't exist.
		def entity = TemplateEntity.parseEntity(xmlObject.entity?.text());
		if (!entity) {
			throw new Exception("Incorrect entity given");
		}

		t.entity = entity

		// Set the owner to the currently logged in user
		t.owner = loggedInUser

		// Set all template fields
		xmlObject.templateFields.templateField.each {
			def field = TemplateField.parse(it, entity);

			// Check whether a similar field already exists. For that, we search in all
			// template fields with the same name, in order to have as little comparisons
			// as possible
			for (def otherField in TemplateField.findAllByName(field?.name)) {
				if (field.contentEquals(otherField)) {
					field = otherField;
					break;
				}
			}

			t.addToFields(field);
		}

		return t
	}
}
