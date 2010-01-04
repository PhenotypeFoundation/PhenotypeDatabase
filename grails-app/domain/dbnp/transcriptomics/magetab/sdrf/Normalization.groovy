package dbnp.transcriptomics.magetab.sdrf

class Normalization {

    String type;
    String term_source_ref;

    static constraints = {
        type(nullable:true,blank:true)
        term_source_ref(nullable:true,blank:true)
    }
}
