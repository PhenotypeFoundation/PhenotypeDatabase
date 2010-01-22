package dbnp.data;

import java.util.Map;

/**
 * Interface which the dbNP clean data modules should implement.
 */
public interface CleanDataLayer {

        public String getAssayDescription(long assayID);
        public String[] getFeatureNames(long assayID);
        public Map getFeatureData(long assayID, long[] sampleIDs);
        public Map getFeatureDataDifferential(long assayID, long[] sampleIDs1, long[] sampleIDs2);

}
