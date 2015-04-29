package dbnp.configuration
import dbnp.authentication.*
import grails.util.Holders

/**
 * @Author Jeroen Wesbeek <work@osx.eu>
 * @Since 20101111
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class BootStrapAuthentication {
	/**
	 * set up the initial roles and users if required
	 * @visibility  public
	 * @void
	 */
	public static void initDefaultAuthentication(springSecurityService) {
		// Grom a development message
		if (String.metaClass.getMetaMethod("grom")) "setting up default authentication".grom()

		// user work variable
		def user=null

		// get configuration
		def config = Holders.config

		// create the admin role
		def adminRole = SecRole.findByAuthority('ROLE_ADMIN') ?: new SecRole(authority: 'ROLE_ADMIN').save(flush:true, failOnError:true)

		// create the client role
		def clientRole = SecRole.findByAuthority('ROLE_CLIENT') ?: new SecRole(authority: 'ROLE_CLIENT').save(flush:true, failOnError:true)

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
					apiKey:values.apikey ?: '',
					userConfirmed: true,
					adminConfirmed: true
				).save(flush:true, failOnError: true)

				if (String.metaClass.getMetaMethod("grom")) "adding user ${values.username}".grom()

				// is this user an administrator?
				if (values.administrator == 'true') { SecUserSecRole.create(user, adminRole, true) }

				// is this user a client/api user?
				if (values.client == 'true') { SecUserSecRole.create(user, clientRole, true) }
			}
		}
	}
}
