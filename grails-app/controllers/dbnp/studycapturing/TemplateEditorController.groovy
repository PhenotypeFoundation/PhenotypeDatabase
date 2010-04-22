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

class TemplateEditorController {
	/**
	 * index closure
	 */
    def index = {
		// got a entity get parameter?
		def entity = null
		if (params.entity) {
			// decode entity get parameter
			entity = new String(params.entity.toString().decodeBase64())
		}

		// got with the flow!
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

		// error dynamically loading entity
		errorInvalidEntity {
			render(view: "_errorInvalidEntity")
		}

		// main template editor page
		templates {
			render(view: "/templateEditor/templates")
			onRender {
				println "render templates"
			}
			on("next").to "start"
		}
	}
}
