/**
 * IconTagLib Tag Library
 *
 * Description of my tab library
 *
 * @author  Jeroen Wesbeek
 * @since	20121009
 */
package dbnp.common

import dbnp.authentication.SecUser
import java.security.MessageDigest
import org.codehaus.groovy.grails.commons.GrailsApplication

class IconTagLib {
	// define the tag namespace (e.g.: <af:action ... />
	static namespace = "icon"

	// define the userIcon tag
	def userIcon = { attrs, body ->
		SecUser user = (attrs.containsKey('user') ? attrs.get('user') : null)
		Integer size = (attrs.containsKey('size') ? attrs.get('size') as Integer : 20)
		Boolean transparent = (attrs.containsKey('transparent') ? attrs.get('transparent') as Boolean : false)

		// calculate the md5 hash for this user's email address
		MessageDigest digest = MessageDigest.getInstance("MD5")
		String emailSum = new BigInteger(1,digest.digest(user.email.toLowerCase().trim().getBytes())).toString(16).padLeft(32,"0")

		// define baseURL
		String baseURL = resource(dir:'', absolute: true)
		Boolean https = baseURL =~ /https/

		// define image path when a gravatar icon does not exist
		String fallbackURL = resource(
				dir:'images/gravatar',
				file: (transparent) ? "unknown.gif" : sprintf("unknown-%d.jpg", size),
				absolute: true
		)

		// define the gravatar icon url
		String URL = sprintf(
				"http%s://www.gravatar.com/avatar/%s?s=%d&d=%s",
				((https) ? "s" : ""),
				emailSum,
				size,
				java.net.URLEncoder.encode(fallbackURL)
		)

		// on development, always return the fallback as
		// gravatar cannot properly redirect to localhost
		// when a user does not have a gravatar image defined
		out << ((grails.util.GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT) ? fallbackURL : URL)
	}
}
