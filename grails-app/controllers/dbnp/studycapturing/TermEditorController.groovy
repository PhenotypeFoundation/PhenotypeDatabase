/**
 * TermEditorController Controler
 *
 * Webflow driven term editor
 *
 * @author  Jeroen Wesbeek
 * @since	20100420
 * @package	studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.studycapturing

class TermEditorController {
	/**
	 * index closure
	 */
    def index = {
		// got a ontology get parameter?
		def ontology = (params.ontology) ? params.ontology : null

		// enter the flow!
    	redirect(action: 'pages', params:["ontology":ontology])
    }

	/**
	 * Webflow
	 */
	def pagesFlow = {
		// start the flow
		onStart {
			println "start term / ontology editor flow"

			if (params.ontology) {
				flow.ontologies = []
				params.ontology.split(/\,/).each() { ncboId ->
					// trim the id
					ncboId.trim()

					// and add to the flow scope
					flow.ontologies[ flow.ontologies.size() ] = ncboId
				}

				/*** EXAMPLE OF HOW TO FETCH ONTOLOGY INSTANCES
				 * ontologies.each() {
				 * 	 println Ontology.findAllByNcboId( it )
				 * }
				 */
			}
		}

		// main term editor page
		terms {
			render(view: "terms")
			onRender {
				println "renderderender!"
			}
			on("next").to "start"
		}
	}
}
