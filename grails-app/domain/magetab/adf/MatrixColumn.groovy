package magetab.adf

class MatrixColumn {

    int columnNumber
    String columnData
    magetab.sdrf.Node columnHeader
    magetab.idf.OntologyTerm quantitationType

    static constraints = {
        quantitationType(nullable:true)
    }
}
