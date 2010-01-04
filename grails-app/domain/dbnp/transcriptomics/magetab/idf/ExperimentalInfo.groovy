package dbnp.transcriptomics.magetab.idf

class ExperimentalInfo {

    String factor_name
    String factor_type
    String factor_term_source_ref
    String design
    String design_term_source_ref

    /*static hasMany = [
        factor_name:String,
        factor_type:String,
        design:String
        design_term_source_ref:String,
        factor_term_source_ref:String
    ]*/

    static constraints = {
        factor_name(nullable:true,blank:true)
        factor_type(nullable:true,blank:true)
        factor_term_source_ref(nullable:true,blank:true)
        design(nullable:true,blank:true)
        design_term_source_ref(nullable:true,blank:true)
    }
}
