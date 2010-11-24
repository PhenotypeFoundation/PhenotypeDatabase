package dbnp.rest.common

import grails.converters.JSON
import java.net.URLEncoder
import org.codehaus.groovy.grails.web.json.*


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


    /* this should moved somewhere else */
    /* The static URLs are set in grails-app/conf/Config.groovy */ 
    def        static Encoding      = "UTF-8" 
    def public static ServerURL     = "http://localhost:8182/sam"
    def public static SAMServerURL  = "http://localhost:8182/sam"
    def public static GSCFServerURL = "http://localhost:8080/gscf"
    def public static DSPServerURL  = "http://localhost:8080/ncdsp"



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
		params['consumer']=ServerURL
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

    public static addRestWrapper( serverURL, restName, params = [], closure = { return it } ) {
		if(!serverURL) { throw new Exception("addRestWrapper: REST serverURL is null") }
		def result
		try {
			CommunicationManager.metaClass.registerStaticMethod( restName ) { Object [] strangeGroovyArgs ->
				def map = [:]
			    def args = strangeGroovyArgs[0]        // groovy nests the parameters of the methods in some other array
				if(params.size > 0 )
				{
					for( i in 0..(params.size-1) ) {
						def param = params[i]
				   	 	map[param] = args[i]
					}
				}
				result = closure( getRestResource( serverURL, restName, map ) )
			} 
		} catch ( Exception e ) { 
			throw new Exception("addRestWrapper: error. Could not retrieve data from RESTFful service. ") 
		}

		return result
	}


    /**
     *  This method creates on run time new methods for accessing Grails views that SAM provides for GSCF.
     *  This method should be called in grails-app/conf/BootStrap.groovy in the GSCF module.
     */          
    public static registerRestWrapperMethodsFromSAM() {
		def url = SAMServerURL
		addRestWrapper( url+'/rest', 'getQueryResult',  ['query'] )
    }



    /**
     * Give list of missing parameters for a parameter call in a RestController.
     *  
     * @params params Map params     	The parameter list required by this view.
     * @params requiredParamers 		List of parameter names that must be provided
     * @return true, if params has all required parameters, false otherwise 
     */  
	static String hasValidParams( params, Object [] requiredParams ) {
		requiredParams.every { params[it] }
	}


}
