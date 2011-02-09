/**
 * Importer tag library
 *
 * The importer tag library gives support for automating several 'components'
 *
 * @package importer
 * @author t.w.abma@umcutrecht.nl
 * @since 20100202
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

package dbnp.importer
import org.dbnp.gdt.*
import org.dbnp.gdt.*

import org.dbnp.gdt.*

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter

class ImporterTagLib {
	static namespace = 'importer'
	def ImporterService
	def GdtService

	/**
	 * @param header string array containing header
	 * @param datamatrix two dimensional array containing actual data
	 * @return preview of the data with the ability to adjust the datatypes
	 */
	def preview = { attrs ->

		def header = attrs['header']
		def datamatrix = attrs['datamatrix']

		out << render(template: "common/preview", model: [header: header, datamatrix: datamatrix])
	}

	/**
	 * @param datamatrix two dimensional array containing entities with read values
	 * @return postview of the imported data
	 */
	def postview = { attrs ->
		def datamatrix = attrs['datamatrix']

		out << render(template: "common/postview", model: [datamatrix: datamatrix])
	}

	def entity = { attrs ->
		out << entities[attrs['index']].name
	}

	def datapreview = { attrs ->
		def datamatrix = attrs['datamatrix']
		out << render(template: "common/datapreview", model: [datamatrix: datamatrix])
	}

	/**
	 * Show missing properties
	 */
	def missingProperties = { attrs ->
		def datamatrix = attrs['datamatrix']
		def failedcells = attrs['failedcells']
		out << render(template: "common/missingproperties", model: [datamatrix: datamatrix, failedcells: failedcells])
	}

	/**
	 * Show failed cells
	 */
	def failedCells = { attrs ->
		def failedcells = attrs['failedcells']
		out << render(template: "common/failedcells", model: [failedcells: failedcells])
	}

	/**
	 * @param entities array containing selected entities
	 * @param header array containing mappingcolumn objects
	 * @param allfieldtypes if set, show all fields
	 */
	def properties = { attrs ->
		def header = attrs['header']
		def entities = attrs['entities']

		out << render(template: "common/properties_horizontal")
	}

	/**
	 * Possibly this will later on return an AJAX-like autocompletion chooser for the fields?
	 *
	 * @param name name for the property chooser element
	 * @param importtemplate_id template identifier where fields are retrieved from
	 * @param matchvalue value which will be looked up via fuzzy matching against the list of options and will be selected
	 * @param MappingColumn object containing all required information
     * @param fuzzymatching boolean true if fuzzy matching should be used, otherwise false
	 * @param allfieldtypes boolean true if all templatefields should be listed, otherwise only show filtered templatefields
	 * @return chooser object
	 * */
	def propertyChooser = { attrs ->
		// TODO: this should be changed to retrieving fields per entity instead of from one template
		//	 and session variables should not be used inside the service, migrate to controller

		def t = Template.get(attrs['template_id'])
		def mc = attrs['mappingcolumn']
		def allfieldtypes = attrs['allfieldtypes']		
        def matchvalue = (attrs['fuzzymatching']=="true") ? attrs['matchvalue'] : ""

        def domainfields = mc.entity.giveDomainFields().findAll { it.type == mc.templatefieldtype }
		domainfields = domainfields.findAll { it.preferredIdentifier != mc.identifier}

		//def templatefields = (allfieldtypes=="true") ? t.fields : t.fields.findAll { it.type == mc.templatefieldtype }
		/*def templatefields = (allfieldtypes == "true") ?
			t.fields + mc.entity.giveDomainFields() :
			t.fields.findAll { it.type == mc.templatefieldtype } + domainfields*/
        def templatefields = t.fields + mc.entity.giveDomainFields()

		// map identifier to preferred column
		def prefcolumn = mc.entity.giveDomainFields().findAll { it.preferredIdentifier == true }

		/*(mc.identifier) ? out << createPropertySelect(attrs['name'], prefcolumn, matchvalue, mc.index) :
			out << createPropertySelect(attrs['name'], templatefields, matchvalue, mc.index)*/
         out << createPropertySelect(attrs['name'], templatefields, matchvalue, mc.index)
	}

	/**
	 * Create the property chooser select element
	 *
	 * @param name name of the HTML select object
	 * @param options list of options (fields) to be used
	 * @param matchvalue value which will be looked up via fuzzy matching against the list of options and will be selected
	 * @param columnIndex column identifier (corresponding to position in header of the Excel sheet)
	 * @return HTML select object
	 */
	def createPropertySelect(String name, options, matchvalue, Integer columnIndex) {
		// Determine which field in the options list matches the best with the matchvalue
		def mostsimilar = (matchvalue) ? ImporterService.mostSimilar(matchvalue, options) : ""

		def res = "<select style=\"font-size:10px\" id=\"${name}.index.${columnIndex}\" name=\"${name}.index.${columnIndex}\">"

		res += "<option value=\"dontimport\">Don't import</option>"

		options.each { f ->
			res += "<option value=\"${f.name}\""

			res += (mostsimilar.toString().toLowerCase() == f.name.toLowerCase()) ?
				" selected>" :
				">"

			res += (f.preferredIdentifier) ?
				"${f.name} (IDENTIFIER)</option>" :
				"${f.name}</option>"
		}

		res += "</select>"
		return res
	}

	/**
	 * @param selected selected TemplateFieldType
	 * @param custval custom value to be combined in the option(s) of the selector
	 * @param name name of the HTML select object
	 * @return HTML select object
	 *
	 * @see org.dbnp.gdt.TemplateFieldType
	 */

	def entitySelect = { attrs ->
		def sel = (attrs['selected'] == null) ? -1 : attrs['selected']
		def custval = (attrs['customvalue'] == null) ? "" : attrs['customvalue']
		def name = (attrs['name'] == null) ? -1 : attrs['name']

		def res = "<select style=\"font-size:10px\" name=\"${name}.index.${custval}\">"

		GdtService.getTemplateEntities().each { e ->
			res += "<option value\"${e.name}\""
			res += ">${e.name} bla</option>"
		}

		res += "</select>"
		out << res
	}

	/**
	 * Create a templatefieldtype selector
	 *
	 * @param selected selected TemplateFieldType
	 * @param customvalue custom value to be combined in the option(s) of the selector
	 * @param name name of the HTML select object
	 * @return HTML select object
	 *
	 * @see org.dbnp.gdt.TemplateFieldType
	 */
	def templatefieldtypeSelect = { attrs ->
		def selected = (attrs['selected'] == null) ? -1 : attrs['selected']
		def custval = (attrs['customvalue'] == null) ? "" : attrs['customvalue']
		def name = (attrs['name'] == null) ? "" : attrs['name']

		def res = "<select style=\"font-size:10px\" name=\"${name}.index.${custval}\">"

		TemplateFieldType.list().each { e ->
			res += "<option value=\"${e}\""
			res += (e == selected) ? " selected" : ""
			res += ">${e}</option>"
		}

		res += "</select>"

		out << res
	}

	/**
	 * @param cell Cell variable
	 * @return good representation of variable (instead of toString())
	 */
	def displayCell = { attrs ->
		def cell = attrs['cell']
		def df = new DataFormatter()

		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING: out << cell.getStringCellValue()
				break
			case Cell.CELL_TYPE_NUMERIC: out << df.formatCellValue(cell)
				break
		}
	}
}
