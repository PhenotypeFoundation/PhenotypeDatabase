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
import cr.co.arquetipos.crypto.Blowfish
import grails.converters.*

class TemplateEditorController {
    def entityName;
    def entity;

    /**
     * index closure
     */
    def index = {
        // Check whether a right entity is given
        _checkEntity();

        // fetch all templates for this entity
        def templates = Template.findAllByEntity(entity)

        // Check whether a template is already selected
        def selectedTemplate = params.template;
        def template = null;

        if( selectedTemplate ) {
            template = Template.get( selectedTemplate );
        }

        return [
            entity: entity,
            templates: templates,
            encryptedEntity: params.entity,
            fieldTypes: TemplateFieldType.list(),
            
            template: template
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
     * Adds a new template field using a AJAX call
	 *
	 * @param template	ID of the template to add a field to
	 * @return			JSON object with two entries:
	 *						id: [id of this object]
	 *						html: HTML to replace the contents of the LI-item that was updated.
	 *					On error the method gives a HTTP response status 500 and the error
     */
    def addField = {
        // Search for the template
        def template = Template.get( params.template );

        if( !template ) {
            response.status = 404;
            render 'Template not found';
            return;
        }

		// Create the template field and add it to the template
		def templateField = new TemplateField( params );
        if (templateField.save(flush: true)) {
			template.fields.add( templateField );

			def html = g.render( template: 'elements/liContent', model: [templateField: templateField, fieldTypes: TemplateFieldType.list()] );
			def output = [ id: templateField.id, html: html ];
			render output as JSON;

            //render '';
        } else {
            response.status = 500;
            render 'TemplateField could not be added because errors occurred.';
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
    def update = {
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
        templateField.properties = params
        if (!templateField.hasErrors() && templateField.save(flush: true)) {
			def html = g.render( template: 'elements/liContent', model: [templateField: templateField, fieldTypes: TemplateFieldType.list()] );
			def output = [ id: templateField.id, html: html ];
			render output as JSON;
        } else {
            response.status = 500;
            render 'TemplateField was not updated because errors occurred.';
            return
        }
    }

    /**
     * Deletes a selected template field from the template using a AJAX call
	 *
	 * @param templateField	ID of the field to update
	 * @param template		ID of the template for which the field should be removed
	 * @return				Status code 200 on success, 500 otherwise
     */
    def delete = {
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

		// Delete the field from this template
        def currentIndex = template.fields.indexOf( templateField );
        template.fields.remove( currentIndex );
		template.save();
		render '';

		/*
		 *try {
			templateField.delete(flush: true)
			render "";
			return;
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			response.status = 500;
			render "Templatefield not deleted: " + e.getMessage();
			return;
		}
		*/
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
    def move = {
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
        template.fields.add( Integer.parseInt( params.position ), moveField );

		def html = g.render( template: 'elements/liContent', model: [templateField: templateField, fieldTypes: TemplateFieldType.list()] );
		def output = [ id: templateField.id, html: html ];
		render output as JSON;
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
            retur; false
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
        if (entity.superclass =~ /TemplateEntity$/) {
            return entity;
        } else {
            return false;
        }

    }
}
