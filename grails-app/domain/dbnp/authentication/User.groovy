/**
 * User Domain Class
 *
 * Represents a user that has ever logged in to the system. Data for the user
 * is filled from the oAuth provider
 *
 * @author  robert@isdat.nl
 * @since   20101015
 * @package dbnp.authentication
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.authentication

class User {
    String username
    int providedId

    static constraints = {
    }
}
