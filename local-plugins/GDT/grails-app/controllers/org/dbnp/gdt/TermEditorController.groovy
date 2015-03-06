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
 * $Rev: 1457 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-31 14:14:35 +0100 (Mon, 31 Jan 2011) $
 */
package org.dbnp.gdt

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
			log.info ".start term / ontology editor flow"

            flow.apikey = Ontology.getBioOntologyApiKey()
			if (params.ontologies) {
				flow.ontologies		= params.ontologies
				flow.ontologiesList	= []
				params.ontologies.split(/\,/).each() { acronym ->
					// trim the id
                    acronym.trim()

					// and add to the flow scope
					flow.ontologiesList[ flow.ontologies.size() ] = acronym
				}
			}
		}

		// main term editor page
		terms {
			render(view: "terms")
			onRender {
				log.info ".rendering term selection popup"
			}
			on("add") {
                def ontologyUrl = (params.containsKey('term-ontology_id')) ? (params['term-ontology_id'] as String) : ""
				def ontology = null

                try {
                    ontology = Ontology.getOrCreateOntology(ontologyUrl)
                } catch (Exception e) {
                    response.status = 500;
                    render 'Ontology with ID ' + ontologyUrl + ' not found';
                    return;
                }

				try {
					// instantiate term with parameters
					def term = new Term(
						name: params.get('term'),
						ontology: ontology,
						accession: params.get('term-concept_id')
					)

					// validate term
					if (term.validate()) {
						// save the term to the database
						if (term.save(flush:true)) {
							flash.message = "'" + params.get('term') + "' was successfully added, either search for another term to add or close this window"
							success()
						} else {
							flash.errors = ["We encountered a problem while storing the selected term. Please try again."]
							//term.errors.each() { println it }
							error()
						}
					} else {
						// term did not validate properly
						if (term.errors =~ 'unique') {
							flash.errors = ["'" + params.get('term') + "' already exists, either search for another term or close this window"]
						} else {
							flash.errors = ["We encountered a problem while storing the selected term. Please try again."]
						}

						error()
					}
				} catch (Exception e) {
					flash.errors = ["${e.getMessage()}"]

					error()
				}
			}.to "terms"
		}
	}
}