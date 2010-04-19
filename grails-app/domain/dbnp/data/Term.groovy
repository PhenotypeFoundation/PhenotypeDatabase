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
	Ontology ontology       // Parent ontology
	String accession        // BioPortal: fullId

	static constraints = {
		accession(unique: 'ontology')
	}

	def String toString() {
		return name
	}

}