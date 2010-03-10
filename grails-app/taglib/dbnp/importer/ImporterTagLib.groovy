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
import dbnp.studycapturing.Template
import dbnp.studycapturing.TemplateFieldType

class ImporterTagLib {
    static namespace = 'importer'
    def standardentities = [[type:-1, name:"Don't import"], [type:0, name:"Study"], [type:1, name:"Subject"], [type:2, name:"Event"],
			[type:3, name:"Protocol"], [type:4, name:"Sample"]]

    /*def standardcelltypes = [
			 [type:0, name:"Numeric"], [type:1, name:"String"], [type:2, name:"Formula"],
			 [type:3, name:"Blank"], [type:4, name:"Boolean"], [type:5, name:"Error"], [type:6, name:"Date"],
			 [type:7, name:"Float"], [type:8, name:"Double"], [type:9, name:"List of items"], [type:10, name:"Ontologyterm"]
		     ]*/

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
     * @param entities array in the format of columnindex:entitytype format
     */
    def properties = { attrs ->
	def selectedentities = []
	def header = attrs['header']

	attrs['entities'].each { se ->
	    def temp = se.split(":")
	    def entity = [type:temp[1],columnindex:temp[0]]
	    selectedentities.add(entity)
	}

	out << render (template:"common/properties", model:[selectedentities:selectedentities, standardentities:standardentities, header:header])
    }

    def createSelect(int selected, String name, options, String customvalue) {
	def res = "<select style=\"font-size:10px\" name=\"${name}\">"

	options.each { e ->
	    res += "<option value=\"${customvalue}:${e.type}\""
	    res += (e.type.toInteger() == selected) ? " selected" : ""
	    res += ">${e.name}</option>"
	}

	res += "</select>"
	return res
    }

    /**
     * Possibly this will later on return an AJAX-like autocompletion chooser for the fields?
     * 
     * @param importtemplate_id template identifier where fields are retrieved from
     * @param columnindex column in the header we're talking about
     * @return chooser object
     * */
    def propertyChooser = { attrs ->
	// TODO: this should be changed to retrieving fields per entity
	def t = Template.get(session.importtemplate_id)
	def columnindex = attrs['columnindex']

	switch (attrs['entitytype']) {
	    case 0  : createPropertySelect(attrs['name'], t.fields, columnindex)
		      break
	    case 1  : break
	    case 2  : break
	    case 3  : break
	    default : out << createPropertySelect(attrs['name'], t.fields, columnindex)
		     break
	}
    }

    /**
     * @param name name of the HTML select object
     * @param options list of options to be used
     * @param columnIndex column identifier (corresponding to position in header of the Excel sheet)
     * @return HTML select object
     */
    def createPropertySelect(String name, options, String columnIndex)
    {
	def res = "<select style=\"font-size:10px\" name=\"${name}\">"

	options.each { f ->
	    res += "<option value=\"${columnIndex}:${f.id}\""
	    //res += (e.type.toInteger() == selected) ? " selected" : ""
	    res += ">${f}</option>"
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
	//out << createSelect(selected, attrs['name'], standardcelltypes, customvalue)
	out << createSelect(selected, attrs['name'], TemplateFieldType.list(), customvalue)
    }
}
