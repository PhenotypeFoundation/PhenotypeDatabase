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
				println "renderderender!"
				render('henkie')
			}
			on("add") {
				println params
				def ontology = Ontology.findByNcboVersionedId( params.get('term-ontology_id') as int )

				// do we have an ontology?
				if (!ontology) {
					// maak eerst deze ontology aan. Er zijn web services beschikbaar om
					// de Ontology properties op te halen.... mag jij maken, leuk he!
					println "neeeeee geen ontology!"
					println "ik moet ff deze ontology aanmaken in onze database!"
				}

				// instantiate term with parameters
				def term = new Term(
					name: params.get('term'),
					ontology: ontology,
					accession: params.get('term-concept_id')
				)

				// validate term
				if (term.validate()) {
					println "jaaaa het was kei goed!"
					term.save()
					success()
				} else {
					println "klopt voor geen meter!"
					println "errors:"
					term.errors.getAllErrors().each() {
						println it
					}
					flash.errors = term.errors
					error()
				}
			}.to "terms"
		}
	}
}
