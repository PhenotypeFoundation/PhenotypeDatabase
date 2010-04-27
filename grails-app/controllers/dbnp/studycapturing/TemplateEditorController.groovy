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
	/**
	 * index closure
	 */
    def index = {
		// got a entity get parameter?
		def entity = null
		if (params.entity) {
			// decode entity get parameter
			if (grailsApplication.config.crypto) {
				// generate a Blowfish encrypted and Base64 encoded string.
				entity = Blowfish.decryptBase64(
					params.entity,
					grailsApplication.config.crypto.shared.secret
				)
			} else {
				// base64 only; this is INSECURE! Even though it is not
				// very likely, it is possible to exploit this and have
				// Grails dynamically instantiate whatever class you like.
				// If that constructor does something harmfull this could
				// be dangerous. Hence, use encryption (above) instead...
				entity = new String(params.entity.toString().decodeBase64())
			}
		}

		// go with the flow!
    	redirect(action: 'pages', params:["entity":entity])
    }

	/**
	 * Webflow
	 */
	def pagesFlow = {
		// start the flow
		start {
			action {
				// define initial flow variables
				flow.entity = null
				flow.templates = []

				// define success variable
				def errors = true

				// got an entity parameter?
				if (params.entity && params.entity instanceof String) {
					// yes, try to dynamicall load the entity
					try {
						// dynamically load the entity
						def entity = Class.forName(params.entity, true, this.getClass().getClassLoader())

						// succes, is entity an instance of TemplateEntity?
						if (entity.superclass =~ /TemplateEntity$/) {
							errors = false

							// yes, assign entity to the flow
							flow.entity = entity

							// fetch all templates to this entity
							flow.templates = Template.findAllByEntity(entity)
							
							// find all template fields for this particular entity
							// for now, all
							// TODO: limit for this entity only
							flow.allTemplateFields = TemplateField.findAll().sort{ it.name }
						}
					} catch (Exception e) { }
				}

				// success?
				if (errors) {
					error()
				} else {
					success()
				}
			}
			on("success").to "templates"
			on("error").to "errorInvalidEntity"
		}

		// could not dynamically load entity, possible hack
		// or invalid entity specified in template field
		errorInvalidEntity {
			render(view: "errorInvalidEntity")
		}

		// main template editor page
		templates {
			render(view: "templates")
			onRender {
				// template parameter given?
				if (params.template) {
					// yes, find template by name
					flow.template = Template.findByName(params.template)
					flow.templateFields = flow.allTemplateFields

					flow.template.fields.each() {
						println it
						flow.templateFields.remove(it)
						
					}
					println "count: "+flow.template.fields.size()

					println "---"
					flow.allTemplateFields.each() {
						println it
					}
					println "count: "+flow.allTemplateFields.size()
					println "---"

					println flow.allTemplateFields.class
					println flow.template.fields.class
					println "---"
				}
			}
			on("next").to "start"
		}
	}
}
