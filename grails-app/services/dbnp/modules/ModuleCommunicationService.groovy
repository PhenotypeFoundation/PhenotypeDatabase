/**
 * SynchronizationService Service
 * 
 * Description of my service
 *
 * @author  your email (+name?)
 * @since	2010mmdd
 * @package	???
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.modules

import dbnp.studycapturing.*
import dbnp.authentication.*
import grails.converters.*
import javax.servlet.http.HttpServletResponse
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.hibernate.*;

class ModuleCommunicationService implements Serializable {
	static transactional = false
	def authenticationService
	def moduleNotificationService
	SessionFactory sessionFactory
	
	/**
	 * Cache containing the contents of different URLs. These urls are
	 * saved per user, since the data could be different for different users.
	 */
	def cache = [:]

	/**
	 * Number of seconds to save the data in cache
	 */
	def numberOfSecondsInCache = ConfigurationHolder.config.modules.cacheDuration ? Integer.valueOf( ConfigurationHolder.config.modules.cacheDuration.toString() ) : 300;

	/**
	 * Sends a notification to assay modules that some part of a study has changed.
	 * 
	 * Only modules that have the notify flag set to true will be notified. They will be notified on the URL
	 * 
	 * [moduleUrl]/rest/notifyStudyChange?studyToken=abc
	 * 
	 * Errors that occur when calling this URL are ignored. The module itself is responsible of
	 * maintaining a synchronized state.
	 * 
	 * @param study
	 * @return
	 */
	def invalidateStudy( def study ) {
		moduleNotificationService.invalidateStudy( study );
	}

	/**
	 * Checks whether a specific method on a module is reachable and returns a SC_OK response code. 
	 * 
	 * This method will return false if a method returns an error (including 403 and 401 errors) or
	 * a redirect
	 * 
	 * @param moduleUrl		URL of the module
	 * @param path			Path of the rest method on that module. If omitted, the module reachablility itself is tested
	 * @return				True if the module is reachable, false otherwise
	 */
	def isModuleReachable(moduleUrl, path = "") {
		def connection = ( moduleUrl + path ).toURL().openConnection()
		try {
			return connection.responseCode == HttpServletResponse.SC_OK
		} catch(e) {
			return false
		}
	}

	/**
	 * Calls a rest method on a module
	 * 
	 * @param consumer	Consumer of that specific module
	 * @param restUrl	Full URL for the method to call
	 * @return			JSON 	JSON object of the parsed text
	 * @deprecated		Use callModuleMethod instead
	 */
	def callModuleRestMethodJSON( consumer, restUrl ) throws Exception {
		def parts = restUrl.split( /\?/ );
		def url = "";
		def query = "";
		
		if( parts.size() > 1 ) {
			url = parts[ 0 ]
			query = parts[ 1 ]
		} else if( parts.size() > 0 ) { 
			url = parts[ 0 ];
		}
		
		return callModuleMethod( consumer, url, query );
	}
	
	/**
	* Calls a rest method on a module
	*
	* @param consumer	Consumer of that specific module
	* @param restUrl	Full URL for the method to call, without query string
	* @param args		Query string for the url to call (e.q. token=abc&field=xyz)
	* @param requestMethod	GET or POST - HTTP request method to use
	* @return			JSON 	JSON object of the parsed text
	*/
	def callModuleMethod( String consumer, String restUrl, String args = null, String requestMethod = "GET" ) {
		if (!authenticationService.isLoggedIn()) {
			// should not happen because we can only get here when a user is
			// logged in...
			throw new Exception('User is not logged in.')
		}

		// Check whether the url is present in cache
		def cacheData = retrieveFromCache( restUrl, args );
		if( cacheData && cacheData[ "success" ] )
			return cacheData[ "contents" ];
		else if( cacheData && !cacheData[ "success" ] )
			throw new Exception( "Error while fetching data from " + restUrl + " (from cache): " + cacheData[ "error" ] )

		// create a random session token that will be used to allow to module to
		// sync with gscf prior to presenting the measurement data
		def sessionToken = UUID.randomUUID().toString()

		// put the session token to work
		authenticationService.logInRemotely( consumer, sessionToken, authenticationService.getLoggedInUser() )
		
		// Append the sessionToken to the URL
		if( !args )
			args = ""
		
		args += "&sessionToken=" + sessionToken
		
		// Perform a call to the url
		def restResponse
		try {
			log.trace "GSCF call (" + requestMethod + ") to " + consumer + " URL: " + restUrl + " (args: " + args + ")"

			def textResponse
			switch( requestMethod.toUpperCase() ) {
				case "GET":
					def url = restUrl + "?" + args;
					def connection = url.toURL().openConnection();
		
					textResponse = url.toURL().getText()
				
					break
				case "POST":
					def connection = restUrl.toURL().openConnection()
					connection.setRequestMethod( "POST" );
					connection.doOutput = true
					
					def writer = new OutputStreamWriter( connection.outputStream )
					writer.write( args );
					writer.flush()
					writer.close()
					
					connection.connect();
					
					textResponse = connection.content.text

					break
				default:
					throw new Exception( "Unknown request method given. Use GET or POST" )
			}

			log.trace "GSCF response: " + textResponse
			restResponse = JSON.parse( textResponse )
		} catch (Exception e) {
			storeErrorInCache( restUrl, e.getMessage(), args );
			throw new Exception( "An error occurred while fetching " + restUrl + ".", e )
		} finally {
			// Dispose of the ephemeral session token
			authenticationService.logOffRemotely(consumer, sessionToken)
		}

		// Store the response in cache
		storeInCache( restUrl, restResponse, args );
		
		return restResponse

	}
	
	/**
	 * Checks whether a specific url exists in cache
	 * @param url	URL to call
	 * @return		true if the url is present in cache
	 */
	def existsInCache( url, args = null ) {
		return retrieveFromCache( url, args ) == null;
	}

	/**
	 * Retrieves the contents of a specific URL from cache
	 * @param url	URL to call
	 * @return		JSON object with the contents of the URL or null if the url doesn't exist in cache
	 */
	def retrieveFromCache( url, args = null ) {
		def user = authenticationService.getLoggedInUser();
		def userId = user ? user.id : -1;
		
		url = cacheUrl( url, args )
		
		if( cache[ userId ] && cache[ userId ][ url ] && ( System.currentTimeMillis() - cache[ userId ][ url ][ "timestamp" ] ) < numberOfSecondsInCache * 1000 ) {
			return cache[ userId ][ url ];
		} else {
			return null;
		}
	}

	/**
	 * Store the retrieved contents from a url in cache
	 * @param url		URL that has been called
	 * @param contents	Contents of the URL
	 */
	def storeInCache( url, contents, args = null ) {
		def user = authenticationService.getLoggedInUser();
		def userId = user ? user.id : -1;

		if( !cache[ userId ] )
			cache[ userId ] = [:]

		cache[ userId ][ cacheUrl( url, args ) ] = [
			"timestamp": System.currentTimeMillis(),
			"success": true,
			"contents": contents
		];
	}
	
	/**
	* Store the retrieved error from a url in cache
	* @param url		URL that has been called
	* @param contents	Contents of the URL
	*/
   def storeErrorInCache( url, error, args = null ) {
	   def user = authenticationService.getLoggedInUser();
	   def userId = user ? user.id : -1;

	   if( !cache[ userId ] )
		   cache[ userId ] = [:]

	   cache[ userId ][ cacheUrl( url, args ) ] = [
		   "timestamp": System.currentTimeMillis(),
		   "success": false,
		   "error": error
	   ];
   }
   
   /**
    * Url used to save data in cache
    */
   def cacheUrl( url, args = null ) {
		if( args ) {
			// Remove sessionToken from args
			args = args;
			def sessionFound = ( args =~ /sessionToken=[^&]/ );
			args = sessionFound.replaceAll( "sessionToken=" );
			
			url += '?' + args
		}
			
		return url;
   }

}
