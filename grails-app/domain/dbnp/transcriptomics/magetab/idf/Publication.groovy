package dbnp.transcriptomics.magetab.idf

class Publication {

    String title
    String pubMedID
    String DOI
    OntologyTerm status
    String status_term_source_ref
    String authors_list

    static hasMany = [
        authors: Person
    ]

    static constraints = {
        status(nullable:true,blank:true)
        title(nullable:true,blank:true)
        pubMedID(nullable:true,blank:true)
        DOI(nullable:true,blank:true)
        authors_list(nullable:true,blank:true)
        status_term_source_ref(nullable:true,blank:true)
    }
}
