package dbnp.clinicaldata

class ClinicalAssayInstance {

	long externalAssayID // in the future, we might do a more sophisticated mapping to the study capture part, as multiple metadata instances might exist
	ClinicalAssay assay

    static constraints = {
    }
}
