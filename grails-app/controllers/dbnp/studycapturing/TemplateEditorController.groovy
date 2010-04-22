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
		onStart {
			println params
			if (params.entity) {
				//def name = params.entity
				//def name = "java.lang.Integer"
				//println name.class
				//def S = new dbnp.studycapturing.Study()

				//def name = "dbnp.studycapturing.Study" as Class
				//println name.newInstance()
				println Class.forName(new GString("dbnp.studycapturing.Study"))
				//flow.entity = (name as Class).newInstance()
				//flow.templates = Template.findAllByEntity(flow.entity)
			} else {
				// all templates for all entities; note that normally
				// this shouldn't happen as this flow is designed to
				// launch in a jquery dialog, passing an encoded entity
				flow.templates = Template.findAll()
			}
			flow.templates = Template.findAll()
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
