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
			println ".start term / ontology editor flow"

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
				// get ontology by ncboVersionedId
				def ontology = Ontology.findByNcboVersionedId( params.get('term-ontology_id') as int )
                def strTerm = params.get('term')

				// do we have an ontology?
				if (!ontology && params.get('term-ontology_id')) {
					// no, so either this is a new ontology that does not yet
					// exist in the database, or it is a new version of an
					// ontology that is already present in the database
					println ".ontology missing, first fetch ontology information"

					// use the NCBO REST service to fetch ontology information
					try {
						def url = "http://rest.bioontology.org/bioportal/ontologies/" + params.get('term-ontology_id')
						def xml = new URL( url ).getText()
						def data = new XmlParser().parseText( xml )
						def bean = data.data.ontologyBean

						// instantiate Ontology with the proper values
						ontology = Ontology.getBioPortalOntologyByVersionedId( params.get('term-ontology_id') )

						// check if this is a newer version of an existing ontology
						def checkOntology = Ontology.findByName( ontology.name )
						if ( checkOntology ) {
							// this is a newer version of an existing Ontology, update
							// the ontology to a newer version. This is not the best
							// way to handle these updates as we don't know if terms
							// have been updated. However, introducing different versions
							// of Ontologies results into numerous difficulties as well:
							//	- what to do with studies that rely on an older ontology
							//	- when a new ontology is added, the existing terms of the
							//	  older version are lacking in the new version
							//	- the webservice can only search on ontologyid, not on
							//	  versions of ncboVersioned id's
							//	- if the name has changed between versions this check
							//	  will not work anymore
							//	- etc :)
							// So for now, we will just update the existing ontology with
							// the new information until it becomes clear this needs a
							// more thorough workaround...
							//
							// Jeroen, 20101026

							// update ontology values
							checkOntology.ncboVersionedId	= ontology.ncboVersionedId
							checkOntology.versionNumber		= ontology.versionNumber
							checkOntology.url				= ontology.url

							// store the ontology
							if ( checkOntology.validate() ) {
								println ".updated ontology with new version information"
								checkOntology.save(flush:true)

								// and use this existing ontology
								ontology = checkOntology
							}
						} else if ( ontology.validate() ) {
							// store the ontology
							println ".adding new ontology"
							ontology.save(flush:true)
						}
					} catch (Exception e) {
						// something went wrong, probably the
						// ontology-id is invalid (hence, the term
						// is invalid)
						println ".oops? --> " + e.getMessage()
						flash.errors = ["We could not add the ontology for this term, please try again"]
					}
				}

				// got an error?
				if (!flash.errors) {
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
							flash.message = "'" + params.get('term') + "' was successfully added, either search for another term to add or close this window"
							success()
						} else {
							flash.errors = ["We encountered a problem while storing the selected term. Please try again."]
							term.errors.each() { println it }
							error()
						}
					} else {
						// term did not validate properly
						term.errors.each() { println it }
						if (term.errors =~ 'unique') {
							flash.errors = ["'" + params.get('term') + "' already exists, either search for another term or close this window"]
						} else {
							flash.errors = ["We encountered a problem while storing the selected term. Please try again."]
						}

						error()
					}
				}
			}.to "terms"
		}
	}
}
