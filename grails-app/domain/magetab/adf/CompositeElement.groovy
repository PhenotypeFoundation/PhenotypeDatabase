package magetab.adf

class CompositeElement {

    	String name
	String comment
	magetab.idf.OntologyTerm dataBaseEntry

	static constraints = {
		dataBaseEntry(nullable:true)
     }
}
