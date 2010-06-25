package dbnp.rest

import java.util.Map 
import java.util.List
import java.util.HashMap
import java.net.URLEncoder
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.*
import dbnp.studycapturing.TemplateFieldListItem
import dbnp.studycapturing.Template
import dbnp.data.CleanDataLayer
import dbnp.studycapturing.Study 
import dbnp.studycapturing.Assay




/**  CommunicationManager 
 *
 *   This class implements a REST client to fetch data from the Simple Assay Module (SAM).
 *   The communicatino manager provides methods for accessing each resources.
 *   Every REST resource corresponds to exactly one method in this class that makes
 *   the communication with the resource available. 
 *
 *   For instance, the getSearchable() method calls the getMeasurements resource of the SAM 
 *   by passing arguments to it and returning the result of the calling that resource. 
 */


class CommunicationManager implements CleanDataLayer {

    
    /** ServerULR contains a string that represents the URL of the 
     *  rest resources that this communication manager connects to.
     */ 

    //def static ServerURL = "http://localhost:8182/ClinicalChemistry"
    def static ServerURL = "http://nbx5.nugo.org/sam"
    def static RestServerURL = ServerURL + "/rest"
    def static Encoding = "UTF-8" 


    /* Methods implemented for CleanDataLayer */



    /**
     * Get the names of all quantitative features that are available for a certain assay
     * @param assayID the module internal ID for the assay
     * @return
     */
    public String[] getFeaturesQuantitative(long assayID) {
         return new String [20]
    }
   


    /**
     * Get the data for a quantitative feature for a certain assay for a certain set of samples
     * @param feature
     * @param assayID
     * @param sampleIDs
     * @return Map
     */
    public Map getDataQuantitative(String feature, long assayID, String[] sampleIDs) {
         return new HashMap() 
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
        def url = new URL( RestServerURL + "/" + getSearchable(keyword) )
        return  JSON.parse( url.newReader() )
    }



    /** Send a request for the REST resource to the server and deliver the 
     *  resulting JSON object. (This is just a convenience method.)
     *
     *  @param resource: the name of the resource including parameters
     *  @return JSON object
     */
    private Object request( String resource ) { 
        def url = new URL( RestServerURL + "/" + resource )
        return  JSON.parse( url.newReader() )
    }



    /** Send a request for the REST resource to SAM and deliver the 
     *  results for the Query controller.
     *
     *  @param  compound	a SAM compound, e.g., "ldl" or "weight"
     *  @param  value		a SAM value of a measurement, e.g. "20" (without unit, please)
     *  @param  opperator	a SAM operator, i.e., "", "=", "<", or ">"
     *  @return List of matching studies
     */
    public List<Study> getSAMStudies( String compound, String value, String opperator ) {
         return [] 
    }





    /* Methods for accessing URLs in SAM */




    /**
     * Convenience method for constructing URLs for SAM that need parameters.
     * Note that parameters are first convereted to strings by calling their toString() method
     * and then Encoded to protect special characters.
     *
     * @params String resource The name of the resource, e.g. importer/pages 
     * @params Map params      A Map of parmater names and values., e.g. ['externalAssayID':12]
     * @return String url   
     */
    private URL getSAMURL( resource, params ) {
        def url = ServerURL + '/' + resource
		def first = true
		params.each { name, value ->
			if(first) {
				first = false
				url += '/nil?' + name + "=" + URLEncoder.encode( value.toString(), Encoding )
			}
			else { 
				url += '&' + name + "=" + URLEncoder.encode( value.toString(), Encoding  )
			}
		}
		return new URL( url )
    }


    /**
     * Get the URL for importing an assay from SAM. 
     * This is not a REST method! It only creates a rest resource and returns it's url.
     *
     * @params Study
     * @params Assay
     * @return URL 
     */
    public URL getAssayImportURL( study, assay ) {
		def params = ['externalAssayID':assay.externalAssayID, 'externalStudyID':study.code ] 
        return getSAMURL( 'importer/pages', params )
    }


    /**
     * Get the URL for showing an assay in SAM. 
     * This is not a REST method! It only creates a rest resource and returns it's url.
     *
     * @params Assay 
     * @return URL 
     */
    public URL getAssayShowURL( assay ) {
		def params = ['externalAssayID':assay.externalAssayID ] 
        return getSAMURL( 'simpleAssay/show', params )
    }


    /**
     * Get the URL for editing an assay in SAM. 
     * This is not a REST method! It only creates a rest resource and returns it's url.
     *
     * @params Assay 
     * @return URL 
     */
    public URL getAssayEditURL( assay ) {
		def params = ['externalAssayID':assay.externalAssayID ] 
        return getSAMURL( 'simpleAssay/edit', params )
    }


    /**
     * Get the URL for showing a measurement in SAM. 
     * This is not a REST method! It only creates a rest resource and returns it's url.
     *
     * @params study 
     * @return list of ClinicalFloatData
     */
    public URL getMeasurementTypesURL( study ) {
		def params = ['externalStudyID':study.code] 
        return getSAMURL( 'simpleAssayMeasurementType/list', params )
    }

}
