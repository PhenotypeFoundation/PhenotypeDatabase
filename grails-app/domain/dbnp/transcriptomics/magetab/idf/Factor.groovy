package dbnp.transcriptomics.magetab.idf

class Factor {

    String name
    OntologyTerm type

    static constraints = {
        name(nullable:true,blank:true)
        type(nullable: true, blank:true)
    }
}
