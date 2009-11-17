package magetab.idf

class InvestigationDesign {

    String title
    String experimentDescription
    Date dateOfExperiment
    Date publicReleaseDate

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
        experimentalDesigns: OntologyTerm,
        termSources: TermSource
    ]

    static constraints = {
    }
}
