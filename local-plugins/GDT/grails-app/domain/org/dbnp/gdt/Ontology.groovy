package org.dbnp.gdt

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

/**
 * This class describes an existing ontology, of which terms can be stored (actually 'cached' would be a better description)
 * in the (global) Term store.
 * This information is mapped from the BioPortal NCBO REST service, e.g.: http://rest.bioontology.org/bioportal/ontologies/38802
 * see http://www.bioontology.org/wiki/index.php/NCBO_REST_services
 *
 * Revision information:
 * $Rev: 1174 $
 * $Author: work@osx.eu $
 * $Date: 2010-11-19 10:55:15 +0100 (Fri, 19 Nov 2010) $
 */
class Ontology implements Serializable {
	String name             // bioontology: name
	String url              // bioontology: id
	String acronym       // bioontology: acronym
	Date dateCreated
	Date lastUpdated

	static constraints = {
        acronym(unique: true)     // For now, we just want one version of each NCBO ontology in the database
	}	

	/**
	 * Find child terms
	 * @return A set containing all terms that reside under this ontology
	 */
	Set<Term> giveTerms() {
		Term.findAllByOntology(this)
	}

	Object giveTermByName(String name) {
		giveTerms().find {
			it.name == name
		}

		/* TODO: find out why the following doesn't work (probably more efficient):
		Term.find {
			it.name == name
			it.ontology == this
		}
		}*/
	}

    /**
     * Return the Ontology by unique ontologyUrl, or create it if nonexistent.
     * @param ontologyUrl
     * @return Ontology
     */
    static Ontology getOrCreateOntology( String ontologyUrl) {
        def ontology = findByUrl( ontologyUrl )

        // got an ontology?
        if (!ontology) {
            // no, fetch it from the webservice
            ontology = getBioOntology( ontologyUrl )

            if (ontology && ontology.validate() && ontology.save(flush:true)) {
                ontology.refresh()
            }
        }

        return ontology
    }

    static Ontology getBioOntology(String ontologyUrl) {
        def http = new HTTPBuilder( ontologyUrl )
        http.request( GET, JSON ) {
            headers.'Authorization' = 'apikey token='+ getBioOntologyApiKey()

            response.success = { response, ontology ->
                new Ontology(
                        name: ontology.name,
                        url: ontology['@id'],
                        acronym: ontology.acronym
                );
            }

            response.failure = { resp ->
                println ("ERROR: ontology with ontologyUrl ${ontologyUrl} could not be found! Server terturned: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}")
            }
        }
    }

    static String getBioOntologyApiKey() {
        def grailsApplication = new Ontology().domainClass.grailsApplication
        return grailsApplication.config.bioontology.apikey
    }
}
