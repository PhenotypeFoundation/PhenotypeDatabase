/**
 * TemplateController Controler
 *
 * An overview of 
 *
 * @author  Kees van Bochove
 * @since	20100726
 * @package	dbnp.studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.studycapturing

// Automatic marshalling of XML and JSON
import org.dbnp.gdt.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import dbnp.authentication.AuthenticationService
import grails.util.Holders

class TemplateController {
    def authenticationService

    /**
     * Shows a form to pick a file to import templates
     */
    def importTemplate = {
    }

    /**
     * Returns a JSON list with all templates for the given entity
     */

    def getAllForEntity() {
        def entityName = params.entity
        def entity

        try {
            entity = Class.forName(entityName, true, Holders.grailsApplication.getClassLoader())

            render Template.findAllByEntity(entity).collectEntries { [ (it.id): it.name ] } as JSON
        } catch( Exception e ) {
            response.status = 400
            render "Invalid entity given"
        }
    }

    /**
     * Handles file import
     */
    def handleImportedFile = {
        if( !request.getFile("file") ) {
            flash.message = "No file given.";
            redirect( action: 'importTemplate' );
            return;
        }

        // Parse XML
        def xml

        try {
            xml = XML.parse( request.getFile("file").inputStream.text )
        } catch( Exception e ) {
            // Error in parsing. Probably the file is not a XML file
            flash.message = "Imported file could not be read. Please specify an XML template file.";
            redirect( action: 'importTemplate' );
        }

        def numTemplates = xml.@count;

        if( !xml.template ) {
            flash.message = "No templates could be found in the imported file. Please specify an XML template file.";
            redirect( action: 'importTemplate' );
        }

        // Check whether the templates already exist
        def templates = []
        def id = 0;
        xml.template.each { template ->
            try {
                def t = Template.parse( template, authenticationService.getLoggedInUser() );

                def templateData = [:]
                templateData.key = id++;
                templateData.template = t
                templateData.alternatives = []

                // If a template exists that equals this xml template , return it.
                def existingTemplates = Template.findAllByEntity( t.entity )
                for( def otherTemplate in existingTemplates ) {
                    if( t.contentEquals( otherTemplate ) ) {
                        templateData.alternatives << otherTemplate;
                    }
                }

                // If a template with this name already exists, tell the user to choose another name
                if( existingTemplates.find { it.name == t.name } ) {
                    templateData.requireNameChange = true
                }

                // Pass existing template names to the view, to ensure uniqueness
                templateData.existingNames = existingTemplates*.name

                templates << templateData
            } catch (Exception e) {
                templates << [ template: null, error: "Template " + ( template.name ?: " without name" ) + " could not be parsed: " + e ];
            }
        }

        // Save templates in session in order to have data available in the next (import) step
        session.templates = templates

        [templates: templates]
    }

    /**
     * Saves the imported templates that the user has chosen
     */
    def saveImportedTemplates = {
        def ids = params.selectedTemplate
        def templates = session.templates
        def messages = []

        // Save all selected templates
        ids.each { id ->
            def templateData = templates.find { template -> template.key == id.toLong() }
            def importTemplate = templateData?.template

            if( !importTemplate ) {
                messages << "Template with id " + id + " could not be found."
            } else {
                def originalName = importTemplate.name
                def newName = null

                // Check whether a new name has been given
                if( params[ 'templateNames_' + id ] && params[ 'templateNames_' + id ] != importTemplate.name ) {
                    importTemplate.name = params[ 'templateNames_' + id ]
                    newName = params[ 'templateNames_' + id ]
                }

                // Check whether the name is unique. This validation is not added to the domain object
                // due to a bug in GORM/Hibernate.
                // As we are importing, we can be sure that the entity is not persisted yet.
                println "Entity: " + importTemplate.entity
                println "Name: " + importTemplate.name
                def existingTemplates = Template.findAllByEntity(importTemplate.entity)
                
                if( existingTemplates.find { it.name == importTemplate.name } ) {
                    messages << "Template " + originalName + " could not be saved, as a template with the name " + importTemplate.name + " already exists."
                } else if( importTemplate.save() ) {
                    messages << "Template " + originalName + " saved" + ( newName ? " as " + newName : "" )
                } else {
                    messages << "Template " + originalName + " could not be saved"
                }
            }
        }

        // Remove templates from the session
        session.templates = null

        [messages: messages]
    }

    /**
     * Shows a form to select templates for export
     */
    def export = {
        // If the templates are already selected, export them
        if( params.templates ) {
            if( !( params.templates instanceof String ) ) {
                params.templates = params.templates.join(",")
            }

            switch( params.type ) {
                case "xml":
                default:
                    xml();
                    return
            }
            return;
        } else {
            [templates: Template.findAll()]
        }
    }

    /**
     * XML Export of given templates, or all templates if no templates are given
     */
    def xml = {
        def templates
        if( params.templates ) {
            def ids = [];
            params.templates.split(",").each { ids << it.toLong() }

            def c = Template.createCriteria()
            templates = c {
                'in'("id", ids)
            }
        } else {
            templates = Template.findAll()
        }

        response.setHeader "Content-disposition", "attachment; filename=templates.xml"

        render(view: 'xml', contentType:"text/xml", model: [templates: templates])
    }

}
