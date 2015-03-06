/**
 *  GDTImporter, a plugin for importing data into Grails Domain Templates
 *  Copyright (C) 2011 Tjeerd Abma
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  $Author$
 *  $Rev$
 *  $Date$
 */

package org.dbnp.gdtimporter

import org.dbnp.gdt.*
import grails.converters.JSON

/**
 * The GdtImporter tag library contains easy tags for displaying and
 * working with imported data
 */

class GdtImporterTagLib {
    static namespace = 'GdtImporter'
	def GdtImporterService
	def GdtService

    /**
	 * @param header string array containing header
	 * @param datamatrix two dimensional array containing actual data
	 * @return preview of the data with the ability to adjust the datatypes
	 */
	def preview = { attrs ->

		def header = attrs['header']
		def dataMatrix = attrs['dataMatrix']

		out << render(template: "common/preview", plugin: "gdtimporter", model: [header: header, datamatrix: dataMatrix])
	}

	def entity = { attrs ->
		out << entities[attrs['index']].name
	}

	def datapreview = { attrs ->
		def dataMatrix = attrs['dataMatrix']
		out << render(template: "common/datapreview", plugin: "gdtimporter", model: [dataMatrix: dataMatrix])
	}

	/**
	 * Show missing properties
	 */
	def validation = { attrs ->
		def entityList = attrs['entityList']
		def failedFields = attrs['failedFields']
		out << render(template: "common/validation", plugin: "gdtimporter", model: [entityList: entityList, failedFields: failedFields])
	}

   /**
     * Show checkbox and put it checked if more than 'checkedIfRowsMoreThan' rows, show a warning if more than 'warningIfRowsMoreThan' rows
     *
     *  @param size datamatrix size (number of rows)
     *  @param checkedIfRowsLessThan amount of rows required to set the checkbox to checked
     *  @param warningIfRowsMoreThan amount of rows before a warning is given
     */
    def showTableEditorCheckBox= { attrs ->
        def warningMessage = "if (\$('#showTableEditor').is(':checked')) alert ('Warning: you are importing a large number of rows, using this function might render the import wizard unresponsive.')"

        out << "Show editable table before importing " + g.checkBox(id:"showTableEditor", name:"showTableEditor", checked: (attrs.size.toInteger()<=attrs.checkedIfRowsLessThan.toInteger()),
                onClick:(attrs.warningIfRowsMoreThan.toInteger() >= attrs.size.toInteger()) ? "":warningMessage)
    }

    /**
     * @param entityList list of imported entities
     * @param failedFields list of failed fields
     * @return preview of the imported entities
     */

    def previewImportedAndFailedEntities = { attrs ->

        out << '<script>'
        out << '$(document).ready(function() {'
	    out << '$("#previewImportedAndFailedEntities").dataTable( { "sScrollX": "100%", "bScrollCollapse": true, "bPaginate": false, "bSort" : false })'
        out << "} );"
        out << '</script>'

        out << '<table id="previewImportedAndFailedEntities" class="display"><thead><tr>'

        // Give all fields of the entity to build up the table header for the datatables JS library
        attrs.entityList[0].giveFields().each {
            out << "<th>${it}</th>"
        }

        out << '</tr></thead>'
        out << '<tbody>'

        // For every entity check the failed fields
        attrs.entityList.each { importedEntity ->

            // Find all fields which failed for the current entity
            def failedFieldList = attrs.failedFields.findAll { field ->
                field.identifier == importedEntity.identifier
            }


            // For every entity show the fields and the values, including the highlighting of failed fields
            out << '<tr>'
                importedEntity.giveFields().each { field ->
                    out << "<td>${ (importedEntity.getFieldValue(field.name)) ?:""}"

                    // Highlight the current cell if it is listed in the failed fields collection
                    if (attrs.failedFields.find { it.identifier == importedEntity.identifier && it.property == field.name})
                        out << ' <span style="color:red"><b> (invalid)</b></span>'

                    out << "</td>"
                }
            out << '</tr>'
        }

        out << '</tbody>'
        out << '</table>'
    }

	/**	 
	 * @param header array containing mappingcolumn objects	 
	 */
	def properties = { attrs ->
		def header = attrs['header']

		out << render(template: "common/properties", plugin: "gdtimporter", model: [header:header])
	}

	/**
	 * Possibly this will later on return an AJAX-like autocompletion chooser for the fields?
	 *
	 * @param name name for the property chooser element
	 * @param templateId template identifier where fields are retrieved from
	 * @param matchvalue value which will be looked up via fuzzy matching against the list of options and will be selected
     * @param selected value to be selected by default
	 * @param MappingColumn object containing all required information
     * @param useFuzzymatching boolean true if fuzzy matching should be used, otherwise false
     * @param extraOptions options to add to the select boxes (e.g. non template field items)
	 * @return chooser object
	 * */
	def propertyChooser = { attrs ->

		// TODO: this should be changed to retrieving fields per entity instead of from one template?
 		def t = Template.get(attrs['templateId'])
		def mc = attrs['mappingcolumn']
        def matchvalue = (attrs['useFuzzymatching']=="true") ? attrs['matchvalue'] : ""
        def selected = (attrs['selected']) ? attrs['selected'] : ""
		def fuzzyTreshold = attrs[ 'treshold' ] && attrs[ 'treshold' ].toString().isNumber() ? Float.valueOf( attrs[ 'treshold' ] ) : 0.1;
        def returnmatchonly = attrs['returnmatchonly']

        def templatefields = t.fields + mc.entityclass.newInstance().giveDomainFields() + (attrs.extraOptions ?: [])

        //  Just return the matched value only
        if (returnmatchonly)
            out << GdtImporterService.mostSimilar(matchvalue, templatefields, fuzzyTreshold)
        else // Return a selectbox
            out << createPropertySelect(attrs['name'], templatefields, matchvalue, selected, mc.index, fuzzyTreshold)

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
	def createPropertySelect(String name, options, matchvalue, selected, Integer columnIndex, float fuzzyTreshold = 0.1f) {

		// Determine which field in the options list matches the best with the matchvalue
		def mostsimilar = (matchvalue) ? GdtImporterService.mostSimilar(matchvalue, options, fuzzyTreshold) : ""

		def res = "<select style=\"font-size:10px\" id=\"${name}.index.${columnIndex}\" name=\"${name}.index.${columnIndex}\">"
        def prefIdentifier = options.find { it.preferredIdentifier}

        res += "<option value=\"dontimport\">Don't import</option>"

        options.sort { it.preferredIdentifier }.reverse().each { f ->
			res +=  "<option value=\"${f.name}\""

			// mostsimilar string passed as argument or selected value passed?
            res += (mostsimilar.toString().toLowerCase() == f.name.toLowerCase() || selected.toLowerCase() == f.name.toLowerCase() ) ?
				" selected='selected'>" :
				">"

			res += "${f.name} ${ (!f.unit)?'': '(' + f.unit + ')'}"
            res += (!f.preferredIdentifier) ? '': '(IDENTIFIER)'
            res += "</option>"
		}

        res += "</select>"

		return res
	}
}
