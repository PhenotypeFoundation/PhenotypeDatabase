package dbnp.studycapturing

/**
 * Person class, describes persons that are somehow connected to the study
 */
class Person {

    String title
    String gender
    String lastName
    String prefix
    String firstName
    String initials
    String email
    String fax
    String phone
    String mobile
    String address

    static hasMany = [affiliations: PersonAffiliation]

    static constraints = {
        title(nullable:true,blank:true)
        gender(nullable:true,blank:true)
        firstName(nullable:true,blank:true)
        initials(nullable:true,blank:true)
        prefix(nullable:true,blank:true)
        lastName(nullable:true,blank:true)
        email(nullable:true,blank:true)
        fax(nullable:true,blank:true)
        phone(nullable:true,blank:true)
        address(nullable:true,blank:true)
        mobile(nullable:true,blank:true)
    }
}
