/**
 * FuzzyStringMatch Controller
 *
 * A String Comparison controller
 *
 * @author  Jeroen Wesbeek (work@osx.eu)
 * @since	20110428
 * @package	org.dbnp.gdt
 *
 * Revision information:
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
 */
package org.dbnp.gdt

import grails.converters.JSON

class FuzzyStringMatchController {
	def fuzzyStringMatchService
	def gdtService

	/**
	 * find properties of a given entity that have similar values
	 */
	def ajaxFuzzyFind = {
		def result			= []
		def searchValue		= params.value
		def searchProperty	= params.property
		def searchEntity	= gdtService.getInstanceByEntity(params.entity)

		// got a proper entity?
		if (searchEntity) {
			// find property values that match this search value
			try {
				result = fuzzyStringMatchService.findByDifference(searchEntity,searchProperty,searchValue)
			} catch (Exception e) {
				//println "error searching --> ${e.getMessage()}"
			}
		}

		// output result
		response.setContentType("application/json; charset=UTF-8")
		render "${params.callback}(${result as JSON})"
	}
}
