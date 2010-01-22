package dbnp.clinicaldata

class ClinicalDataLayerService implements dbnp.data.CleanDataLayer {

	boolean transactional = false

	String getAssayDescription(long assayID) {
		return "";
	}

	String[] getFeatureNames(long assayID) {
		return new String[0];
	}

	Map getFeatureData(long assayID, long[] sampleIDs) {
		return null;
	}

	Map getFeatureDataDifferential(long assayID, long[] sampleIDs1, long[] sampleIDs2) {
		return null;
	}
}
