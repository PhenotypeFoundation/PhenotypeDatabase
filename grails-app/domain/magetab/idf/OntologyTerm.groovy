package magetab.idf

class OntologyTerm {

    String text
    String category
    String accessionNumber
    TermSource termSource


    static constraints = {
        termSource(nullable: true)
    }
}
