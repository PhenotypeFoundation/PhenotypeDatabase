/**
 * StudyEditTagLib Tag Library
 *
 * This tag library contains tags used in the study edit pages
 *
 * @author  your email (+name?)
 * @since	2013
 * @package	???
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.studycapturing

import java.lang.ClassValue.Identity;

// TODO: Refactor wizard tag library
class StudyEditTagLib {
	// define the tag namespace (e.g.: <edit:action ... />
	static namespace = "studyEdit"
	
	def templatedDatatable = { attrs, body ->
		r.require( module: "gscf-datatables" )
		
		def id = attrs.get( "id" )
		def dataUrl = attrs.get( "dataUrl" )
		
		out << "<table class='templatedDatatable' "
		
		out << 'id="' + id + '" '
		out << 'data-url="' + dataUrl + '" '
		
		out << "></table>"
	}
	
}
