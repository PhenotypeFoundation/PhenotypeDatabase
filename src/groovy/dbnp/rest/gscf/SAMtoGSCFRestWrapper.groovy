package dbnp.rest.gscf

import dbnp.rest.common.CommunicationManager
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




/**  SAMtoGSCFRestWrapper 
 *
 *   @author Jahn
 *
 *   This class defines methods for accessing (1) Rest resources provided by the Simple Assay Module (SAM), and
 *   (2) URL that SAM exposes to be rendered by the GSCF. 
.*   Each method corresponds to exactly one Rest resoruce on the GSCF side and acts as a wrapper 
 *   around the dbnp.rest.common.CommunicationManager. The CommunicationManager actually connects to the GSCF.
 *   Use the CommunicationManager in order to add new wrapper methods.
 */



class SAMtoGSCFRestWrapper {
    
    /** ServerULR contains a string that represents the URL of the 
     *  rest resources that this communication manager connects to.
     */ 

    //def static ServerURL = "http://nbx5.nugo.org/sam"
    def static ServerURL = "http://localhost:8182/sam"
    def static RestServerURL = ServerURL + "/rest"


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
     * Get the URL for importing an assay from SAM. 
     * This is not a REST method! It only creates a rest resource and returns it's url.
     *
     * @params Study
     * @params Assay
     * @return URL 
     */
    public URL getAssayImportURL( study, assay ) {
		def params = ['externalAssayID':assay.externalAssayID, 'externalStudyID':study.code ] 
        return getRestURL( 'importer/pages', params )
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
        return getRestURL( 'simpleAssay/show', params )
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
        return getRestURL( 'simpleAssay/edit', params )
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
        return getRestURL( 'simpleAssayMeasurementType/list', params )
    }


    /**
     * Get the results of provided by a rest Rest resource.
     *
     * @params String resource The name of the resource, e.g. importer/pages 
     * @params Map params      A Map of parmater names and values., e.g. ['externalAssayID':12]
     * @return String url   
     */
    public static Object getRestURL( resource, params ) {
		def url = CommunicationManager.getRestURL( RestServerURL, resource, params )
		return  JSON.parse( url.newReader() )
    }

}
