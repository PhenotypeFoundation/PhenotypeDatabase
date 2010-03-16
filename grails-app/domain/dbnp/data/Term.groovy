package dbnp.data

import dbnp.data.Ontology

/**
 * The Term object describes a term in the ontology that is referred to in other entities such as events.
 * The Term object should point to an existing term in an online ontology, therefore instances of this class can also
 * be seen as a cache of elements of the external ontology.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Term implements Serializable {
	static searchable = true
	String name
	Ontology ontology
	String accession

	static constraints = {
	}

	def String toString() {
		return name
	}


	// Covenenice method for delivering Terms.
	// if the term is already defined, use it.
	// otherwise, create it and return it.
	// should be removed when ontologies work.
	static getTerm( string ) {
	    def term = Term.find("from Term as t where t.name = '${string}'")
	    if( term==null ) { term = new Term()
		    term.name=string
		    term.ontology = Ontology.find('from Ontology as o')
		    term.accession = ''
		    if( !term.save(flush:true) )  {
			    term.errors.each{ println it }
		    }
	    }
	    return term
	}
}