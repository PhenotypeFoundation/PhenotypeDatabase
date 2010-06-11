package dbnp.data

/**
 * This class describes an existing ontology, of which terms can be stored (actually 'cached' would be a better description)
 * in the (global) Term store.
 * This information is mapped from the BioPortal NCBO REST service, e.g.: http://rest.bioontology.org/bioportal/ontologies/38802
 * @see http://www.bioontology.org/wiki/index.php/NCBO_REST_services
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Ontology implements Serializable {
	String name             // BioPortal: displayLabel
	String description      // BioPortal: description
	String url              // BioPortal: homepage
	String versionNumber    // BioPortal: versionNumber
	int ncboId              // BioPortal: ontologyId
	int ncboVersionedId     // BioPortal: id

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

	static Ontology getBioPortalOntology(String ncboId) {
		// Get ontology from BioPortal via Ontocat
		// TODO: maybe make a static OntologyService instance to be more efficient, and decorate it with caching?
		uk.ac.ebi.ontocat.OntologyService os = new uk.ac.ebi.ontocat.bioportal.BioportalOntologyService()
		uk.ac.ebi.ontocat.Ontology o = os.getOntology(ncboId)

		// Instantiate and return Ontology object
		new dbnp.data.Ontology(
			name: o.label,
			description: o.description,
			url: o.properties['homepage'] ?: "http://bioportal.bioontology.org/ontologies/${o.id}",
			versionNumber: o.versionNumber,
			ncboId: o.ontologyAccession,
			ncboVersionedId: o.id
		);
	}

	static Ontology getBioPortalOntologyByTerm(String termId) {
		// Get ontology from BioPortal via Ontocat
		// TODO: maybe make a static OntologyService instance to be more efficient, and decorate it with caching?
		uk.ac.ebi.ontocat.OntologyService os = new uk.ac.ebi.ontocat.bioportal.BioportalOntologyService()
		uk.ac.ebi.ontocat.OntologyTerm term = os.getTerm( termId );
		println( term );
		uk.ac.ebi.ontocat.Ontology o = os.getOntology( term.getOntologyAccession() );
		println( o );

		// Instantiate and return Ontology object
		new dbnp.data.Ontology(
			name: o.label,
			description: o.description,
			url: o.properties['homepage'] ?: "http://bioportal.bioontology.org/ontologies/${o.id}",
			versionNumber: o.versionNumber,
			ncboId: o.ontologyAccession,
			ncboVersionedId: o.id
		);
	}
}
