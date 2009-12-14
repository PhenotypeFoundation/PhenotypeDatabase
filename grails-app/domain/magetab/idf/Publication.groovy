package magetab.idf

class Publication {

    String title
    String pubMedID
    String DOI
    OntologyTerm status

    static hasMany = [
        authors: Person
    ]

    static constraints = {
        status(nullable:true,blank:true)
        title(nullable:true,blank:true)
        pubMedID(nullable:true,blank:true)
        DOI(nullable:true,blank:true)
    }
}
