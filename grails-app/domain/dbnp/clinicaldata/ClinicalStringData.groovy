package dbnp.clinicaldata

/**
 * This class describes the table which actually holds all the clinical data of the string type
 */
class ClinicalStringData {

	long assayID
	long sampleID
	ClinicalMeasurement measurement
	float value

    static constraints = {
    }
}
