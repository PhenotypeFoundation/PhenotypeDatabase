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

class TemplateEditorController {
	/**
	 * index closure
	 */
    def index = {
		// got a template get parameter?
		def template = (params.template) ? params.template : null

		// enter the flow!
    	redirect(action: 'pages', params:["template":template])
    }

	/**
	 * Webflow
	 */
	def pagesFlow = {
		// start the flow
		onStart {
			println "start template editor flow"

			if (params.template) {
				flow.template = template
			}
		}

		// main template editor page
		templates {
			render(view: "/templateEditor/templates", collection:[templates: Template.findAll()])
			onRender {
				println "render templates"
				println Template.findAll()
			}
			on("next").to "start"
		}
	}
}
