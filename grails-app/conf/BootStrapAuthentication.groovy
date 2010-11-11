import dbnp.authentication.*

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
		"setting up default authentication".grom()

		def adminRole = SecRole.findByAuthority('ROLE_ADMIN') ?: new SecRole(authority: 'ROLE_ADMIN').save()
		def user = SecUser.findByUsername('user') ?: new SecUser(
			username: 'user',
			password: springSecurityService.encodePassword('useR123!', 'user'),
			email: 'user@dbnp.org',
			userConfirmed: true, adminConfirmed: true).save(failOnError: true)
		def userTest = SecUser.findByUsername('test') ?: new SecUser(
			username: 'test',
			password: springSecurityService.encodePassword('useR123!', 'test'),
			email: 'test@dbnp.org',
			userConfirmed: true, adminConfirmed: true).save(failOnError: true)
		def userAdmin = SecUser.findByUsername('admin') ?: new SecUser(
			username: 'admin',
			password: springSecurityService.encodePassword('admiN123!', 'admin'),
			email: 'admin@dbnp.org',
			userConfirmed: true, adminConfirmed: true).save(failOnError: true)

		// Make the admin user an administrator
		SecUserSecRole.create userAdmin, adminRole, true
	}
}