package dbnp.clinicaldata

class ClinicalAssay {

	String name
	String reference
	boolean approved
	String appliedMethod
	String SOP

	static hasMany = [
	        measurements: ClinicalMeasurement
	]

	static constraints = {
	}
}
