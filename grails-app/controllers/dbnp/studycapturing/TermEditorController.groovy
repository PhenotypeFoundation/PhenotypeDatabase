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
			}
		}

		// main term editor page
		terms {
			render(view: "terms")
			onRender {
				println ".rendering term selection popup"
			}
			on("add") {
				println params
				def ontology = Ontology.findByNcboVersionedId( params.get('term-ontology_id') as int )
                def strTerm = params.get('term')

				// do we have an ontology?
				if (!ontology) {
					println ".ontology missing, first fetch ontology information"

					// use the NCBO REST service to fetch ontology information
					def url = "http://rest.bioontology.org/bioportal/ontologies/" + params.get('term-ontology_id')
					def xml = new URL( url ).getText()
					def data = new XmlParser().parseText( xml )
					def bean = data.data.ontologyBean

					// instantiate Ontology with the proper values
					ontology = Ontology.getBioPortalOntologyByVersionedId( params.get('term-ontology_id') ).save(flush:true)
					println ontology

					if (ontology.validate()) {
						ontology.save(flush:true)
					}
					println ontology
				}

				// instantiate term with parameters
				def term = new Term(
					name: strTerm,
					ontology: ontology,
					accession: params.get('term-concept_id')
				)

				// validate term
				if (term.validate()) {
					// save the term to the database
					if (term.save(flush:true)) {
						flash.message = "Term addition succeeded"
						success()
					} else {
						flash.message = "Oops, we encountered a problem while storing the selected term. Please try again."
						term.errors.each() { println it }
						error()
					}
				} else {
					// term did not validate properly
					flash.message = "Oops, we encountered a problem while storing the selected term. Please try again."
					term.errors.each() { println it }
					error()
				}
			}.to "terms"
		}
	}
}
