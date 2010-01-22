package dbnp.clinicaldata

/**
 * This class describes the table which actually holds all the clinical data of the float type
 */
class ClinicalFloatData {

	long assayID   // external or internal assay ID? the latter approach seems better
	long sampleID  // same question
	ClinicalMeasurement measurement
	float value

    static constraints = {
    }
}
