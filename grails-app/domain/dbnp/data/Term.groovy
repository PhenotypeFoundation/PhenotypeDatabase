package dbnp.data

import dbnp.data.Ontology

/**
 * The Term object describes a term in the ontology that is referred to in other entities such as events.
 * The Term object should point to an existing term in an online ontology, therefore instances of this class can also
 * be seen as a cache of elements of the external ontology.
 * BioPortal example: Mus musculus: http://rest.bioontology.org/bioportal/concepts/38802/NCBITaxon:10090
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Term implements Serializable {
	static searchable = true

	String name             // BioPortal: label (preferred name)
	Ontology ontology       // Parent ontology. To enable the unique constraints, we describe the Ontology-Term relation here
	String accession        // BioPortal: conceptId

	static constraints = {
		accession(unique: 'ontology')   // Accession should be unique within an ontology
		name(unique: 'ontology')        // Preferred name should be unique within an ontology
        name(size: 1..255)              // Name should be a non-empty string
	}

	def String toString() {
		return name
	}

	/**
	 * Return all terms for a string of comma separated ontology ncboId's.
	 * @see Ontology.groovy
	 * @param ontologies
	 * @return
	 */
	def giveAllByOntologies( ontologies ) {
		// this method does not seem to work (see taglibrary:termSelect)
		// i'll try to get it working later, or delete this altogether
		// - Jeroen
		def data = []
		def terms = []

		// got a string?
		if (ontologies instanceof String) {
			// split the ontologies string
			ontologies.split(/\,/).each() { ncboId ->
				// trim the id
				ncboId.trim()

				// fetch all terms for this ontology
				def ontology = Ontology.findAllByNcboId(ncboId)

				// does this ontology exist?
				if (ontology) {
					ontology.each() {
						data[ data.size() ] = it
					}
				}
			}

			ontologies = data
		}

		// iterate through ontologies
		ontologies.each() { ontology ->
			Term.findAllByOntology( ontology ).each() { term ->
				terms[ terms.size() ] = term
			}
		}

		// sort alphabetically
		terms.sort()
	}
}