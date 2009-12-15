package magetab.idf

class OntologyTerm {

    String text
    String category
    String accessionNumber
//    magetab.adf.TermSource termSource


    static constraints = {
//        termSource(nullable: true)
        text(nullable:true,blank:true)
        category(nullable:true,blank:true)
        accessionNumber(nullable:true,blank:true)
    }
}
