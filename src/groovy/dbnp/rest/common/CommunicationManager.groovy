package dbnp.rest.common

import grails.converters.JSON
import java.net.URLEncoder
import org.codehaus.groovy.grails.web.json.*




/**  CommunicationManager 
 *
 *   @author Jahn
 *
 *   This class implements a Rest client for fetching data from Rest resources provided by
 *   GSCF and SAM. This class provides general means needed for fetching the JSON data. 
 *   Do not use this class directly to fetch data. Instead use your module's RestWrapper. 
 *   For instance, use dbnp.rest.sam.GSCFtoSAMRestWrapper to define a new method for accessing
 *   GSCF's Rest service in SAM; your new method shoud then use this class.
 */


class CommunicationManager {

    def static Encoding = "UTF-8" 

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

}
