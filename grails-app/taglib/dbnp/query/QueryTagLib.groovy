package dbnp.studycapturing

import org.codehaus.groovy.grails.plugins.web.taglib.JavascriptTagLib
import dbnp.studycapturing.*
import dbnp.data.*

/**
 * Wizard tag library
 *
 * @author Jeroen Wesbeek
 * @since 20100113
 * @package wizard
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

class QueryTagLib extends JavascriptTagLib {

	/**
	 * render the content of a particular wizard page
	 * @param Map attrs
	 * @param Closure body  (help text)
	 */

	def pageContent = {attrs, body ->
		// define AJAX provider
		setProvider([library: ajaxProvider])

		// render new body content
		/*
		out << render(template: "/wizard/common/tabs")
		out << '<div class="content">'
		out << body()
		out << '</div>'
		out << render(template: "/wizard/common/navigation")
		out << render(template: "/wizard/common/error")
		*/
	}

}