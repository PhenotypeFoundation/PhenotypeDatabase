package dbnp.transcriptomics.magetab.adf

class Reporter {

    String name
    String sequence
    dbnp.transcriptomics.magetab.idf.OntologyTerm group
    dbnp.transcriptomics.magetab.idf.OntologyTerm controlType
    dbnp.transcriptomics.magetab.idf.OntologyTerm databaseEntry

    static constraints ={
        group(nullable:true)
        controlType(nullable:true)
        databaseEntry(nullable:true)
    }
}
