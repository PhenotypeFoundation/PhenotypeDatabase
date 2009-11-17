package magetab.idf

class Person {

    String lastName
    String firstName
    String midInitials
    String email
    String phone
    String address
    String affiliation

    static hasMany = [
        roles: OntologyTerm
    ]

    static constraints = {
    }
}
