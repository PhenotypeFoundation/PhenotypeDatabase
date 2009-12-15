package magetab.idf

class Protocol {

    String name
    String description
    String software
    String hardware
    OntologyTerm type
    String parameters
    String contact
    String term_source_ref

    static hasMany = [
        parameters: Parameter
    ]

    static constraints = {
        type(nullable: true,blank:true)
        name(nullable:true,blank:true)
        description(nullable:true,blank:true)
        software(nullable:true,blank:true)
        hardware(nullable:true,blank:true)
        parameters(nullable:true,blank:true)
        contact(nullable:true,blank:true)
        term_source_ref(nullable:true,blank:true)
    }
}
