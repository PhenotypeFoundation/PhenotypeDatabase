package dbnp.studycapturing

class Publication {

    String title
    String pubMedID
    String DOI
    String authorsList
    String comments

    static constraints = {
        pubMedID(nullable:true,blank:true)
        DOI(nullable:true,blank:true)
        authorsList(nullable:true,blank:true)
        comments(nullable:true,blank:true)
    }
}
