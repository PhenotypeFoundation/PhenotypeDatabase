package magetab.adf

class MatrixColumn {

    Integer columnNumber
    String[] columnData
    Node columnHeader
    magetab.idf.OntologyTerm quantitationType

    static constrainst = {
        quantitationType(nullable:true)
    }
}
