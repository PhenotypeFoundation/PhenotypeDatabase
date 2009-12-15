package magetab.idf

class InvestigationDesign {

    String title
    String experimentDescription
    String dateOfExperiment
    String publicReleaseDate

    static hasMany = [
        experimentalFactors: Factor,
        protocols: Protocol,
        publications: Publication,
        //sdrfs: magetab.sdrf.SampleAndDataRelationship,
        userDefinedAttributes: UserDefinedAttribute,
        contacts: Person,
        normalizationTypes: OntologyTerm,
        qualityControlTypes: OntologyTerm,
        replicateTypes: OntologyTerm,
        experimentalDesigns: OntologyTerm
//        termSources: magetab.adf.TermSource
    ]

    static constraints = {
        title(nullable:true,blank:true)
        experimentDescription(nullable:true,blank:true)
        dateOfExperiment(nullable:true,blank:true)
        publicReleaseDate(nullable:true,blank:true)
    }
}
