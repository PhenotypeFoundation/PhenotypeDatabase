package dbnp.rest

import java.util.Map 
import java.util.List
import java.util.HashMap
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.*
import dbnp.studycapturing.TemplateFieldListItem
import dbnp.studycapturing.Template
import dbnp.data.CleanDataLayer
import dbnp.studycapturing.Study 




/**  CCMCommunicationManager 
 *
 *   This class implements a REST client to fetch data from the Clinical Chemistry Module (CCM).
 *   The communicatino manager provides methods for accessing each resources.
 *   Every REST resource corresponds to exactly one method in this class that makes
 *   the communication with the resource available. 
 *
 *   For instance, the getSearchable() method calls the getMeasurements resource of the CCM
 *   by passing arguments to it and returning the result of the calling that resource. 
 */


class CCMCommunicationManager implements CleanDataLayer {

    
    /** ServerULR contains a string that represents the URL of the 
     *  rest resources that this communication manager connects to.
     */ 
    def static ServerURL = "http://nbx5.nugo.org:8182/ClinicalChemistry/rest";
    //def static ServerURL = "http://localhost:8080/gscf/rest";


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
    //    return  request( "features" ) 
        return  getStudiesForKeyword("ldl")
    }


    /**
     * For a string for the searchable plugin.
     * This works for one keyeword, but protection should be built in using 
     * the methods that searchable uses for building query strings. 
     *
     * @return list of ClinicalFloatData
     */
    private String getSearchable( keyword ) {
        return  "?submit=Query&q=" + keyword 
    }


    /**
     * Get all meassurements that contain a given keyword as feature.
     *
     * @param  keyword, the keyword used
     * @return list of ClinicalFloatData
     */
    public String getStudiesForKeyword( String keyword ) {
        def resource = "getMeasurementsForValue"
        request( resource + getSearchable(keyword) )
    }


    /**
     * Get all meassurements that contain a given keyword as feature.
     *
     * @param  keyword, the keyword used
     * @return list of ClinicalFloatData
     */
    public Object getMeasurementsResource( String keyword ) {
        def url = new URL( ServerURL + "/" + getSearchable(keyword) )
        return  JSON.parse( url.newReader() )
    }


    public void getMeasurementsForValueResource() {
    }


    public void getMeasurementsForRangeResource() {
    }


    public void getDataSimple() {
    }






    /** Send a request for the REST resource to the server and deliver the 
     *  resulting JSON object. (This is just a convenience method.)
     *
     *  @param resource: the name of the resource including parameters
     *  @return JSON object
     */
    private Object request( String resource ) { 
        def url = new URL( ServerURL + "/" + resource );
        return  JSON.parse( url.newReader() );
    }



    /** Send a request for the REST resource to SAM and deliver the 
     *  results for the Query controller.
     *
     *  @param  compound	a SAM compound, e.g., "ldl" or "weight"
     *  @param  value		a SAM value of a measurement, e.g. "20" (without unit, please)
     *  @param  opperator	a SAM operator, i.e., "=", "<", or ">"
     *  @return List of matching studies
     */
    public List<Study> getSAMStudies( String compound, String value, String opperator ) {
         return [] 
    }


}
