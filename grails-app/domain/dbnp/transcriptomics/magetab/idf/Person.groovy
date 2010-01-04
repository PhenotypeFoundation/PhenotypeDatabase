package dbnp.transcriptomics.magetab.idf

class Person {

    String lastName
    String firstName
    String midInitials
    String email
    String fax
    String phone
    String address
    String affiliation
    String roles
    String roles_ref

    static hasMany = [
        roles: OntologyTerm
    ]

    static constraints = {
        lastName(nullable:true,blank:true)
        firstName(nullable:true,blank:true)
        midInitials(nullable:true,blank:true)
        email(nullable:true,blank:true)
        fax(nullable:true,blank:true)
        phone(nullable:true,blank:true)
        affiliation(nullable:true,blank:true)
        roles(nullable:true,blank:true)
        roles_ref(nullable:true,blank:true)
        address(nullable:true,blank:true)
    }
}
