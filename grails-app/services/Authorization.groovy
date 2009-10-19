/**
 * Authentication and Authorization service
 * @Author  Jeroen Wesbeek
 * @Since   20091019
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
public class Authorization {
    /**
     * class constructor
     * @void
     */
    public def Authorization() {
	// debug line for now
	printf("instantiated %s\n",this.class.name);
    }

    /**
     * check if a user is authenticated to use this class and / or method
     * @return boolean
     */
    def isAuthorized(controller,action) {
	// logic should be implemented here containing:
	//	user / sessions
	//	user authorization per controller + action
	// another method should be able to list authorized actions?
	printf("calling isAuthorized(%s,%s)\n",controller,action);

	// everything is allowed for now...
	return true;
    }
}