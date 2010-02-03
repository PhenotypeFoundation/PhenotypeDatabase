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

    /**
    * @param selected selected celltype
    * @param name name of the HTML select object
    * @param celltypes built-in cell types, based on the cell type
    * @see org.apache.poi.ss.usermodel.Cell
    * @return HTML select object
    */
    def celltypeselector = { attrs ->
	def selected = attrs['selected']
	def name = attrs['name']
	def celltypes = [[celltype:0, name:"Numeric"], [celltype:1, name:"String"], [celltype:2, name:"Formula"],
			 [celltype:3, name:"Blank"], [celltype:4, name:"Boolean"], [celltype:5, name:"Error"]]

	def res = "<select name=\"${name}\">"

	celltypes.each { c ->
	    res += "<option value=\"${c.celltype}\""
	    res += (c.celltype == selected) ? " selected" : ""
	    res += ">${c.name}</option>"
	}

	res += "</select>"

	out << res
    }

}
