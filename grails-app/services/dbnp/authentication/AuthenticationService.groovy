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
    def SpringSecurityService

    boolean transactional = true

    protected boolean isLoggedIn() {
      def principal = SpringSecurityService.getPrincipal()

      // If the user is logged in, the principal should be a GrailsUser object.
      // If the user is not logged in, the principal is the 'anonymous username'
      // i.e. a string
      if( principal instanceof GrailsUser ) {
          return true;
      }

      return false;
    }

    protected SecUser getLoggedInUser() {
      def principal = SpringSecurityService.getPrincipal()

      // If the user is logged in, the principal should be a GrailsUser object.
      // If the user is not logged in, the principal is the 'anonymous username'
      // i.e. a string
      if( principal instanceof GrailsUser ) {
          return SecUser.findByUsername( principal.username );
      }

      return null;
    }
}
