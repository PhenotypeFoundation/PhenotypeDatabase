package dbnp.clinicaldata

/**
 * This class describes the table which actually holds all the clinical data of the float type
 */
class ClinicalFloatData {

	ClinicalAssayInstance assay
	ClinicalMeasurement measurement
	String sample  // universal sample ID
	float value

    static constraints = {
    }
}
