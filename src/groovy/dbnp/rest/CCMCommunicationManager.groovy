package dbnp.rest

import java.util.Map; 
import java.util.HashMap; 
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.*
import dbnp.studycapturing.TemplateFieldListItem
import dbnp.studycapturing.Template
import dbnp.data.CleanDataLayer



class CCMCommunicationManager implements CleanDataLayer {

    //def static ServerURL = "http://localhost:8080/gscf/rest";
    def static ServerURL = "http://nbx5.nugo.org:8182/ClinicalChemistry/rest";


    /* Methods implemented for CleanDataLayer */


    /**
     * Get the names of all quantitative features that are available for a certain assay
     * @param assayID the module internal ID for the assay
     * @return
     */
    public String[] getFeaturesQuantitative(long assayID) {
         return new String [20];
    }
   


    /**
     * Get the data for a quantitative feature for a certain assay for a certain set of samples
     * @param feature
     * @param assayID
     * @param sampleIDs
     * @return Map
     */
    public Map getDataQuantitative(String feature, long assayID, String[] sampleIDs) {
         return new HashMap(); 
    }





    /**
     * Testing REST. Remove when connection to nbx5 is established.
     *
     * @return list of ClinicalFloatData
     */
    public Object getFeatures() {
        def url = new URL( ServerURL + "/features" )
        return  JSON.parse(url.newReader())
    }


    /**
     * Testing REST. Remove when connection to nbx5 is established.
     *
     * @return list of ClinicalFloatData
     */
    private String getSearchable( keyword ) {
        return "submit=Query&q=" + keyword 
    }


    /**
     * Testing REST. Remove when connection to nbx5 is established.
     *
     * @return list of ClinicalFloatData
     */
    public String getStudiesForKeyword( String keyword ) {
    }


    public void getMeasurementsResource() {
    }

    public void getMeasurementsForValueResource() {
    }

    public void getMeasurementsForRangeResource() {
    }


    public void getDataSimple() {
    }


}
