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

import grails.plugin.springsecurity.userdetails.GrailsUser
import org.codehaus.groovy.grails.commons.GrailsApplication

class AuthenticationService {
    def springSecurityService
    def remoteAuthenticationService
    GrailsApplication grailsApplication

    static transactional = true

    public boolean isLoggedIn() {
        log.trace "isLoggedIn"
        return springSecurityService.isLoggedIn();
    }

    public SecUser getLoggedInUser() {
        log.debug "getLoggedInUser: getting principal"
        def principal = springSecurityService.getPrincipal()

        // If the user is logged in, the principal should be a GrailsUser object.
        // If the user is not logged in, the principal is the 'anonymous username'
        // i.e. a string
        if( principal instanceof GrailsUser ) {
            log.debug "retrieving SecUser for the user that is logged in"
            return SecUser.where { username == principal.username }.find();
        }

        return null;
    }

    /**
     * Returns a session token to be used in communication with the modules
     * If a session token already exists, it will be extended and reused
     */
    public String getRemoteSessionToken( String consumer, SecUser user ) {
        log.trace "getRemoteSessionToken"
        remoteAuthenticationService.getRemoteSessionToken(consumer, user);
    }
    
    /**
     * Logs a user in for a remote session
     */
    public boolean logInRemotely( String consumer, String token, SecUser user ) {
        log.trace "logInRemotely"
        remoteAuthenticationService.logInRemotely( consumer, token, user );
    }

    /**
     * Logs a user off in a remote session
     */
    public boolean logOffRemotely( String consumer, String token ) {
        log.trace "logOffRemotely"
        remoteAuthenticationService.logOffRemotely( consumer, token );
    }

    /**
     * Checks whether a user is logged in from a remote consumer with the
     * given token
     */
    public boolean isRemotelyLoggedIn( String consumer, String token ) {
        log.trace "isRemotelyLoggedIn"
        remoteAuthenticationService.isRemotelyLoggedIn( consumer, token );
    }

    /**
     * Returns the user that is logged in remotely
     */
    public SecUser getRemotelyLoggedInUser( String consumer, String token ) {
        log.trace "getRemotelyLoggedInUser"
        remoteAuthenticationService.getRemotelyLoggedInUser( consumer, token );
    }

    /**
     * Remove all remote sessions for a user
     * @param user
     */
    public void deleteRemoteSessions( SecUser user ) {
        log.trace "deleteRemoteSessions"
        remoteAuthenticationService.deleteRemoteSessions( user );
    }

    /**
     * Gets the email addresses to send template change requests to.
     * If not overridden in the configuration (grails.mail.overrideTemplateRequestAddresses),
     * these will be the addresses of all users that have the template admin role,
     * or if there are none, the users that have the admin role.
     */
    public getTemplateAdminEmails() {
        // Check if we're overriding the template request addresses
        def overrideAddresses = grailsApplication.config.grails.mail.overrideTemplateRequestAddresses
        if (overrideAddresses) {
            return overrideAddresses.split(",").collect({s -> s.trim()})
        }

        // Not overriding, so find the template admin users
        def administrators = SecRole.findUsers( 'ROLE_TEMPLATEADMIN' )
        if (administrators.size() > 0) {
            return administrators.email.toArray()
        }
        else {
            // If there are no template admins, send the request to the admins
            return SecRole.findUsers( 'ROLE_ADMIN' ).email.toArray()
        }
    }

}
