package dbnp.clinicaldata

import dbnp.data.FeatureType

class ClinicalMeasurement
 extends dbnp.data.FeatureBase {

	// For now, let's assume that quantitative values are stored as float,
	// and qualitative values as string
	// We can always add an additional ClinicalMeasurementType datatype (FLOAT, INTEGER etc.) later (as was here until rev 186)

	String referenceValues
	float detectableLimit
	String correctionMethod
	boolean isDrug
	boolean isIntake
	boolean inSerum

	static constraints = {
		name(unique:true)
		referenceValues(nullable: true, blank: true)
		detectableLimit(nullable: true)
		correctionMethod(nullable: true, blank: true)
	}

	Map getValues(long assayID, String[] sampleIDs) {
		FeatureType featureType = type
		switch(featureType) {
			case FeatureType.QUANTITATIVE:
				def values = ClinicalFloatData.withCriteria {
					assay { eq("id",assayID) }
					eq("measurement",this)
					'in'("sample",sampleIDs)
				}
				def result = new HashMap<String,Float>();
				values.each {
					result.put(it.sample,it.value)
				}
				return result
		        default:
				throw new NoSuchFieldException("Feature type ${featureType} not supported")
		}
	}
}
