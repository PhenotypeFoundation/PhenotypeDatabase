package org.dbnp.gdt

import groovy.sql.Sql

/**
 * A TemplateField is a specification for either a 'domain field' of a subclass of TemplateEntity or a
 * 'template field' for a specific Template. See the Template class for an explanation of these terms.
 * The TemplateField class contains all information which is needed to specify what kind of data can be stored
 * in this particular field, such as the TemplateFieldType, the name, the ontologies from which terms can derive
 * in case of an ONTOLOGYTERM field, the list entries in case of a STRINGLIST fields, and so on.
 * The actual values of the template fields are stored in instances of subclasses of the TemplateEntity class.
 * For example, if there exists a Study template with a 'description' TemplateField as a member of Template.fields,
 * the actual description for each Study would be stored in the inherited templateStringFields map of that Study instance.
 *
 * One TemplateField can belong to many Templates, but they have to be the same entity as the TemplateField itself.
 *
 * Revision information:
 * $Rev: 1274 $
 * $Author: robert@isdat.nl $
 * $Date: 2010-12-15 13:53:28 +0100 (Wed, 15 Dec 2010) $
 */
class TemplateField implements Serializable {
	/** The name of the TemplateField, by which it is represented to the user.   */
	String name

	/** The type of this TemplateField, such as STRING, ONTOLOGYTERM etc.  */
	TemplateFieldType type

	/** The entity for which this TemplateField is meant. Only Templates for this entity can contain this TemplateField  */
	Class entity

	/** The unit of the values of this TemplateField (optional)  */
	String unit

	/** The help string which is shown in the user interface to describe this template field (optional, TEXT)  */
	String comment

	/** The different list entries for a STRINGLIST TemplateField. This property is only used if type == TemplateFieldType.STRINGLIST  */
	List listEntries

	/** Indicates whether this field is required to be filled out or not  */
	boolean required

	/** Indicates whether this field is the preferred identifier for the resulting templated entity.
	 This is for example used when importing to match entries in the database against the ones that are being imported.  */
	boolean preferredIdentifier

	static hasMany = [
		listEntries: TemplateFieldListItem,	// to store the entries to choose from when the type is 'item from predefined list'
		ontologies: Ontology				// to store the ontologies to choose from when the type is 'ontology term'
	]

	static constraints = {
		// outcommented for now due to bug in Grails / Hibernate
		// see http://jira.codehaus.org/browse/GRAILS-6020
		// This is to verify that TemplateField names are unique within templates of each super entity
		// TODO: this probably has to change in the case of private templates of different users,
		// which can co-exist with the same name. See also Template
		// name(unique:['entity'])

		name(nullable: false, blank: false)
		type(nullable: false, blank: false)
		entity(nullable: false, blank: false)
		unit(nullable: true, blank: true)
		comment(nullable: true, blank: true)
		required(default: false)
		preferredIdentifier(default: false)
	}

	static mapping = {
		// Make sure the comments can be Strings of arbitrary length
		comment type	: 'text'
		name column		: "templatefieldname"
		type column		: "templatefieldtype"
		entity column	: "templatefieldentity"
		unit column		: "templatefieldunit"
		comment column	: "templatefieldcomment"
	}

	String toString() {
		return name
	}

	/**
	 * return an escaped name which can be used in business logic
	 * @return String
	 */
	def String escapedName() {
		return name.toLowerCase().replaceAll("([^a-z0-9])", "_")
	}

	/**
	 * overloading the findAllByEntity method to make it function as expected
	 * @param Class entity (for example: dbnp.studycapturing.Subject)
	 * @return ArrayList
	 */
	public static findAllByEntity(java.lang.Class entity) {
		def results = []
		// 'this' should not work in static context, so taking Template instead of this
		TemplateField.all.each() {
			if (entity.equals(it.entity)) {
				results[results.size()] = it
			}
		}

		return results
	}

	def addListEntries(items) {
		items.each {
			TemplateFieldListItem item = new TemplateFieldListItem(name: it)
			this.addToListEntries(item)
		}
		this
	}

    /**
     * Retrieves the templates that use this template field
     *
     * @returns a list of templates that use this template field.
     */
    def getUses() {
        def templates = Template.all;
        def elements;

        if (templates && templates.size() > 0) {
            elements = templates.findAll { template -> template.fields.contains(this) };
        } else {
            return [];
        }

        return elements;
    }

    /**
     * Retrieves all ontologies of an ontologyterm template field that have been used in an object
     *
     * @return ArrayList containing all ontologies of this template field that have been used in an object
     * 			(i.e. all ontologies from which a term has been selected in this template field).
     */
    def getUsedOntologies() {
        if (this.type != TemplateFieldType.ONTOLOGYTERM) {
            return []
        }

        def entities = getEntities()

        if (entities.size() == 0) {
            return []
        }

        return this.ontologies.findAll { ontologyEntryUsed(it, entities) }
    }

    /**
     * Retrieves all list items of an ontologyterm template field that have never been used in an object
     *
     * @return ArrayList containing all ontologies of this template field that have never been used in an object.
     * 			(i.e. all ontologies from which no term has been selected in this template field).
     */
    def getNonUsedOntologies() {
        if (this.type != TemplateFieldType.ONTOLOGYTERM) {
            return []
        }

        def entities = getEntities()


        if (entities.size() == 0) {
            return []
        }

        return this.ontologies.findAll { !ontologyEntryUsed(it, entities) }
    }

    /**
     * Retrieves all template entities for a templateField
     *
     * @return ArrayList containing all tentities of this template field
     */
    def getEntities() {
        // Find all templates that use this template field
        def templates = this.getUses();

        if (templates.size() == 0)
            return [];

        // Find all entities that use these templates
        def c = this.entity.createCriteria()
        def entities = c {
            'in'("template", templates)
        }

        if (entities.size() == 0)
            return []

        return entities
    }

    /**
     * Checks whether the item is selected in an entity where this ontology template field is used
     * @param mixed item
     * @returns boolean
     */
    def ontologyEntryUsed(item, entities) {
        //Checks is the ontology is part of this template field and a term from the given
        //ontology is selected in an entity where this template field is used. false otherwise
        //Returns false if the type of this template field is other than ONTOLOGYTERM
        def entitiesWithOntology = entities.findAll { entity ->

            //Quite inefficient to just check wether a entry is used or not.
            def value = entity.getFieldValue(this.name);

            if (value)
                return value.ontology.equals(item)
            else
                return false;
        }
        return entitiesWithOntology.size() > 0;
    }

	/**
	 * Checks whether this template field is used in a template and also filled in any instance of that template
	 *
	 * @returns true iff this template field is used in a template, the template is instantiated
	 * 				and an instance has a value for this field. false otherwise
	 */
	def isFilled() {
		// Find all templates that use this template field
		def templates = getUses();

		if (templates.size() == 0)
		return false;

		// Find all entities that use these templates
		def c = this.entity.createCriteria()
		def entities = c {
			'in'("template", templates)
		}

		def filledEntities = entities.findAll { entity -> entity.getFieldValue(this.name) }

		return filledEntities.size() > 0;
	}

	/**
	 * Checks whether this template field is used in the given template and also filled in an instance of that template
	 *
	 * @returns true iff this template field is used in the given template, the template is instantiated
	 * 				and an instance has a value for this field. false otherwise
	 */
	def isFilledInTemplate(Template t) {
		if (t == null)
		return false;

		// If the template is not used, if can never be filled
		if (!t.fields.contains(this))
		return false;

		// Find all entities that use this template
		def entities = entity.findAllByTemplate(t);
		def filledEntities = entities.findAll { entity -> entity.getFieldValue(this.name) }

		return filledEntities.size() > 0;
	}

	/**
	 * Checks whether this template field is filled in all objects using a template with this template field
	 * If the template field is never used, the method returns true. If the template field is used in a template,
	 * but no objects with this template exist, the method also returns true
	 *
	 * @returns false iff objects exist using this template field, but without a value for this field. true otherwise
	 */
	def isFilledInAllObjects() {
		// Find all templates that use this entity
		def templates = getUses();

		if (templates.size() == 0)
		return true;

		// Find all entities that use these templates
		def c = this.entity.createCriteria()
		def entities = c {
			'in'("template", templates)
		}

		if (entities.size() == 0)
		return true;

		def emptyEntities = entities.findAll { entity -> !entity.getFieldValue(this.name) }

		return (emptyEntities.size() == 0);
	}

	/**
	 * Check whether a templatefield that is used in a template may still be edited or deleted.
	 * That is possible if the templatefield is never filled and the template is only used in one template
	 *
	 * This method should only be used for templatefields used in a template that is currently shown. Otherwise
	 * the user may edit this template field, while it is also in use in another template than is currently shown.
	 * That lead to confusion.
	 *
	 * @returns true iff this template may still be edited or deleted.
	 */
	def isEditable() {
		return !isFilled() && getUses().size() == 1;
	}

	/**
	 * Checks whether this field is filled in any of the entities in the given list
	 *
	 * @param List List of TemplateEntities to search in
	 * @return boolean	True iff any of the given entities has this field as template field, and has a value for it. False otherwise
	 */
	def isFilledInList(entityList) {
		if (!entityList)
		return false;

		return true in entityList.collect { it.fieldExists(this.name) && it.getFieldValue(this.name) != null }?.flatten()
	}

	/**
	 * Check whether the contents of the other templatefield and the current templatefield are equal.
	 * For this check the comments field doesn't matter. 
	 *
	 * @return true iff this template field equals the other template field
	 * 			(the comments field may be different)
	 */
	public boolean contentEquals(Object otherObject) {
		if (!(otherObject instanceof TemplateField))
		return false

		if (otherObject == null)
		return false

		TemplateField otherField = (TemplateField) otherObject;

		if (otherField == this)
		return true

		if (otherField.entity != this.entity) {
			return false
		}
		if (otherField.name != this.name) {
			return false
		}
		if (otherField.type != this.type) {
			return false
		}
		if (otherField.unit != this.unit) {
			return false
		}
		if (otherField.required != this.required) {
			return false
		}

		if (otherField.preferredIdentifier != this.preferredIdentifier) {
			return false
		}

		// Check whether the list entries are equal (except for the order)
		def size1 = otherField.listEntries?.size() ?: 0
		def size2 = this.listEntries?.size() ?: 0
		if (size1 != size2) {
			return false
		}

		if (otherField.listEntries != null && this.listEntries != null) {
			for (def entry in this.listEntries) {
				def entryFound = false;
				for (def otherEntry in otherField.listEntries) {
					def name1 = entry != null ? entry.name : ""
					def name2 = otherEntry != null ? otherEntry.name : ""
					if (name1 == name2) {
						entryFound = true;
						break
					}
				}

				if (!entryFound) {
					return false
				}
			}
		}

		// Check whether the ontologies are equal (except for the order)
		size1 = otherField.ontologies?.size() ?: 0
		size2 = this.ontologies?.size() ?: 0
		if (size1 != size2) {
			return false
		}
		if (this.ontologies != null && otherField.ontologies != null) {
			for (def ontology in this.ontologies) {
				if (!otherField.ontologies.contains(ontology)) {
					return false
				}
			}
		}

		// If all tests pass, the objects are content-equal
		return true
	}

	/**
	 * Create a new template field based on the parsed XML object. 
	 *
	 * @see grails.converters.XML#parse(java.lang.String)
	 * @throws IllegalArgumentException
	 */
	public static parse(Object xmlObject, Class entity) {
		def t = new TemplateField();

		t.name = xmlObject?.name?.text()
		t.unit = xmlObject?.unit?.text() == "" ? null : xmlObject?.unit?.text()
		t.comment = xmlObject?.comment?.text()
		t.required = xmlObject?.required?.text() == 'true' ? true : false
		t.preferredIdentifier = xmlObject?.preferredIdentifier?.text() == 'true' ? true : false

		t.entity = entity

		t.type = TemplateFieldType.valueOf(xmlObject?.type?.text())

		// Search for ontologies
		xmlObject.ontologies?.ontology.each {
			def ncboId = it.ncboId?.text();
			t.addToOntologies(Ontology.getOrCreateOntologyByOntologyId(ncboId));
		}

		// Search for list entries
		xmlObject.listItems?.listItem.each {
			def name = ""
			if (it != null && it.name)
			name = it.name.text()

			t.addToListEntries(new TemplateFieldListItem(name: name));
		}
		return t;
	}


}