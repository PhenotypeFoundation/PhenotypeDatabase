package dbnp.transcriptomics.magetab.adf

class CompositeElement {

    	String name
	String comment
	dbnp.transcriptomics.magetab.idf.OntologyTerm dataBaseEntry

	static constraints = {
		dataBaseEntry(nullable:true)
     }
}
