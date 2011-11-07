/**
 * Info Controler
 *
 * Provides some information about the application, for the
 * admin's eyes only
 *
 * @author  your email (+name?)
 * @since	20110412
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package generic

import grails.plugins.springsecurity.Secured
import dbnp.authentication.SecUser

@Secured(['ROLE_ADMIN'])
class InfoController {
	/**
	 * index closure
	 */
    def index = { }
}
