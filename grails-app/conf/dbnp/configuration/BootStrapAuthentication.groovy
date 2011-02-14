package dbnp.configuration
import dbnp.authentication.*
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * @Author Jeroen Wesbeek <work@osx.eu>
 * @Since 20101111
 *
 * Revision information:
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
 */
class BootStrapAuthentication {
	/**
	 * set up the initial roles and users if required
	 * @visibility  public
	 * @void
	 */
	public static void initDefaultAuthentication(springSecurityService) {
		"setting up default authentication".grom()

		// user work variable
		def user=null

		// get configuration
		def config = ConfigurationHolder.config

		// create the admin role
		def adminRole = SecRole.findByAuthority('ROLE_ADMIN') ?: new SecRole(authority: 'ROLE_ADMIN').save()

		// iterate through default users, see
		//	- grails-app/conf/config-environment.properties
		//	- ~/.grails-config/environment-gscf.properties
		config.authentication.users.each { key, values ->
			// make sure we do not add duplicate users
			if (!SecUser.findAllByUsername(values.username)) {
				// create user instance
				user = new SecUser(
					username:values.username,
					password:springSecurityService.encodePassword( values.password , values.username ),
					email:values.email,
					userConfirmed: true,
					adminConfirmed: true
				).save(failOnError: true)

				// is this user an administrator?
				if (values.administrator == 'true') {
					SecUserSecRole.create(user, adminRole, true)
				}
			}
		}
	}
}