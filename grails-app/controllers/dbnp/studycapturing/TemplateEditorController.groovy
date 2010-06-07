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
     * Updates a selected template field using a AJAX call
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
            render '';
        } else {
            response.status = 500;
            render 'TemplateField was not updated because errors occurred.';
            return
        }
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
     * Moves a template field using a AJAX call
     *
     * 
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

        render "";
    }

    /**
     * Checks whether a correct entity is given
     */
    def _checkEntity = {
        // got a entity get parameter?
        entityName = _parseEntityType();

        if( !entityName ) {
            error();
            return;
        }

        // Create an object of this type
        entity = _getEntity( entityName );

        if( !entity ) {
            error();
            return;
        }

        return true;
    }


    /**
     * Checks whether the entity type is given and can be parsed
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
     * Creates an object of the given entity. Returns false is the entity
     *   is not a subclass of TemplateEntity
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
