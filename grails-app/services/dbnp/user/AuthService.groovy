/**
 * AuthService Service
 * 
 * Checks whether a user is logged in (see also nl.metabolomicscentre.dsp.aaa.AuthService)
 *
 * @author  keesvb
 * @since	20100823
 * @package	dbnp.user
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.user

// Shiro
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.UsernamePasswordToken


class AuthService {

	static transactional = true

	def authUser(username = "", password = "") {
		println "authenticating ${username} with password ${password}"
		if (!username || !password){
			return false // required information missing to authenticate
		}

		def currentUser = SecurityUtils.getSubject()
		def token = new UsernamePasswordToken(username, password);

		try {
			currentUser.login( token );
		} catch ( Exception e ) {
			def foundUser = User.findByUsernameAndPasswordHash(username, password)
			if (foundUser) {
				return foundUser
			}
			else {
				return false //username wasn't in the system, show them an error message?
			}
		}

		//return UserBase.get(currentUser.getPrincipal())
		return dbnp.user.User.get(currentUser.getPrincipal())
	}
}