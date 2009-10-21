/**
 * Authentorization / Authentication User Domain Class
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
class AuthUser {
    // define many to many relationship with groups
    static belongsTo = AuthGroup;
    static hasMany = [ groups: AuthGroup ];

    String  username;
    String  password;
    String  firstname;
    String  lastname;
    String  email;
    
    //static constraints = {}
}
