package dbnp.clinicaldata

class ClinicalMeasurement
 extends dbnp.data.FeatureBase {

	ClinicalMeasurementType type
	String referenceValues
	float detectableLimit
	String correctionMethod
	boolean isDrug
	boolean isIntake
	boolean inSerum

	static constraints = {
	}
}
