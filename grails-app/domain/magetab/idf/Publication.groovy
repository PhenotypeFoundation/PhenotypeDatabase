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
        status(nullable: true)
    }
}
