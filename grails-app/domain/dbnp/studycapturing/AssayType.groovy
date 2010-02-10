package dbnp.studycapturing

/**
 * Enum describing the different assay types (aka omics submodules).
 */
public enum AssayType {
    TRANSCRIPTOMICS('Transcriptomics'),
    METABOLOMICS('Metabolomics'),
    CLINICAL_DATA('Clinical data')

    String name

    AssayType(String name) {
     this.name = name
    }

    static list() {
     [TRANSCRIPTOMICS, METABOLOMICS, CLINICAL_DATA]
    }

    /*def String toString() {
        return this.name
    }*/

}