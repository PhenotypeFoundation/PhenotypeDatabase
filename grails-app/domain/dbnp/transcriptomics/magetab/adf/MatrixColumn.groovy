package dbnp.transcriptomics.magetab.adf

class MatrixColumn {

    int columnNumber
    String columnData
    dbnp.transcriptomics.magetab.sdrf.Node columnHeader
    dbnp.transcriptomics.magetab.idf.OntologyTerm quantitationType

    static constraints = {
        quantitationType(nullable:true)
    }
}
