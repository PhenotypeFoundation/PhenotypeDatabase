/**
 * AuthenticationService
 * 
 * Is used for keeping track of the logged in user
 *
 * @author      robert@isdat.nl (Robert Horlings
 * @since	20101021
 * @package	dbnp.authentication
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.authentication

import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser

class AuthenticationService {
    def springSecurityService
	def remoteAuthenticationService

    static transactional = true

    public boolean isLoggedIn() {
        return springSecurityService.isLoggedIn();
    }

    public SecUser getLoggedInUser() {
      def principal = springSecurityService.getPrincipal()

      // If the user is logged in, the principal should be a GrailsUser object.
      // If the user is not logged in, the principal is the 'anonymous username'
      // i.e. a string
      if( principal instanceof GrailsUser ) {
          return SecUser.findByUsername( principal.username );
      }

      return null;
    }

    /**
     * Logs a user in for a remote session
     */
    public boolean logInRemotely( String consumer, String token, SecUser user ) {
		remoteAuthenticationService.logInRemotely( consumer, token, user );
    }
    
    public boolean logOffRemotely( String consumer, String token ) {
		remoteAuthenticationService.logOffRemotely( consumer, token );
    }

    /**
     * Checks whether a user is logged in from a remote consumer with the
     * given token
     */
    public boolean isRemotelyLoggedIn( String consumer, String token ) {
		remoteAuthenticationService.isRemotelyLoggedIn( consumer, token );
    }

    /**
     * Returns the user that is logged in remotely
     */
    public SecUser getRemotelyLoggedInUser( String consumer, String token ) {
		remoteAuthenticationService.getRemotelyLoggedInUser( consumer, token );
    }
	
	/**
	 * Remove all remote sessions for a user
	 * @param user
	 */
	public void deleteRemoteSessions( SecUser user ) {
		remoteAuthenticationService.deleteRemoteSessions( user );
	}
}
