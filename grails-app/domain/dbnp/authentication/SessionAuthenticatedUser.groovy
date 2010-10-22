/**
 * SessionAuthenticatedUser Domain Class
 *
 * This class represents a user that has logged in from another module, using
 * the session id
 *
 * @author      Robert Horlings (robert@isdat.nl)
 * @since	20101022
 * @package	dbnp.authentication
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.authentication

class SessionAuthenticatedUser {
    String  consumer
    String  token
    Date    expiryDate
    SecUser secUser

    static constraints = {
        token: unique: true
    }

}
