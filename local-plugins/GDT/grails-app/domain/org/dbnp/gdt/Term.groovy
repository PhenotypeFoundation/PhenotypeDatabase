package org.dbnp.gdt

/**
 * The Term object describes a term in the ontology that is referred to in other entities such as events.
 * The Term object should point to an existing term in an online ontology, therefore instances of this class can also
 * be seen as a cache of elements of the external ontology.
 * BioPortal example: Mus musculus: http://rest.bioontology.org/bioportal/concepts/38802/NCBITaxon:10090
 *
 * Revision information:
 * $Rev: 959 $
 * $Author: j.a.m.wesbeek@umail.leidenuniv.nl $
 * $Date: 2010-10-20 21:13:14 +0200 (Wed, 20 Oct 2010) $
 */
class Term implements Serializable {
    //static searchable = { [only: ['ontology']] }

	String name             // BioPortal: prefLabel (preferred name)
	Ontology ontology       // Parent ontology. To enable the unique constraints, we describe the Ontology-Term relation here
	String accession        // BioPortal: conceptId

	Date dateCreated
	Date lastUpdated

	static constraints = {
		accession(unique: true)     // Accession should be unique db-wide
		name(unique: 'ontology')    // Preferred name should be unique within an ontology
        name(size: 1..255)          // Name should be a non-empty string
	}

	def String toString() {
		return name
	}

	/**
	 * get or create a term
	 * @param name
	 * @param ontology
	 * @param accession
	 * @return
	 */
	static public getOrCreateTerm(String name, Ontology ontology, String accession) {
		def term = ontology.giveTerms().find { it.name == name }

		// got the term?
		if (term) {
			return term
		} else {
			// create a new term
			term = new Term(
				name		: name,
				ontology	: ontology,
				accession	: accession
        	)

			if (term.validate() && term.save(failOnError:true)) {
				return term
			}
		}
	}
}
