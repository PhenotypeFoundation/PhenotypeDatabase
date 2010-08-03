/**
 * Nimble Profile Domain Class
 *
 * @see http://www.grails.org/plugin/nimble
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

package nimble

class User extends grails.plugins.nimble.core.UserBase {
	def String toString() {
		return this.username;
	}
}
