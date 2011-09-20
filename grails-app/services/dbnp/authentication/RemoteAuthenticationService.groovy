/**
 * RemoteAuthenticationService Service
 * 
 * Description of my service
 *
 * @author  your email (+name?)
 * @since	2010mmdd
 * @package	???
 *
 * Revision information:
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
 */
package dbnp.authentication

import java.util.Date;

class RemoteAuthenticationService implements Serializable {
	static final int expiryTime = 12 * 60; // Number of minutes a remotely logged in user remains active

	static transactional = false

	/**
	 * Logs a user in for a remote session
	 */
	public boolean logInRemotely( String consumer, String token, SecUser user, Integer seconds = null ) {
		// Remove expired users, otherwise they will be kept in the database forever
		removeExpiredTokens()

		// Make sure there is no other logged in user anymore
		logOffRemotely( consumer, token )

		def SAUser = new SessionAuthenticatedUser( consumer: consumer, token: token, secUser: user, expiryDate: createExpiryDate( seconds ) )

		return SAUser.save()
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
	 * Remove all remote sessions for a user
	 * @param user
	 */
	public void deleteRemoteSessions( SecUser user ) {
		if( user ) {
			SessionAuthenticatedUser.executeUpdate("delete SessionAuthenticatedUser u where u.secUser = :secUser", [ secUser: user ])
		}
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
	protected Date createExpiryDate( Integer seconds = null) {
		// Compute expiryDate
		long now = new Date().getTime();

		if( seconds == null )
			seconds = RemoteAuthenticationService.expiryTime * 60

		return new Date( now + seconds * 1000 );

	}

	/**
	 * Resets the expiry date of the given user. This should be called every time
	 * an action occurs with this user. That way, if (in case of a timeout of 60 minutes)
	 * he logs in and returns 50 minutes later, he will keep a timeout value of
	 * 60 minutes, instead of only 10 minutes.
	 */
	protected boolean updateExpiryDate( SessionAuthenticatedUser user ) {
		user.expiryDate = createExpiryDate()
		user.save()
		user.refresh()
	}

}
