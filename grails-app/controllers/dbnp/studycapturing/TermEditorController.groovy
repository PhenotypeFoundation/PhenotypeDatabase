/**
 * TermEditorController Controller
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

import dbnp.data.Term
import dbnp.data.Ontology

class TermEditorController {
	/**
	 * index closure
	 */
    def index = {
		// got a ontology get parameter?
		def ontologies = (params.ontologies) ? params.ontologies : null

		// enter the flow!
    	redirect(action: 'pages', params:["ontologies":ontologies])
    }

	/**
	 * Webflow
	 */
	def pagesFlow = {
		// start the flow
		onStart {
			println "start term / ontology editor flow"

			if (params.ontologies) {
				flow.ontologies		= params.ontologies
				flow.ontologiesList	= []
				params.ontologies.split(/\,/).each() { ncboId ->
					// trim the id
					ncboId.trim()

					// and add to the flow scope
					flow.ontologiesList[ flow.ontologies.size() ] = ncboId
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
				println "Rendering term selection popup"
			}
			on("add") {
				println params
				def ontology = Ontology.findByNcboVersionedId( params.get('term-ontology_id') as int )
                def strTerm = params.get('term')

				// do we have an ontology?
				if (!ontology) {
					// TODO: if ontology is missing, create it
                    // pending possible addition to OntoCAT BioportalOntologyService API of search by versioned Ontology Id
                    println "Ontology is empty"
				}

				// instantiate term with parameters
				def term = new Term(
					name: strTerm,
					ontology: ontology,
					accession: params.get('term-concept_id')
				)

				// validate term
				if (term.validate()) {
					println "Term validated correctly"
					if (term.save(flush:true)) {
						println ".term save ok"
					} else {
						println ".term save failed?"
					}
					success()
                    flash.message = "Term addition succeeded"
				} else {
					println "Term validation failed"
					println "errors:"
					term.errors.getAllErrors().each() {
						println it
					}
					error()
                    flash.message = "Term addition failed"
				}
			}.to "terms"
		}
	}
}
