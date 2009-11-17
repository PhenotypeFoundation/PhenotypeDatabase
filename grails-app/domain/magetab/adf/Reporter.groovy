package magetab.adf

class Reporter {

    String name
    String sequence
    magetab.idf.OntologyTerm group
    magetab.idf.OntologyTerm controlType
    magetab.idf.OntologyTerm databaseEntry

    static constraints ={
        group(nullable:true)
        controlType(nullable:true)
        databaseEntry(nullable:true)
    }
}
