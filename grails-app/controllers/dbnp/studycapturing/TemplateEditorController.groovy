/**
 * TemplateEditorController Controler
 *
 * Webflow driven template editor
 *
 * @author  Jeroen Wesbeek
 * @since	20100415
 * @package	studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.studycapturing
import dbnp.data.*
import dbnp.studycapturing.*

import dbnp.authentication.AuthenticationService
import grails.plugins.springsecurity.Secured

import cr.co.arquetipos.crypto.Blowfish
import grails.converters.*
import java.lang.reflect.*;

@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class TemplateEditorController {
    def entityName;
    def entity;
	def authenticationService

	/**
     * Fires after every action and determines the layout of the page
     */
    def afterInterceptor = { model, modelAndView ->
      if ( params['standalone'] && params['standalone'] == 'true') {
        model.layout = 'main';
        model.extraparams = [ 'standalone': 'true' ] ;
      } else {
        model.layout = 'dialog';
        model.extraparams = [] ;
      }
    }
	
	/**
    * Study template editor page
	*/
	def study = {
	    showEntity('dbnp.studycapturing.Study')
    }

	/**
    * Subject template editor page
	*/
	def subject = {
	    showEntity('dbnp.studycapturing.Subject')
    }

	/**
    * Event template editor page
	*/
	def event = {
	    showEntity('dbnp.studycapturing.Event')
    }

	/**
    * Sampling Event template editor page
	*/
	def samplingEvent = {
	    showEntity('dbnp.studycapturing.SamplingEvent')
    }

	/**
    * Event template editor page
	*/
	def sample = {
	    showEntity('dbnp.studycapturing.Sample')
    }

	/**
    * Assay template editor page
	*/
	def assay = {
	    showEntity('dbnp.studycapturing.Assay')
    }

	/**
	 * Show the template editor page for a particular entity
	 * @param targetEntity The full class name of the target entity
	 */
	private void showEntity(String targetEntity) {
		String resultEntity
		if (grailsApplication.config.crypto) {
			// if a shared secret is defined, encrypt using that
			resultEntity = Blowfish.encryptBase64(
					targetEntity.toString(),
					grailsApplication.config.crypto.shared.secret
				)
		}
		else {
			// otherwise use standard encoding
			resultEntity = targetEntity.toString().bytes.encodeBase64()
		}

		// redirect to template editor page of the specified entity
		params.entity = resultEntity
		redirect(action: "index", params:params)
	}

    /**
     * index closure
     */
    def index = {
        // Check whether a right entity is given
        if( !_checkEntity() ) {
			return
		}

        // fetch all templates for this entity
        def templates = Template.findAllByEntity(entity)

		// Generate a human readable entity name
		def parts = entityName.tokenize( '.' );
		def humanReadableEntity = parts[ parts.size() - 1 ];

        return [
            entity: entity,
            templates: templates,
            encryptedEntity: params.entity,
            humanReadableEntity: humanReadableEntity,
			ontologies: params.ontologies
        ];
    }

    /**
     * compare two or more templates
     */
    def compare = {
        // Check whether a right entity is given
        if( !_checkEntity() ) {
			return
		}

        // fetch all templates for this entity
        def templates = Template.findAllByEntity(entity)

		// Find all available fields
		def allFields = TemplateField.findAllByEntity( entity ).sort { a, b -> a.name <=> b.name }

		// Generate a human readable entity name
		def parts = entityName.tokenize( '.' );
		def humanReadableEntity = parts[ parts.size() - 1 ];

        return [
            entity: entity,
            templates: templates,
			allFields: allFields,
            encryptedEntity: params.entity,
            humanReadableEntity: humanReadableEntity,
			ontologies: params.ontologies,
			templateEntities: this.templateEntityList()
        ];
    }

	/**
	 * Shows the editing of a template
	 */
	def template = {
        // Check whether a right entity is given
        if( !_checkEntity() ) {
			return
		}

        // Check whether a template is selected. If not, redirect the user to the index
        def selectedTemplate = params.template;
        def template = null;
		def domainFields = null;

        if( selectedTemplate ) {
            template = Template.get( selectedTemplate );
			domainFields = template.entity.giveDomainFields();
        } else {
			redirect(action:"index",params:[entity:params.entity])
			return;
		}

        // fetch all templates for this entity
        def templates = Template.findAllByEntity(entity)

		// Generate a human readable entity name
		def parts = entityName.tokenize( '.' );
		def humanReadableEntity = parts[ parts.size() - 1 ];

		// Find all available fields
		def allFields = TemplateField.findAllByEntity( entity ).sort { a, b -> a.name <=> b.name }

        return [
            entity: entity,
            templates: templates,
            encryptedEntity: params.entity,
            fieldTypes: TemplateFieldType.list(),
			ontologies: Ontology.list(),
            humanReadableEntity: humanReadableEntity,

            template: template,
			allFields: allFields,
			domainFields: domainFields
        ];

	}


    /**
     * Shows an error page
     *
     * TODO: improve the error page
     */
    def error = {
        render( 'view': 'error' );
    }

    /**
     * Creates a new template using a AJAX call
	 *
	 * @return			JSON object with two entries:
	 *						id: [id of this object]
	 *						html: HTML to replace the contents of the LI-item that was updated.
	 *					On error the method gives a HTTP response status 500 and the error
     */
   def createTemplate = {
		// Decode the entity
        if( !_checkEntity() ) {
			response.status = 500;
			render "Incorrect entity given";
			return;
		}

		// set entity
		params.entity = entity;

		// Create the template fields and add it to the template
		def template = new Template( params );
        if (template.validate() && template.save(flush: true)) {
			def html = g.render( template: 'elements/liTemplate', model: [template: template] );
			def output = [ id: template.id, html: html ];
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
	 * @return			JSON object with two entries:
	 *						id: [id of this object]
	 *						html: HTML of the contents of the LI-item that will be added.
	 *					On error the method gives a HTTP response status 500 and the error
     */
     def cloneTemplate = {
        // Search for the template field
        def template = Template.get( params.id );
        if( !template ) {
            response.status = 404;
            render 'Template not found';
            return;
        }

		// Create the template fields and add it to the template
		def newTemplate = new Template( template, authenticationService.getLoggedInUser() );
        if (newTemplate.validate() && newTemplate.save(flush: true)) {
			def html = g.render( template: 'elements/liTemplate', model: [template: newTemplate] );
			def output = [ id: newTemplate.id, html: html ];
			render output as JSON;
        } else {
            response.status = 500;
            render 'Template could not be cloned because errors occurred.';
            return
        }
    }

    /**
     * Updates a selected template using a AJAX call
	 *
	 * @param id	ID of the template to update
	 * @return		JSON object with two entries:
	 *					id: [id of this object]
	 *					html: HTML to replace the contents of the LI-item that was updated.
	 *				On error the method gives a HTTP response status 500 and the error
     */
    def updateTemplate = {
        // Search for the template field
        def template = Template.get( params.id );
        if( !template ) {
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
			def html = g.render( template: 'elements/liTemplate', model: [template: template] );
			def output = [ id: template.id, html: html ];
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
	 * @param template		ID of the template to move
	 * @return				JSON object with one entry:
	 *							id: [id of this object]
	 *						On error the method gives a HTTP response status 500 and the error
     */
    def deleteTemplate = {
        // Search for the template field
        def  template = Template.get( params.template );
        if( !template ) {
            response.status = 404;
            render 'Template not found';
            return;
        }

        // Delete the template field
		try {
			template.delete(flush: true)

			def output = [ id: template.id ];
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
	 * @param template	ID of the template to add a field to
	 * @return			JSON object with two entries:
	 *						id: [id of this object]
	 *						html: HTML to replace the contents of the LI-item that was updated.
	 *					On error the method gives a HTTP response status 500 and the error
     */
    def createField = {
        // Search for the template
        def template = Template.get( params.template );

        if( !template ) {
            response.status = 404;
            render 'Template not found';
            return;
        }

		// Decode the entity, in order to set a good property
        if( !_checkEntity() ) {
			response.status = 500;
			render "Incorrect entity given";
			return;
		}

		params.entity = entity;

		// See whether this field already exists. It is checked by name, type and unit and entity
		// The search is done using search by example (see http://grails.org/DomainClass+Dynamic+Methods, method find)
		def uniqueParams = [ name: params.name, type: params.type, unit: params.unit, entity: params.entity ];
		if( TemplateField.find( new TemplateField( uniqueParams ) ) ) {
			response.status = 500;
			render "A field with this name, type and unit already exists.";
			return;
		}

		// See whether this exists as domain field. If it does, raise an error
		def domainFields = template.entity.giveDomainFields()
		if( domainFields.find { it.name.toLowerCase() == params.name.toLowerCase() } ) {
			response.status = 500;
			render "All templates for entity " + template.entity + " contain a domain field with name " + params.name + ". You can not create a field with this name.";;
			return;
		}

		// If this field is type stringlist, we have to prepare the parameters
		if( params.type.toString() == 'STRINGLIST' ) {
			def listEntries = [];
			params.listEntries.eachLine {
				// We don't search for a listitem that might already exist,
				// because if we use that list item, it will be removed from the
				// other string list. 
				def name = it.trim();

				def listitem = new TemplateFieldListItem( name: name )
				listEntries.add( listitem );
			}

			params.listEntries = listEntries;
		} else {
			params.remove( 'listEntries' );
		}

		// If this field isnot a ontologyterm, we should remove the ontologies
		if( params.type.toString() != 'ONTOLOGYTERM' ) {
			params.remove( 'ontologies' );
		}

		// Create the template field and add it to the template
		def templateField = new TemplateField( params );
        if (templateField.save(flush: true)) {

			def html = g.render( template: 'elements/available', model: [templateField: templateField, ontologies: Ontology.list(), fieldTypes: TemplateFieldType.list()] );
			def output = [ id: templateField.id, html: html ];
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
	 * @param id	ID of the field to update
	 * @return		JSON object with two entries:
	 *					id: [id of this object]
	 *					html: HTML to replace the contents of the LI-item that was updated.
	 *				On error the method gives a HTTP response status 500 and the error
     */
    def updateField = {
        // Search for the template field
        def templateField = TemplateField.get( params.id );
        if( !templateField ) {
            response.status = 404;
            render 'TemplateField not found';
            return;
        }

        // Update the field if it is not updated in between
        if (params.version) {
            def version = params.version.toLong()
            if (templateField.version > version) {
                response.status = 500;
                render 'TemplateField was updated while you were working on it. Please reload and try again.';
                return
            }
        }

		// If this field is type stringlist or ontology, we have to prepare the parameters
		//
		// For stringlist and ontologyterm fields, the list items can be changed, even when the field is in use
		// In that case, only never-used items can be removed or changed and items can be added. If that is the case
		// params.is_disabled is true and we should combine listEntries and extraListEntries with the items already in use.
		if( params.type.toString() == 'STRINGLIST' || ( templateField.type == TemplateFieldType.STRINGLIST && params.is_disabled) ) {
			def listEntryLines = "";
			def listEntries = [];

			if( params.is_disabled ) {
				listEntries = templateField.getUsedListEntries();
			}

			if( params.listEntries ) {
				listEntryLines = params.listEntries;
				
				listEntryLines.eachLine {
					// We don't search for a listitem that might already exist,
					// because if we use that list item, it will be removed from the
					// other string list. We only search for an item that already
					// belongs to this list.
					def name = it.trim();

					def c = TemplateFieldListItem.createCriteria()
					def listitem = c.get {
						eq( "name", name )
						eq( "parent", templateField)
					}
					if( !listitem ) {
						listitem = new TemplateFieldListItem( name: name )
					}

					// Prevent using the same list entry twice
					if( !listEntries.contains( listitem ) )
						listEntries.add( listitem );
				}
			}

			// Add listEntries to the templateField
			params.listEntries = listEntries;
		} else {
			params.remove( 'listEntries' );
		}

		// If this field is a ontologyterm, we add ontology objects
		// For stringlist and ontologyterm fields, the list items can be changed, even when the field is in use
		// In that case, only never-used items can be removed or changed and items can be added. If that is the case
		// params.is_disabled is true and we should combine ontologies with the ontologies already in use.
		if( ( params.type.toString() == 'ONTOLOGYTERM' || ( templateField.type == TemplateFieldType.ONTOLOGYTERM && params.is_disabled ) ) && params.ontologies ) {
			def usedOntologies = [];

			if( params.is_disabled ) {
				usedOntologies = templateField.getUsedOntologies();
			}

			if( params.ontologies ) {
				def ontologies = params.ontologies;
	
				params.ontologies = usedOntologies + Ontology.getAll( ontologies.collect { Integer.parseInt( it ) } );
			}

		} else {
			params.remove( 'ontologies' );
		}

		// A field that is already used in one or more templates, but is not filled everywhere,
		// can not be set to required
		if( params.required ) {
			if( !templateField.isFilledInAllObjects() ) {
				response.status = 500;
				render "A field can only be marked as required if all objects using this field have a value for the field."
				return
			}
		}

		// Set all parameters
		templateField.properties = params

		templateField.validate();
		

        if (!templateField.hasErrors() && templateField.save(flush: true)) {

			// Remove all orphaned list items, because grails doesn't handle it for us
			TemplateFieldListItem.findAllByParent( templateField ).each {
				if( !params.listEntries.contains( it ) ) {
					templateField.removeFromListEntries( it );
					it.delete();
				}
			}

			// Select the template to use for the HTML output
			def renderTemplate = 'elements/available';
			if( params.renderTemplate == 'selected' ) {
				renderTemplate = 'elements/selected';
			}

			// Selected fields should have a template given
			def template = null;
			if( params.templateId )
				template = Template.findById( params.templateId );
			
			def html = g.render( template: renderTemplate, model: [template: template, templateField: templateField, ontologies: Ontology.list(), fieldTypes: TemplateFieldType.list()] );
			def output = [ id: templateField.id, html: html ];
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
	 * @param templateField	ID of the templatefield to move
	 * @return				JSON object with one entry:
	 *							id: [id of this object]
	 *						On error the method gives a HTTP response status 500 and the error
     */
    def deleteField = {
        // Search for the template field
        def  templateField = TemplateField.get( params.templateField );
        if( !templateField ) {
            response.status = 404;
            render 'TemplateField not found';
            return;
        }

        // Delete the template field
		try {
			templateField.delete(flush: true)

			def output = [ id: templateField.id ];
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
	 * @param template	ID of the template to add a field to
	 * @return			JSON object with two entries:
	 *						id: [id of this object]
	 *						html: HTML to replace the contents of the LI-item that was updated.
	 *					On error the method gives a HTTP response status 404 or 500 and the error
     */
    def addField = {
        // Search for the template
        def template = Template.get( params.template );

        if( !template ) {
            response.status = 404;
            render 'Template not found';
            return;
        }

        // Search for the template field
        def templateField = TemplateField.get( params.templateField );
        if( !templateField ) {
            response.status = 404;
            render 'TemplateField does not exist';
            return;
        }

        // The template field should exist within the template
        if( template.fields.contains( templateField ) ) {
            response.status = 500;
            render 'TemplateField is already found within template';
            return;
        }

		// If the template is in use, only non-required fields can be added
		if( template.inUse() && templateField.required ) {
			response.status = 500;
			render 'Only non-required fields can be added to templates that are in use.'
			return;
		}

		// All field names within a template should be unique
		if( template.fields.find { it.name.toLowerCase() == templateField.name.toLowerCase() } ) {
			response.status = 500;
			render 'This template already contains a field with name ' + templateField.name + '. Field names should be unique within a template.'
			return;
		}

		if( !params.position || Integer.parseInt( params.position ) == -1) {
			template.fields.add( templateField )
		} else {
			template.fields.add( Integer.parseInt( params.position ), templateField )
		}

	    if (!template.validate()) {
		    response.status = 500;
		    template.errors.each { render it}
	    }
	    template.save(flush:true);

		def html = g.render( template: 'elements/selected', model: [templateField: templateField, template: template, ontologies: Ontology.list(), fieldTypes: TemplateFieldType.list()] );
		def output = [ id: templateField.id, html: html ];
		render output as JSON;
    }

    /**
     * Removes a selected template field from the template using a AJAX call
	 *
	 * @param templateField	ID of the field to update
	 * @param template		ID of the template for which the field should be removed
	 * @return				JSON object with two entries:
	 *							id: [id of this object]
	 *							html: HTML to replace the contents of the LI-item that was updated.
	 *						On error the method gives a HTTP response status 404 or 500 and the error
     */
    def removeField = {
        // Search for the template
        def template = Template.get( params.template );

        if( !template ) {
            response.status = 404;
            render 'Template not found';
            return;
        }

        // Search for the template field
        def templateField = TemplateField.get( params.templateField );
        if( !templateField ) {
            response.status = 404;
            render 'TemplateField not found';
            return;
        }

        // The template field should exist within the template
        if( !template.fields.contains( templateField ) ) {
            response.status = 404;
            render 'TemplateField not found within template';
            return;
        }

		// If the template is in use, field can not be removed
		if( templateField.isFilledInTemplate(template) ) {
			response.status = 500;
			render 'Fields can not be removed from a template if it has been filled somewhere.'
			return;
		}

		// Delete the field from this template
        def currentIndex = template.fields.indexOf( templateField );
        template.fields.remove( currentIndex );
		template.save(flush:true);


		def html = g.render( template: 'elements/available', model: [templateField: templateField, ontologies: Ontology.list(), fieldTypes: TemplateFieldType.list()] );
		def output = [ id: templateField.id, html: html ];
		render output as JSON;
    }

    /**
     * Moves a template field using a AJAX call
     *
	 * @param template		ID of the template that contains this field
	 * @param templateField	ID of the templatefield to move
	 * @param position		New index of the templatefield in the array. The index is 0-based.
	 * @return				JSON object with two entries:
	 *							id: [id of this object]
	 *							html: HTML to replace the contents of the LI-item that was updated.
	 *						On error the method gives a HTTP response status 500 and the error
     */
    def moveField = {
        // Search for the template
        def template = Template.get( params.template );

        if( !template ) {
            response.status = 404;
            render 'Template not found';
            return;
        }

        // Search for the template field
        def  templateField = TemplateField.get( params.templateField );
        if( !templateField ) {
            response.status = 404;
            render 'TemplateField not found';
            return;
        }

        // The template field should exist within the template
        if( !template.fields.contains( templateField ) ) {
            response.status = 404;
            render 'TemplateField not found within template';
            return;
        }

        // Move the item
        def currentIndex = template.fields.indexOf( templateField );
        def moveField = template.fields.remove( currentIndex );

		println( "Old: " + currentIndex + " - New: " + params.position );
		
        template.fields.add( Integer.parseInt( params.position ), moveField );
		template.save(flush:true);

		def html = g.render( template: 'elements/selected', model: [templateField: templateField, template: template, fieldTypes: TemplateFieldType.list()] );
		def output = [ id: templateField.id, html: html ];
		render output as JSON;
    }

	/**
	 * Checks how many template use a specific template field
	 *
	 * @param	id	ID of the template field
	 * @return	int	Number of uses
	 */
	def numFieldUses = {
        // Search for the template field
        def  templateField = TemplateField.get( params.id );
        if( !templateField ) {
            response.status = 404;
            render 'TemplateField not found';
            return;
        }

		render templateField.numUses();
	}

	/**
	 * Adds a ontolgy based on the ID given
	 *
	 * @param	ncboID
	 * @return	JSON	Ontology object
	 */
	def addOntologyById = {
		def id = params.ncboID;

		if( !id ) {
			response.status = 500;
			render 'No ID given'
			return;
		}

		if( Ontology.findByNcboId( Integer.parseInt(  id ) ) ) {
			response.status = 500;
			render 'This ontology was already added to the system';
			return;
		}

		def ontology = null;

		try {
			ontology = dbnp.data.Ontology.getBioPortalOntology( id );
		} catch( Exception e ) {
			response.status = 500;
			render 'Ontology with ID ' + id + ' not found';
			return;
		}

		if( !ontology ) {
			response.status = 404;
			render 'Ontology with ID ' + id + ' not found';
			return;
		}

		// Validate ontology
		if (!ontology.validate()) {
			response.status = 500;
			render ontology.errors.join( '; ' );
			return;
		}

		// Save ontology and render the object as JSON
		ontology.save(flush: true)
		render ontology as JSON
	}

	/**
	 * Adds a ontolgy based on the term ID given
	 *
	 * @param	ncboID
	 * @return	JSON	Ontology object
	 * @deprecated	User addOntologyById instead, using the ncboId given by the JSON call
	 */
	def addOntologyByTerm = {
		def id = params.termID;

		if( !id ) {
			response.status = 500;
			render 'No ID given'
			return;
		}

		def ontology = null;

		try {
			ontology = dbnp.data.Ontology.getBioPortalOntologyByTerm( id );
		} catch( Exception e ) {
			response.status = 500;

			if( e.getMessage() )
				render e.getMessage()
			else
				render "Unknown error: " + e.getClass()
			return;
		}

		if( !ontology ) {
			response.status = 404;
			render 'Ontology form term ' + id + ' not found';
			return;
		}

		// Validate ontology
		if (!ontology.validate()) {
			response.status = 500;
			render ontology.errors.join( '; ' );
			return;
		}

		// Save ontology and render the object as JSON
		ontology.save(flush: true)
		render ontology as JSON
	}

    /**
     * Checks whether a correct entity is given
	 *
	 * @return	boolean	True if a correct entity is given. Returns false and raises an error otherwise
	 * @see		error()
     */
    def _checkEntity = {
        // got a entity get parameter?
        entityName = _parseEntityType();

        if( !entityName ) {
            error();
            return false;
        }

        // Create an object of this type
        entity = _getEntity( entityName );

        if( !entity ) {
            error();
            return false
        }

        return true;
    }


    /**
     * Checks whether the entity type is given and can be parsed
	 *
	 * @return	Name of the entity if parsing is succesful, false otherwise
     */
    def _parseEntityType() {
        def entityName;
        if (params.entity) {
            // decode entity get parameter
            if (grailsApplication.config.crypto) {
                    // generate a Blowfish encrypted and Base64 encoded string.
                    entityName = Blowfish.decryptBase64(
                    	params.entity,
                    	grailsApplication.config.crypto.shared.secret
					)
            } else {
                    // base64 only; this is INSECURE! Even though it is not
                    // very likely, it is possible to exploit this and have
                    // Grails dynamically instantiate whatever class you like.
                    // If that constructor does something harmfull this could
                    // be dangerous. Hence, use encryption (above) instead...
                    entityName = new String(params.entity.toString().decodeBase64())
            }

            return entityName;
        } else {
            return false;
        }
    }

    /**
     * Creates an object of the given entity.
	 * 
	 * @return False if the entity is not a subclass of TemplateEntity
     */
    def _getEntity( entityName ) {
        // Find the templates
        def entity = Class.forName(entityName, true, this.getClass().getClassLoader())

        // succes, is entity an instance of TemplateEntity?
        if (entity.superclass =~ /TemplateEntity$/ || entity.superclass.superclass =~ /TemplateEntity$/) {
            return entity;
        } else {
            return false;
        }

    }

	def templateEntityList = {
		def entities = [
			[ name: 'Study', entity: 'dbnp.studycapturing.Study' ],
			[ name: 'Subject', entity: 'dbnp.studycapturing.Subject' ],
			[ name: 'Event', entity: 'dbnp.studycapturing.Event' ],
			[ name: 'Sampling Event', entity: 'dbnp.studycapturing.SamplingEvent' ],
			[ name: 'Sample', entity: 'dbnp.studycapturing.Sample' ],
			[ name: 'Assay', entity: 'dbnp.studycapturing.Assay' ]
		]

		entities.each { 
			// add the entity class name to the element
			// do we have crypto information available?
			if (grailsApplication.config.crypto) {
				// generate a Blowfish encrypted and Base64 encoded string.
				it.encoded = URLEncoder.encode(
					Blowfish.encryptBase64(
						it.entity.toString().replaceAll(/^class /, ''),
						grailsApplication.config.crypto.shared.secret
					)
				)
			} else {
				// base64 only; this is INSECURE! As this class
				// is instantiated elsewehere. Possibly exploitable!
				it.encoded = URLEncoder.encode(it.entity.toString().replaceAll(/^class /, '').bytes.encodeBase64())
			}
		}

		return entities
	}
}
