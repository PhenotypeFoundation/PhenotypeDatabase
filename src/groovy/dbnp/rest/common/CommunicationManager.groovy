package dbnp.rest.common

import grails.converters.JSON
import java.net.URLEncoder
import org.codehaus.groovy.grails.web.json.*
import dbnp.studycapturing.Study

/**  CommunicationManager
 *
 *   @author Jahn
 *
 *   This class manages communication between dbNP modules such as GSCF and SAM.
 *   By communication we mean two ways of exchanging information: (1) via Rest resources, 
 *   and (2) via Grails views that a module can make available to another module.
 *
 *   For Rest communication this class implements a Rest client that fetches data 
 *   from other modules' Rest resources. The Rest implementation transfers data in JSON.
 *
 *   Note: Do not use this class directly to fetch data. Instead use your module's 
 *   rest wrapper methods. Use this module, to create these rest wrapper methods. 
 *   For instance, use dbnp.rest.sam.registerRestWrapperMethodsGSCFtoSAM to register new methods 
 *   for accessing GSCF's Rest service in SAM; your new method shoud then use this class.
 */


class CommunicationManager {

    def static Encoding     = "UTF-8" 
    def public static SAMServerURL = "localhost:8182/sam"
    def public static GSCFServerURL = "localhost:8080/gscf"

     

    /**
     * Get the results of provided by a rest Rest resource.
     *
     * @params String resource The name of the resource, e.g. importer/pages 
     * @params Map params      A Map of parmater names and values., e.g. ['externalAssayID':12]
     * @return String url   
     */
    public static Object getRestResource( RestServerURL, resource, params ) {
		def url = getRestURL( RestServerURL, resource, params )
		return  JSON.parse( url.newReader() )
    }


    /**
     * Convenience method for constructing URLs for SAM that need parameters.
     * Note that parameters are first convereted to strings by calling their toString() method
     * and then Encoded to protect special characters.
     *
     * @params String resource The name of the resource, e.g. importer/pages 
     * @params Map params      A Map of parmater names and values., e.g. ['externalAssayID':12]
     * @return String url   
     */
    public static URL getRestURL( RestServerURL, resource, params ) {
        def url = RestServerURL + '/' + resource
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
     * This method dynamically adds a static method to the CommunicationManager.
     *  
     * @params String serverURL		A rest server URL.
     * @params String restName		The name of a rest resource on the server.	
     * @params Map params      		A list of parameter names to be passed to this resource.
     * @return String url   
     *  
     * Given a rest resource at serverURL called resourceName, we register a static method
     * for the CommunicationManager. The new method has the same name and arity as the resource. 
     *  
     * Example: Suppopse http://localhost:8080/gscf/rest/getSamples is a Rest resource.
     *  
     * In our grails app, we would like to connect to this service. We want to have a 
     * method getSamples() that fetches the result from the service. We do this by calling
     *  
     * 		CommunicationManager.addRestWrapper( 'http://localhost:8080/gscf/rest', 'getSamples', ['externalStudyID'] ) 
     *  
     * This registers a new method:
     *  
	 *		 public static Object CommunicationManager.getSamples( Object arg )
     *  
     * This method has arrity 1 and expects to be given a map. The map is the parameter map
     * of the rest service getSamples. It maps parameter called "externalStudyID" to some object 
     * that is passed. So, it can be called like as follows:
     *  
     *      def sampleList = CommunicationManager.getSamples( [externalStudyID:4711] )
     *  
     *  The call will deliver the results of the parameterized rest resource given at:
     *  
     *  	http://localhost:8080/gscf/rest/nil?externalStudyID=4711
     * 
     */

    public static addRestWrapper( serverURL, restName, params = [] ) {
		CommunicationManager.metaClass.registerStaticMethod( restName ) { Object [] strangeGroovyArgs ->
			def map = [:]
		    def args = strangeGroovyArgs[0]        // groovy nests the parameters of the methods in some other array
			for( i in 0..(params.size-1) ) {
				def param = params[i]
			    map[param] = args[i]
			}
			return getRestResource( serverURL, restName, map )
		}
    }



    /**
     * This method dynamically registers a static method to the CommunicationManager. The new method 
     * gives url for a Grails view on some server and takes as arguments the arguments required
     * as params by the view.
     *  
     * @params String methodname	The name for method to be registered. 
     * @params String serverURL		The server's URL.
     * @params String viewName		The view's name, e.g., '/Assay/show'
     * @params Map params      		The parameter list required by this view.
     * @return String URL 
     *  
     */  
    public static addViewWrapper( methodName, serverURL, viewName, params = [] ) {

		CommunicationManager.metaClass.registerStaticMethod( methodName ) { Object [] strangeGroovyArgs ->
			def map = [:]
		    def args = strangeGroovyArgs[0]        // groovy nests the parameters of the methods in some other array
			for( i in 0..(params.size-1) ) {
				def param = params[i]
			    map[param] = args[i]
			}
			return getRestURL( serverURL, viewName, map )
		}
    }


    /**
     *  This creates on run time new methods for accessing Rest resources that GSCF provides for SAM.
     *  This method should be called in grails-app/conf/BootStrap.groovy in the SAM module.
     */ 
    public static registerRestWrapperMethodsGSCFtoSAM() {
    	def url = GSCFServerURL
		addRestWrapper( url , 'getStudies' )
		addRestWrapper( url , 'getSubjects', ['externalStudyID'] )
		addRestWrapper( url , 'getAssays',   ['externalStudyID'] )
		addRestWrapper( url , 'getSamples',  ['externalAssayID'] )
    }


    /**
     *  This method creates on run time new methods for accessing Grails views that SAM provides for GSCF.
     *  This method should be called in grails-app/conf/BootStrap.groovy in the GSCF module.
     */ 
    public static registerRestWrapperMethodsSAMtoGSCF() {
		def url = SAMServerURL 

		// register method that links to the SAM view for importing a SimpleAssay. 
        // parameters: externalAssayID, an externalAssayID 
		addViewWrapper( 'getAssayImportURL', url, 'importer/pages', ['externalAssayID', 'externalStudyID'] )

		// register method that links to the SAM view for showing a SimpleAssay 
        // parameters: externalAssayID
		addViewWrapper( 'getAssayShowURL', url, 'simpleAssay/show', ['externalAssayID'] )

   		// register method that links to the SAM view for editing a SimpleAssay 
        // parameters: externalAssayID
		addViewWrapper( 'getAssayEditURL', url, 'simpleAssay/show', ['externalAssayID'] )

   		// register method that links to the SAM view for editing a SimpleAssay 
        // parameters: externalAssayID
		addViewWrapper( 'getMeasurementTypesURL', url, 'simpleAssayMeasurementType/list', ['externalStudyID'] )
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


}
