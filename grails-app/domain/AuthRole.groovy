/**
 * Authentorization / Authentication Role Domain Class
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
class AuthRole {
    // define many to many relationship with AuthAction
    static  hasMany = [ actions: AuthAction ];
    
    String  name;
    String  description;
    
    //static constraints = {}
}
