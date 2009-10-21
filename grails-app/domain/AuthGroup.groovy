/**
 * Authentorization / Authentication Group Domain Class
 * @Author  Jeroen Wesbeek
 * @Since   20091020
 * @package Authorization
 * @see	    Authorization.groovy
 * @see	    BaseController.groovy
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class AuthGroup {
    // define many to many relationship with self
    static hasMany = [ groups: AuthGroup, users: AuthUser ];

    String  name;
    String  description;

    //static constraints = {}
}
