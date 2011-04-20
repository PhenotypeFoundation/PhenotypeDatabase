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
    static final int expiryTime = 12 * 60; // Number of minutes a remotely logged in user remains active

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
		// Remove expired users, otherwise they will be kept in the database forever
		removeExpiredTokens()

		// Make sure there is no other logged in user anymore
        logOffRemotely( consumer, token )

        def SAUser = new SessionAuthenticatedUser( consumer: consumer, token: token, secUser: user, expiryDate: createExpiryDate() )

        return SAUser.save(flush: true)
    }
    
    public boolean logOffRemotely( String consumer, String token ) {
        def user = getSessionAuthenticatedUser(consumer, token)

        if( user ) {
            user.refresh()
            user.delete()
        }
        
        return true
    }

    /**
     * Checks whether a user is logged in from a remote consumer with the
     * given token
     */
    public boolean isRemotelyLoggedIn( String consumer, String token ) {
        // Check whether a user exists
        def user = getSessionAuthenticatedUser(consumer, token)

        // Check whether the user is logged in. Since we don't want to return a
        // user, we explicitly return true or false
        if( user ) {
			// The expiry date should be reset
			updateExpiryDate( user )

            return true
		} else {
            return false
		}
    }

    /**
     * Returns the user that is logged in remotely
     */
    public SecUser getRemotelyLoggedInUser( String consumer, String token ) {
        // Check whether a user exists
        def user = getSessionAuthenticatedUser(consumer, token)

        return user ? user.secUser : null
    }

    /**
     * Removes all tokens for remote logins that have expired
     */
    protected boolean removeExpiredTokens() {
        SessionAuthenticatedUser.executeUpdate("delete SessionAuthenticatedUser u where u.expiryDate < :expiryDate", [ expiryDate: new Date() ])
    }

    /**
	 * Returns the currently logged in user from the database or null if no user is logged in
	 */
	protected SessionAuthenticatedUser getSessionAuthenticatedUser( String consumer, String token ) {
        def c = SessionAuthenticatedUser.createCriteria()
        def result = c.get {
                and {
                        eq( "consumer", consumer)
                        eq( "token", token)
                        gt( "expiryDate", new Date())
                }
        }

        if( result )
            return result
        else
            return null
    }

	/**
	 * Returns the expiry date for a user that is active now.
	 */
	protected Date createExpiryDate() {
		// Compute expiryDate
		long now = new Date().getTime();
		return new Date( now + AuthenticationService.expiryTime * 60 * 1000 );

	}

	/**
	 * Resets the expiry date of the given user. This should be called every time
	 * an action occurs with this user. That way, if (in case of a timeout of 60 minutes)
	 * he logs in and returns 50 minutes later, he will keep a timeout value of
	 * 60 minutes, instead of only 10 minutes.
	 */
	protected boolean updateExpiryDate( SessionAuthenticatedUser user ) {
		user.expiryDate = createExpiryDate()
		return user.save( flush: true )
	}
}
