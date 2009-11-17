package magetab.idf

class Protocol {

    String name
    String description
    String software
    String hardware
    OntologyTerm type

    static hasMany = [
        parameters: Parameter
    ]

    static constraints = {
        type(nullable: true)
    }
}
