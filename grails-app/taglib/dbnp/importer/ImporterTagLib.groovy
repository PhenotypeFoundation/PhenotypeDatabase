/**
 * Importer tag library
 *
 * The importer tag library gives support for automating several 'components'
 *
 * @package	importer
 * @author	t.w.abma@umcutrecht.nl
 * @since	20100202
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

package dbnp.importer

class ImporterTagLib {
    static namespace = 'importer'
    def standardentities = [[type:0, name:"Study"], [type:1, name:"Subject"], [type:2, name:"Event"],
			[type:3, name:"Protocol"], [type:4, name:"Sample"]]

    def standardcelltypes = [[type:0, name:"Numeric"], [type:1, name:"String"], [type:2, name:"Formula"],
			 [type:3, name:"Blank"], [type:4, name:"Boolean"], [type:5, name:"Error"]]

    /**
    * @param header string array containing header
    * @param datamatrix two dimensional array containing actual data
    * @return preview of the data with the ability to adjust the datatypes
    */
    def preview = { attrs ->
	
	def header = attrs['header']
	def datamatrix = attrs['datamatrix']

	out << render (template:"common/preview", model:[header:header, datamatrix:datamatrix])
    }

    def entity = { attrs ->
	out << entities[attrs['index']].name
    }

    /**
     * @param entities array of entity:columnindex values
     */
    def properties = { attrs ->
	def selectedentities = []
	def header = attrs['header']

	attrs['entities'].each { se ->
	    def temp = se.split(":")
	    def entity = [type:temp[0],columnindex:temp[1]]
	    selectedentities.add(entity)
	}

	out << render (template:"common/properties", model:[selectedentities:selectedentities, standardentities:standardentities, header:header])
    }

    def createSelect(int selected, String name, ArrayList options, String customvalue) {
	def res = "<select style=\"font-size:10px\" name=\"${name}\">"

	options.each { e ->
	    res += "<option value=\"${e.type}:${customvalue}\""
	    res += (e.type.toInteger() == selected) ? " selected" : ""
	    res += ">${e.name}</option>"
	}

	res += "</select>"
	return res
    }

    

    /**
     * @param selected selected entity
     * @param name name of the HTML select object
     **/
    def entitySelect = { attrs ->	
	def selected = (attrs['selected']==null) ? -1 : attrs['selected']
	def customvalue = (attrs['customvalue']==null) ? "" : attrs['customvalue']
	out << createSelect(selected, attrs['name'], standardentities, customvalue)
    }

    /**
    * @param selected selected celltype
    * @param name name of the HTML select object
    * @see org.apache.poi.ss.usermodel.Cell for the possible cell types
    * @return HTML select object
    */
    def celltypeSelect = { attrs ->
	def selected = (attrs['selected']==null) ? -1 : attrs['selected']
	def customvalue = (attrs['customvalue']==null) ? "" : attrs['customvalue']
	out << createSelect(selected, attrs['name'], standardcelltypes, customvalue)
    }
    

}
