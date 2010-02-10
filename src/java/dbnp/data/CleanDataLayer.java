package dbnp.data;

import java.util.Map;

/**
 * Interface which the dbNP clean data modules should implement.
 */
public interface CleanDataLayer {

        /**
         * Get the names of all quantitative features that are available for a certain assay
         * @param assayID the module internal ID for the assay
         * @return
         */
        public String[] getFeaturesQuantitative(long assayID);

        /**
         * Get the data for a quantitative feature for a certain assay for a certain set of samples
         * @param feature
         * @param assayID
         * @param sampleIDs
         * @return
         */
        public Map getDataQuantitative(String feature, long assayID, String[] sampleIDs);

}
