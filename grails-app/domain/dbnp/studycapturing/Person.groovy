package dbnp.studycapturing

/**
 * Person class, describes persons that are somehow connected to the study
 */
class Person {

    String lastName
    String firstName
    String midInitials
    String email
    String fax
    String phone
    String address

    static hasMany = [roles : PersonRole, affiliations: PersonAffiliation]

    static constraints = {
        firstName(nullable:true,blank:true)
        midInitials(nullable:true,blank:true)
        email(nullable:true,blank:true)
        fax(nullable:true,blank:true)
        phone(nullable:true,blank:true)
        address(nullable:true,blank:true)
    }
}
