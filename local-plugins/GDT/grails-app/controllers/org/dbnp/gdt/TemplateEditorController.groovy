/**
 * TemplateEditorController Controler
 *
 * Webflow driven template editor
 *
 * @author Jeroen Wesbeek
 * @since 20100415
 * @package studycapturing
 *
 * Revision information:
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
 */
package org.dbnp.gdt

import grails.converters.*

class TemplateEditorController {
	def entityName
	def entity
	def gdtService
    def mailService
    def templateService
    def templateFieldService
    def authenticationService

	/**
	 * Fires after every action and determines the layout of the page
	 */
	def afterInterceptor = { model, modelAndView ->
		if (params['standalone'] && params['standalone'] == 'true') {
			model.layout = 'main';
			model.extraparams = ['standalone': 'true'];
		} else {
			model.layout = 'dialog';
			model.extraparams = [];
		}
	}

	/**
	 * Show the template editor page for a particular entity
	 * @param targetEntity The full class name of the target entity
	 */
	private void showEntity(String targetEntity) {
		// redirect to template editor page of the specified entity
		params.entity = gdtService.decodeEntity(targetEntity)
		redirect(action: "index", params: params)
	}

	/**
	 * index closure
	 */
	def index = {
		// Check whether a right entity is given
		if (!checkEntity()) {
			return
		}

		// fetch all templates for this entity
		def templates = Template.findAllByEntity(entity)

		// Generate a human readable entity name
		def parts = entityName.tokenize('.');
		def humanReadableEntity = parts[parts.size() - 1];

		return [
			entity: entity,
			templates: templates,
			encryptedEntity: params.entity,
			humanReadableEntity: humanReadableEntity,
			ontologies: params.ontologies,
            templateadmin: authenticationService.getLoggedInUser().hasTemplateAdminRights()
		];
	}

	/**
	 * compare two or more templates
	 */
	def compare = {
		// Check whether a right entity is given
		if (!checkEntity()) {
			return
		}

		// fetch all templates for this entity
		def templates = Template.findAllByEntity(entity)

		// Find all available fields
		def allFields = TemplateField.findAllByEntity(entity).sort { a, b -> a.name <=> b.name }

		// Generate a human readable entity name
		def parts = entityName.tokenize('.');
		def humanReadableEntity = parts[parts.size() - 1];

		return [
			entity: entity,
			templates: templates,
			allFields: allFields,
			encryptedEntity: params.entity,
			humanReadableEntity: humanReadableEntity,
			ontologies: params.ontologies,
			templateEntities: gdtService.getTemplateEntities()
		];
	}

	/**
	 * Shows the editing of a template
	 */
	def template = {
		// Check whether a right entity is given
		if (!checkEntity()) {
			return
		}

		// Check whether a template is selected. If not, redirect the user to the index
		def selectedTemplate = params.template;
		def template = null;
		def domainFields = null;

		if (selectedTemplate) {
			template = Template.get(selectedTemplate);
			domainFields = template.entity.newInstance().giveDomainFields();
		} else {
			redirect(action: "index", params: [entity: params.entity])
			return;
		}

		// fetch all templates for this entity
		def templates = Template.findAllByEntity(entity)

		// Generate a human readable entity name
		def parts = entityName.tokenize('.');
		def humanReadableEntity = parts[parts.size() - 1];

		// Find all available fields
		def allFields = TemplateField.findAllByEntity(entity).sort { a, b -> a.name <=> b.name }

		return [
			entity: entity,
			templates: templates,
			encryptedEntity: params.entity,
			fieldTypes: TemplateFieldType.list(),
			ontologies: Ontology.list(),
			humanReadableEntity: humanReadableEntity,
            apikey: Ontology.getBioOntologyApiKey(),

			template: template,
			allFields: allFields,
			domainFields: domainFields,
            templateadmin: authenticationService.getLoggedInUser().hasTemplateAdminRights()
		];

	}

	/**
	 * Shows an error page
	 *
	 * TODO: improve the error page
	 */
	def error = {
		// set content type
		response.setContentType("text/html; charset=UTF-8")

		render('view': 'error');
	}

	/**
	 * Creates a new template using a AJAX call
	 *
	 * @return JSON object with two entries:
	 * 						id: [id of this object]
	 * 						html: HTML to replace the contents of the LI-item that was updated.
	 * 					On error the method gives a HTTP response status 500 and the error
	 */
	def createTemplate = {
		// set content type
		response.setContentType("text/plain; charset=UTF-8")

		// Decode the entity
		if (!checkEntity()) {
			response.status = 500;
			render "Incorrect entity given";
			return;
		}

		// set entity
		params.entity = entity;

		// Create the template fields and add it to the template
		def template = new Template(params);
		if (template.validate() && template.save(flush: true)) {
			def html = g.render(plugin: 'gdt', template: 'elements/liTemplate', model: [template: template, templateadmin: authenticationService.getLoggedInUser().hasTemplateAdminRights()]);
			def output = [id: template.id, html: html];
			response.setContentType("application/json; charset=UTF-8")
			render output as JSON;
		} else {
			response.status = 500;
			render 'Template could not be created because errors occurred.';
			return
		}
	}

	/**
	 * Clones a template using a AJAX call
	 *
	 * @return JSON object with two entries:
	 * 						id: [id of this object]
	 * 						html: HTML of the contents of the LI-item that will be added.
	 * 					On error the method gives a HTTP response status 500 and the error
	 */
	def cloneTemplate = {
		// set content type
		response.setContentType("text/plain; charset=UTF-8")

		// Search for the template field
		def template = Template.get(params.id);
		if (!template) {
			response.status = 404;
			render 'Template not found';
			return;
		}

		// Create the template fields and add it to the template
		def newTemplate
		if (session.getProperty('loggedInUser') && session.getProperty('loggedInUser').getProperty('id') && session.getProperty('loggedInUser').getProperty('id') instanceof Long) {
			newTemplate = new Template(template, session.getProperty('loggedInUser').getProperty('id'))
		} else {
			newTemplate = new Template(template)
		}

		if (newTemplate.validate() && newTemplate.save(flush: true)) {
			def html = g.render(plugin: 'gdt', template: 'elements/liTemplate', model: [template: newTemplate, templateadmin: authenticationService.getLoggedInUser().hasTemplateAdminRights() ]);
			def output = [id: newTemplate.id, html: html];
			response.setContentType("application/json; charset=UTF-8")
			render output as JSON;
		} else {
			response.status = 500;
			render 'Template could not be cloned because errors occurred.';
			return
		}
	}

    /**
   	 * Sends templaterequest to (template)admins via mailService
   	 */
   	def sendRequest = {

   		// set content type
   		response.setContentType("application/json; charset=UTF-8")
        def body = g.render(plugin: 'gdt', template: 'requestEmail', model: [user: authenticationService.getLoggedInUser(), requestcat: params.requestcat, requestnm: params.requestnm, rname: params.rname, rtype: params.rtype, specification: params.specification]);

        mailService.sendMail {
            to      authenticationService.getTemplateAdminEmails()
            subject "New template request"
            html    body.toString()
        }

        def output = [];
        response.setContentType("application/json; charset=UTF-8")
        render output as JSON;
	}

	/**
	 * Updates a selected template using a AJAX call
	 *
	 * @param id ID of the template to update
	 * @return JSON object with two entries:
	 * 					id: [id of this object]
	 * 					html: HTML to replace the contents of the LI-item that was updated.
	 * 				On error the method gives a HTTP response status 500 and the error
	 */
	def updateTemplate = {
		// set content type
		response.setContentType("text/plain; charset=UTF-8")

		// Search for the template field
		def template = Template.get(params.id);
		if (!template) {
			response.status = 404;
			render 'Template not found';
			return;
		}

		// Update the field if it is not updated in between
		if (params.version) {
			def version = params.version.toLong()
			if (template.version > version) {
				response.status = 500;
				render 'Template was updated while you were working on it. Please reload and try again.';
				return
			}
		}

		template.properties = params
		if (!template.hasErrors() && template.save(flush: true)) {
			def html = g.render(plugin: 'gdt', template: 'elements/liTemplate', model: [template: template, templateadmin: authenticationService.getLoggedInUser().hasTemplateAdminRights()]);
			def output = [id: template.id, html: html];
			response.setContentType("application/json; charset=UTF-8")
			render output as JSON;
		} else {
			response.status = 500;
			render 'Template was not updated because errors occurred.';
			return
		}
	}

	/**
	 * Deletes a template using a AJAX call
	 *
	 * @param template ID of the template to move
	 * @return JSON object with one entry:
	 * 							id: [id of this object]
	 * 						On error the method gives a HTTP response status 500 and the error
	 */
	def deleteTemplate = {
		// set content type
		response.setContentType("text/plain; charset=UTF-8")

		// Search for the template field
		def template = Template.get(params.template);
		if (!template) {
			response.status = 404;
			render 'Template not found';
			return;
		}

		// Delete the template field
		try {
			template.delete(flush: true)

			def output = [id: template.id];
			response.setContentType("application/json; charset=UTF-8")
			render output as JSON;
		}
		catch (org.springframework.dao.DataIntegrityViolationException e) {
			response.status = 500;
			render 'Template could not be deleted: ' + e.getMessage();
		}
	}

	/**
	 * Creates a new template field using a AJAX call
	 *
	 * @param template ID of the template to add a field to
	 * @return JSON object with two entries:
	 * 						id: [id of this object]
	 * 						html: HTML to replace the contents of the LI-item that was updated.
	 * 					On error the method gives a HTTP response status 500 and the error
	 */
	def createField = {
		// set content type
		response.setContentType("text/plain; charset=UTF-8")

		// Search for the template
		def template = Template.get(params.template);

		if (!template) {
			response.status = 404;
			render 'Template not found';
			return;
		}

		// Decode the entity, in order to set a good property
		if (!checkEntity()) {
			response.status = 500;
			render "Incorrect entity given";
			return;
		}

		params.entity = entity;

		// See whether this field already exists. It is checked by name, type and unit and entity
		// The search is done using search by example (see http://grails.org/DomainClass+Dynamic+Methods, method find)
		def uniqueParams = [name: params.name, type: params.type, unit: params.unit, entity: params.entity];
		if (TemplateField.find(new org.dbnp.gdt.TemplateField(uniqueParams))) {
			response.status = 500;
			render "A field with this name, type and unit already exists.";
			return;
		}

		// See whether this exists as domain field. If it does, raise an error
        // template.entity.domainFields would work as well,
        // but that would be relying on a convention in the implementation of the domain classes overriding TemplateEntity
		def domainFields = template.entity.newInstance().giveDomainFields()
		if (domainFields.find { it.name.toLowerCase() == params.name.toLowerCase() }) {
			response.status = 500;
			render "All templates for entity " + template.entity + " contain a domain field with name " + params.name + ". You can not create a field with this name.";;
			return;
		}

        // If this field isnot a ontologyterm, we should remove the ontologies
        if (params.type.toString() != 'ONTOLOGYTERM') {
            params.remove('ontologies');
        }

        // Create TemplateField
        def templateField = new org.dbnp.gdt.TemplateField()

		// If this field is type stringlist, we have to prepare the parameters
		if (params.type.toString() == 'STRINGLIST' || params.type.toString() == 'EXTENDABLESTRINGLIST' ) {
			def listEntries = [];
			params.listEntries.eachLine {
				// We don't search for a listitem that might already exist,
				// because if we use that list item, it will be removed from the
				// other string list.
				def name = it.trim();

                if (!listEntries.contains(name)) {
				    def listitem = new org.dbnp.gdt.TemplateFieldListItem(name: name)
				    templateField.addToListEntries(listitem)
                    listEntries.add(name)
                }
			}
		}
	    params.remove('listEntries');
		//Set all properties except listEntries (which is added by addToListEntries)
		templateField.properties = params;
		if (templateField.save(flush: true)) {

			def html = g.render(plugin: 'gdt', template: 'elements/available', model: [templateField: templateField, ontologies: Ontology.list(), fieldTypes: TemplateFieldType.list(), templateadmin: authenticationService.getLoggedInUser().hasTemplateAdminRights() ||  authenticationService.getLoggedInUser().hasAdminRights()]);
			def output = [id: templateField.id, html: html];
			response.setContentType("application/json; charset=UTF-8")
			render output as JSON;

			//render '';
		} else {
			response.status = 500;
			render 'TemplateField could not be created because errors occurred.';
			return
		}
	}

	/**
	 * Updates a selected template field using a AJAX call
	 *
	 * @param id ID of the field to update
	 * @return JSON object with two entries:
	 * 					id: [id of this object]
	 * 					html: HTML to replace the contents of the LI-item that was updated.
	 * 				On error the method gives a HTTP response status 500 and the error
	 */
	def updateField = {
		// set content type
		response.setContentType("text/plain; charset=UTF-8")

		// Search for the template field
		def templateField = TemplateField.findById(params.id);
		if (!templateField) {
			response.status = 404;
			render 'TemplateField not found'
			return;
		}

		// Update the field if it is not updated in between
		if (params.version) {
			def version = params.version.toLong()
			if (templateField.version > version) {
				response.status = 500
				render 'TemplateField was updated while you were working on it. Please reload and try again.'
				return
			}
		}

		// If this field is type stringlist or ontology, we have to prepare the parameters
		//
		// For stringlist and ontologyterm fields, the list items can be changed, even when the field is in use
		// In that case, only never-used items can be removed or changed and items can be added. If that is the case
		// params.is_disabled is true and we should combine listEntries and extraListEntries with the items already in use.
		if (params.type.toString() == 'STRINGLIST' || params.type.toString() == 'EXTENDABLESTRINGLIST' || ( (templateField.type == TemplateFieldType.STRINGLIST || templateField.type == TemplateFieldType.EXTENDABLESTRINGLIST ) && params.is_disabled)) {

            def entryNames = templateFieldService.getUsedListEntries( templateField )

            def deletedEntries = templateField.listEntries.name

            params.listEntries.eachLine {
                def entryName = it.trim()
                if (!entryNames.contains(entryName) && !templateField.listEntries.name.contains(entryName)) {
                    def listitem = new org.dbnp.gdt.TemplateFieldListItem(name: entryName)
                    templateField.addToListEntries(listitem)
                }
                else {
                    deletedEntries.remove(entryName)
                }
            }

            deletedEntries.each() { deletedEntryName ->
                //Only not used ListItems can be removed
                if (!entryNames.contains(deletedEntryName)) {
                    def removeListItem = TemplateFieldListItem.findByParentAndName(templateField, deletedEntryName)
                    templateField.removeFromListEntries(removeListItem)
                    removeListItem.delete()
                }
            }
		}
        params.remove('listEntries')

		// If this field is a ontologyterm, we add ontology objects
		// For stringlist and ontologyterm fields, the list items can be changed, even when the field is in use
		// In that case, only never-used items can be removed or changed and items can be added. If that is the case
		// params.is_disabled is true and we should combine ontologies with the ontologies already in use.
		if ((params.type.toString() == 'ONTOLOGYTERM' || (templateField.type == TemplateFieldType.ONTOLOGYTERM && params.is_disabled)) && params.ontologies) {
			def usedOntologies = [];

			if (params.is_disabled) {
				usedOntologies = templateField.getUsedOntologies();
			}

			if (params.ontologies) {
				def ontologies = params.ontologies;

                if(ontologies instanceof String) {
                    params.ontologies = usedOntologies + Ontology.get( Integer.parseInt(ontologies) );
                } else {
                    params.ontologies = usedOntologies + Ontology.getAll(ontologies.collect { Integer.parseInt(it) });
                }
			}
		} else {
			params.remove('ontologies');
		}

		// A field that is already used in one or more templates, but is not filled everywhere,
		// can not be set to required
		if (params.required) {
			if (!templateField.isFilledInAllObjects()) {
				response.status = 500;
				render "A field can only be marked as required if all objects using this field have a value for the field."
				return
			}
		}

		// Set all parameters except listEntries (which is added by addToListEntries)
		templateField.properties = params

		// validate the templateField
		templateField.validate();
		if (!templateField.hasErrors() && templateField.save(flush: true)) {
			// Remove all orphaned list items, because grails doesn't handle it for us
			TemplateFieldListItem.findAllByParent(templateField).each {
				if (!templateField.getListEntries().contains(it)) {
					templateField.removeFromListEntries(it);
					it.delete();
				}
			}

			// Select the template to use for the HTML output
			def renderTemplate = 'elements/available';
			if (params.renderTemplate == 'selected') {
				renderTemplate = 'elements/selected';
			}

			// Selected fields should have a template given
			def template = null;
			if (params.templateId)
			template = Template.findById(params.templateId);

			def html = g.render(plugin: 'gdt', template: renderTemplate, model: [template: template, templateField: templateField, ontologies: Ontology.list(), fieldTypes: TemplateFieldType.list(), templateadmin: authenticationService.getLoggedInUser().hasTemplateAdminRights()]);
			def output = [id: templateField.id, html: html];
			response.setContentType("application/json; charset=UTF-8")
			render output as JSON;
		} else {
			response.status = 500;
			render 'TemplateField was not updated because errors occurred. Please contact the system administrator';
			return
		}
	}

	/**
	 * Deletes a template field using a AJAX call
	 *
	 * @param templateField ID of the templatefield to move
	 * @return JSON object with one entry:
	 * 							id: [id of this object]
	 * 						On error the method gives a HTTP response status 500 and the error
	 */
	def deleteField = {
		// set content type
		response.setContentType("text/plain; charset=UTF-8")

		// Search for the template field
		def templateField = TemplateField.get(params.templateField);
		if (!templateField) {
			response.status = 404;
			render 'TemplateField not found';
			return;
		}

		// Delete the template field
		try {
			templateField.delete(flush: true)

			def output = [id: templateField.id];
			response.setContentType("application/json; charset=UTF-8")
			response.setContentType("application/json; charset=UTF-8")
			render output as JSON;
		}
		catch (org.springframework.dao.DataIntegrityViolationException e) {
			response.status = 500;
			render 'TemplateField could not be deleted: ' + e.getMessage();
		}
	}

	/**
	 * Adds a new template field to a template using a AJAX call
	 *
	 * @param template ID of the template to add a field to
	 * @return JSON object with two entries:
	 * 						id: [id of this object]
	 * 						html: HTML to replace the contents of the LI-item that was updated.
	 * 					On error the method gives a HTTP response status 404 or 500 and the error
	 */
	def addField = {
		// Search for the template
		def template = Template.get(params.template);

		// set content type
		response.setContentType("text/plain; charset=UTF-8")

		if (!template) {
			response.status = 404;
			render 'Template not found';
			return;
		}

		// Search for the template field
		def templateField = TemplateField.get(params.templateField);
		if (!templateField) {
			response.status = 404;
			render 'TemplateField does not exist';
			return;
		}

		// The template field should exist within the template
		if (template.fields.contains(templateField)) {
			response.status = 500;
			render 'TemplateField is already found within template';
			return;
		}

		// If the template is in use, only non-required fields can be added
		if (templateService.inUse(template) && templateField.required) {
			response.status = 500;
			render 'Only non-required fields can be added to templates that are in use.'
			return;
		}

		// All field names within a template should be unique
		if (template.fields.find { it.name.toLowerCase() == templateField.name.toLowerCase() }) {
			response.status = 500;
			render 'This template already contains a field with name ' + templateField.name + '. Field names should be unique within a template.'
			return;
		}

		if (!params.position || Integer.parseInt(params.position) == -1) {
			template.fields.add(templateField)
		} else {
			template.fields.add(Integer.parseInt(params.position), templateField)
		}

		if (!template.validate()) {
			response.status = 500;
			template.errors.each { render it}
		}
		template.save(flush: true);

		def html = g.render(plugin: 'gdt', template: 'elements/selected', model: [templateField: templateField, template: template, ontologies: Ontology.list(), fieldTypes: TemplateFieldType.list(), templateadmin:  authenticationService.getLoggedInUser().hasTemplateAdminRights()]);
		def output = [id: templateField.id, html: html];
		response.setContentType("application/json; charset=UTF-8")
		render output as JSON;
	}

	/**
	 * Removes a selected template field from the template using a AJAX call
	 *
	 * @param templateField ID of the field to update
	 * @param template ID of the template for which the field should be removed
	 * @return JSON object with two entries:
	 * 							id: [id of this object]
	 * 							html: HTML to replace the contents of the LI-item that was updated.
	 * 						On error the method gives a HTTP response status 404 or 500 and the error
	 */
	def removeField = {
		// Search for the template
		def template = Template.get(params.template);

		// set content type
		response.setContentType("text/plain; charset=UTF-8")

		if (!template) {
			response.status = 404;
			render 'Template not found';
			return;
		}

		// Search for the template field
		def templateField = TemplateField.get(params.templateField);
		if (!templateField) {
			response.status = 404;
			render 'TemplateField not found';
			return;
		}

		// The template field should exist within the template
		if (!template.fields.contains(templateField)) {
			response.status = 404;
			render 'TemplateField not found within template';
			return;
		}

		// If the template is in use, field can not be removed
		if (templateField.isFilledInTemplate(template)) {
			response.status = 500;
			render 'Fields can not be removed from a template if it has been filled somewhere.'
			return;
		}

		// Delete the field from this template
		def currentIndex = template.fields.indexOf(templateField);
		template.fields.remove(currentIndex);
		template.save(flush: true);


		def html = g.render(plugin: 'gdt', template: 'elements/available', model: [templateField: templateField, ontologies: Ontology.list(), fieldTypes: TemplateFieldType.list(), templateadmin: authenticationService.getLoggedInUser().hasTemplateAdminRights()]);
		def output = [id: templateField.id, html: html];
		response.setContentType("application/json; charset=UTF-8")
		render output as JSON;
	}

	/**
	 * Moves a template field using a AJAX call
	 *
	 * @param template ID of the template that contains this field
	 * @param templateField ID of the templatefield to move
	 * @param position New index of the templatefield in the array. The index is 0-based.
	 * @return JSON object with two entries:
	 * 							id: [id of this object]
	 * 							html: HTML to replace the contents of the LI-item that was updated.
	 * 						On error the method gives a HTTP response status 500 and the error
	 */
	def moveField = {
		// Search for the template
		def template = Template.get(params.template);

		// set content type
		response.setContentType("text/plain; charset=UTF-8")

		if (!template) {
			response.status = 404;
			render 'Template not found';
			return;
		}

		// Search for the template field
		def templateField = TemplateField.get(params.templateField);
		if (!templateField) {
			response.status = 404;
			render 'TemplateField not found';
			return;
		}

		// The template field should exist within the template
		if (!template.fields.contains(templateField)) {
			response.status = 404;
			render 'TemplateField not found within template';
			return;
		}

		// Move the item
		def currentIndex = template.fields.indexOf(templateField);
		def moveField = template.fields.remove(currentIndex);

		template.fields.add(Integer.parseInt(params.position), moveField);
		template.save(flush: true);

		def html = g.render(plugin: 'gdt', template: 'elements/selected', model: [templateField: templateField, template: template, fieldTypes: TemplateFieldType.list(), templateadmin: authenticationService.getLoggedInUser().hasTemplateAdminRights()]);
		def output = [id: templateField.id, html: html];
		response.setContentType("application/json; charset=UTF-8")
		render output as JSON;
	}

	/**
	 * Checks how many template use a specific template field
	 *
	 * @param id ID of the template field
	 * @return int	Number of uses
	 */
	def numFieldUses = {
		// Search for the template field
		def templateField = TemplateField.get(params.id);
		if (!templateField) {
			response.status = 404;
			render 'TemplateField not found';
			return;
		}

		render templateFieldService.numUses(templateField);
	}

	/**
	 * Adds a ontolgy based on the ID given
	 * ID is actually ontology URL
	 * @param unique ontologyURL/ID
	 * @return JSON	Ontology object
	 */
	def addOntologyById = {
        //ID is actually ontology URL
        def id = (params.containsKey('ontology_id')) ? (params.ontology_id as String) : ""

		// set content type
		response.setContentType("text/plain; charset=UTF-8")

		if (!id) {
			response.status = 500;
			render 'No ID given'
			return;
		}

		def ontology = null;

		try {
            ontology = Ontology.getOrCreateOntology(id)
		} catch (Exception e) {
			response.status = 500;
			render 'Ontology with ID ' + id + ' not found';
			return;
		}

		response.setContentType("application/json; charset=UTF-8")
 		render ontology as JSON
	}

	/**
	 * Checks whether a correct entity is given
	 *
	 * @return boolean	True if a correct entity is given. Returns false and raises an error otherwise
	 */
	def checkEntity = {
		// got a entity get parameter?
        try {
            entityName = gdtService.decodeEntity(params.get('entity'))
        }
        catch(Exception e) {
            error()
            return false
        }
		if (!entityName) {
			error();
			return false;
		}

		// Create an object of this type
		try {
            entity = gdtService.getInstanceByEntityName(entityName)
        }
        catch(Exception e) {
            error()
            return false
        }
		if (!entity) {
			error();
			return false
		}

		return true;
	}
}