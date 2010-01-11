package dbnp.transcriptomics.magetab.idf

class InvestigationDesign {

    String title
    String experimentDescription
    String dateOfExperiment
    String publicReleaseDate
    String sdrf_file;

    static mapping = {
       experimentDescription type: 'text'
    }

    static hasMany = [
        experimentalFactors: Factor,
        protocols: MAGEProtocol,
        publications: Publication,
        //sdrfs: dbnp.transcriptomics.magetab.sdrf.SampleAndDataRelationship,
        userDefinedAttributes: UserDefinedAttribute,
        contacts: Person,
        normalizationTypes: OntologyTerm,
        qualityControlTypes: OntologyTerm,
        replicateTypes: OntologyTerm,
        experimentalDesigns: OntologyTerm
//        termSources: dbnp.transcriptomics.magetab.adf.TermSource
    ]

    static constraints = {
        title(nullable:true,blank:true)
        experimentDescription(nullable:true,blank:true)
        dateOfExperiment(nullable:true,blank:true)
        publicReleaseDate(nullable:true,blank:true)
        sdrf_file(nullable:true,blank:true)
    }
}
