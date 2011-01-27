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

import nl.grails.plugins.gdt.*
import dbnp.studycapturing.*
import grails.converters.*

class ModuleCommunicationService implements Serializable {
    boolean transactional = false
	def authenticationService
	def moduleNotificationService
	
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
	def invalidateStudy( Study study ) {
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
	 */
	def callModuleRestMethodJSON( consumer, restUrl ) throws Exception {
		// create a random session token that will be used to allow to module to
		// sync with gscf prior to presenting the measurement data
		def sessionToken = UUID.randomUUID().toString()

		if (!authenticationService.isLoggedIn()) {
			// should not happen because we can only get here when a user is
			// logged in...
			throw new Exception('User is not logged in.')
		}

		// put the session token to work
		authenticationService.logInRemotely( consumer, sessionToken, authenticationService.getLoggedInUser() )

		// Append the sessionToken to the URL
		def url = restUrl
		if( restUrl.indexOf( '?' ) > 0 ) {
			// The url itself also has parameters
			url += '&sessionToken=' + sessionToken
		} else {
			// The url itself doesn't have parameters
			url += '?sessionToken=' + sessionToken
		}
		
		// Perform a call to the url
		def restResponse
		try {
			def textResponse = url.toURL().getText()
			restResponse = JSON.parse( textResponse )
		} catch (Exception e) {
			throw new Exception( "An error occurred while fetching " + url + ".", e )
		} finally {
			// Dispose of the ephemeral session token
			authenticationService.logOffRemotely(consumer, sessionToken)
		}
		
		return restResponse
	}
}
