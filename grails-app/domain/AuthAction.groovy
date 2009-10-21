/**
 * Authentorization / Authentication Action Domain Class
 * @Author  Jeroen Wesbeek
 * @Since   20091020
 * @package Authorization
 * @see	    Authorization.groovy
 * @see	    BaseController.groovy
 *
 * This domain class contains controllers and their actions, this allows user
 * role seperation on a functional level.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class AuthAction {
    // define many to many relationship with AuthRole
    static belongsTo = AuthRole;
    static hasMany = [ roles: AuthRole ];

    String  controller;
    String  action;
    
    //static constraints = {}
}
