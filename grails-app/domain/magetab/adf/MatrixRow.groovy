package magetab.adf

class MatrixRow {


    Integer rowNumber
    String[] rowData
    MatrixRowId matrixRowId

    static constraints = {
        matrixRowId(true)
    }
}
