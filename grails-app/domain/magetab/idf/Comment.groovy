package magetab.idf

class Comment {

    String arrayExpressReleaseDate
    String secondaryAccession
    String arrayExpressAccession
    String aemiameScore
    String timestamp_version

    static constraints = {
        arrayExpressReleaseDate(nullable:true,blank:true)
        secondaryAccession(nullable:true,blank:true)
        arrayExpressAccession(nullable:true,blank:true)
        aemiameScore(nullable:true,blank:true)
        timestamp_version(nullable:true,blank:true)
    }
}
