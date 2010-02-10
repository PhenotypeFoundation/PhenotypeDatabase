package dbnp.clinicaldata

class ClinicalDataLayerService implements dbnp.data.CleanDataLayer {

	boolean transactional = false

	Map getDataQuantitative(String feature, long assayID, String[] sampleIDs) {
		def measurement = ClinicalMeasurement.findByName(feature)
		if (!measurement) throw new NoSuchFieldException("Feature ${feature} not found")
		measurement.getValues(assayID,sampleIDs)
	}

	String[] getFeaturesQuantitative(long assayID) {
		return ClinicalAssayInstance.get(assayID).assay.measurements*.name;
	}
}
