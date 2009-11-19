package magetab.adf

class MatrixRow {


    int rowNumber
    String rowData
    MatrixRowId matrixRowId

    static constraints = {
        matrixRowId(nullable:true)
    }
}
