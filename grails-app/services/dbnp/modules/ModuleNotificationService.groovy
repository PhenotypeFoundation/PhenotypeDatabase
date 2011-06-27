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
import org.dbnp.gdt.*
import org.springframework.web.context.request.RequestContextHolder

class ModuleNotificationService implements Serializable {
	def remoteAuthenticationService
	
    static transactional = false
	
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
		if( !study )
			return
			
		log.info( "Invalidate " + study )

		def modules = AssayModule.findAllByNotify(true);
		
		// If no modules are set to notify, return
		if( !modules )
			return
			
			
		// Try to see which user is logged in. If no user is logged in (or no http session exists yet)
		// we send no authentication parameters
		def user
		try {
			user = RequestContextHolder.currentRequestAttributes().getSession().gscfUser
			
			if( !user )
				throw new Exception( "No user is logged in" );
				
		} catch( Exception e ) {
			log.warn "Sending study change notification without authentication, because an exception occurred: " + e.getMessage();
		}
		
		def urls = []
		modules.each { module ->
			// create a random session token that will be used to allow to module to
			// sync with gscf prior to presenting the measurement data
			def sessionToken = UUID.randomUUID().toString()
			def consumer = module.url
			
			// Create a URL to call
			def authenticationParameters = "";
			if( user ) {
				// put the session token to work (for 15 minutes)
				remoteAuthenticationService.logInRemotely( consumer, sessionToken, user, 15 * 60 )

				authenticationParameters = "consumer=" + consumer + "&sessionToken=" + sessionToken;
			}
				
			urls << module.url + '/rest/notifyStudyChange?studyToken=' + study.giveUUID() + ( authenticationParameters ? "&" + authenticationParameters : "" )
		}
		
		// Notify the module in a separate thread, so the user doesn't have to wait for it
		Thread.start { 
			urls.each { url ->
				log.info( "GSCF NOTIFY MODULE OF STUDY CHANGE: ${url}")
				try {
					def connection = url.toURL().openConnection()
					if( connection.responseCode == 200 ) {
						log.info( "GSCF NOTIFY-CALL SUCCEEDED: ${url}" )
					} else {
						log.info( "GSCF NOTIFY-CALL FAILED: ${url}: " + connection.responseCode )
					}
				} catch( Exception ignore) {
					log.info( "GSCF NOTIFY-CALL ERROR: ${url} - " + ignore.getMessage() )
				}
			}
		 };
    }
}
