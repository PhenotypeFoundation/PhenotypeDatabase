package dbnp.studycapturing

import dbnp.studycapturing.*
import dbnp.authentication.SecUser
import dbnp.data.*
import cr.co.arquetipos.crypto.Blowfish
import nl.grails.plugins.ajaxflow.AjaxflowTagLib

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
class WizardTagLib extends AjaxflowTagLib {
	def AuthenticationService
        
	// define the tag namespace (e.g.: <wizard:action ... />
	static namespace = "wizard"

	// define default text field width
	static defaultTextFieldSize = 25;

	/**
	 * generate a base form element
	 * @param String inputElement name
	 * @param Map attributes
	 * @param Closure help content
	 */
	def baseElement = { inputElement, attrs, help ->
		log.info ".rendering [" + inputElement + "] with name [" + attrs.get('name') + "] and value [" + ((attrs.value) ? attrs.get('value').toString() : "-") + "]"

		// work variables
		def description = attrs.remove('description')
		def addExampleElement = attrs.remove('addExampleElement')
		def addExample2Element = attrs.remove('addExample2Element')
		def helpText = help().trim()

		// execute inputElement call
		def renderedElement = "$inputElement"(attrs)

		// if false, then we skip this element
		if (!renderedElement) return false

		// render a form element
		out << '<div class="element'+ ((attrs.get('required')) ? ' required' : '') +'"'+ ((attrs.get('elementId')) ? 'id="'+attrs.remove('elementId')+'"': '') + '>'
		out << ' <div class="description">'
		out << ((description) ? description.replaceAll(/[a-z][A-Z][a-z]/) { it[0] + ' ' + it[1..2] }.replaceAll(/\w+/) { it[0].toUpperCase() + ((it.size() > 1) ? it[1..-1] : '') } : '')
		out << ' </div>'
		out << ' <div class="input">'
		out << renderedElement
		out << ((helpText.size() > 0) ? '	<div class="helpIcon"></div>' : '')

		// add an disabled input box for feedback purposes
		// @see dateElement(...)
		if (addExampleElement) {
			def exampleAttrs = new LinkedHashMap()
			exampleAttrs.name = attrs.get('name') + 'Example'
			exampleAttrs.class = 'isExample'
			exampleAttrs.disabled = 'disabled'
			exampleAttrs.size = 30
			out << textField(exampleAttrs)
		}

		// add an disabled input box for feedback purposes
		// @see dateElement(...)
		if (addExample2Element) {
			def exampleAttrs = new LinkedHashMap()
			exampleAttrs.name = attrs.get('name') + 'Example2'
			exampleAttrs.class = 'isExample'
			exampleAttrs.disabled = 'disabled'
			exampleAttrs.size = 30
			out << textField(exampleAttrs)
		}

		out << ' </div>'

		// add help content if it is available
		if (helpText.size() > 0) {
			out << '  <div class="helpContent">'
			out << '    ' + helpText
			out << '  </div>'
		}

		out << '</div>'
	}

	/**
	 * bind an ajax submit to an onChange event
	 * @param attrs
	 * @return attrs
	 */
	private getAjaxOnChange = { attrs ->
		// work variables
		def internetExplorer = (request.getHeader("User-Agent") =~ /MSIE/)
		def ajaxOnChange = attrs.remove('ajaxOnChange')

		// is ajaxOnChange defined
		if ( ajaxOnChange ) {
			if (!attrs.onChange) attrs.onChange = ''

			// add onChange AjaxSubmit javascript
			if (internetExplorer) {
				// 		- somehow IE submits these onchanges twice which messes up some parts of the wizard
				//		  (especially the events page). In order to bypass this issue I have introduced an
				//		  if statement utilizing the 'before' and 'after' functionality of the submitToRemote
				//		  function. This check expects lastRequestTime to be in the global Javascript scope,
				//		  (@see pageContent) and calculates the time difference in miliseconds between two
				//		  onChange executions. If this is more than 100 miliseconds the request is executed,
				//		  otherwise it will be ignored... --> 20100527 - Jeroen Wesbeek
				attrs.onChange += ajaxSubmitJs(
					[
						before: "var execute=true;try { var currentTime=new Date().getTime();execute = ((currentTime-lastRequestTime) > 100);lastRequestTime=currentTime;  } catch (e) {};if (execute) { 1",
						after: "}",
						functionName: ajaxOnChange,
						url: attrs.get('url'),
						update: attrs.get('update'),
						afterSuccess: attrs.get('afterSuccess')
					],
					''
				)
			} else {
				// this another W3C browser that actually behaves as expected... damn you IE, DAMN YOU!
				attrs.onChange += ajaxSubmitJs(
					[
						functionName: ajaxOnChange,
						url: attrs.get('url'),
						update: attrs.get('update'),
						afterSuccess: attrs.get('afterSuccess')
					],
					''
				)
			}
		}

		return attrs
	}

	/**
	 * render an ajaxButtonElement
	 * @param Map attrs
	 * @param Closure body  (help text)
	 */
	def ajaxButtonElement = { attrs, body ->
		baseElement.call(
			'ajaxButton',
			attrs,
			body
		)
	}

	/**
	 * render a textFieldElement
	 * @param Map attrs
	 * @param Closure body  (help text)
	 */
	def textFieldElement = { attrs, body ->
		// set default size, or scale to max length if it is less than the default size
		if (!attrs.get("size")) {
			if (attrs.get("maxlength")) {
				attrs.size = ((attrs.get("maxlength") as int) > defaultTextFieldSize) ? defaultTextFieldSize : attrs.get("maxlength")
			} else {
				attrs.size = defaultTextFieldSize
			}
		}

		// render template element
		baseElement.call(
			'textField',
			attrs,
			body
		)
	}

 	/**
	 * render a textAreaElement
	 * @param Map attrs
	 * @param Closure body  (help text)
	 */
	def textAreaElement = { attrs, body ->
		// set default size, or scale to max length if it is less than the default size

		// render template element
		baseElement.call(
			'textArea',
			attrs,
			body
		)
	}


	/**
	 * render a select form element
	 * @param Map attrs
	 * @param Closure body  (help text)
	 */
	def selectElement = { attrs, body ->
		baseElement.call(
			'select',
			attrs,
			body
		)
	}

	/**
	 * render a checkBox form element
	 * @param Map attrs
	 * @param Closure body  (help text)
	 */
	def checkBoxElement = { attrs, body ->
		baseElement.call(
			'checkBox',
			attrs,
			body
		)
	}

	/**
	 * render a set of radio form elements
	 * @param Map attrs
	 * @param Closure body  (help text)
	 */
	def radioElement = { attrs, body ->
		baseElement.call(
			'radioList',
			attrs,
			body
		)
	}

	/**
	 * render a set of radio elements
	 * @param Map attrs
	 * @param Closure body  (help text)
	 */
	def radioList = { attrs ->
		def checked = true

		attrs.elements.each {
			out << radio(
				name: attrs.name,
				value: it,
				checked: (attrs.value == it || (!attrs.value && checked))
			)
			out << it
			checked = false
		}
	}

	/**
	 * render a dateElement
	 * NOTE: datepicker is attached through wizard.js!
	 * @param Map attrs
	 * @param Closure body  (help text)
	 */
	def dateElement = { attrs, body ->
		// transform value?
		if (attrs.value instanceof Date) {
			// transform date instance to formatted string (dd/mm/yyyy)
			attrs.value = String.format('%td/%<tm/%<tY', attrs.value)
		}

		// add 'rel' field to identity the datefield using javascript
		attrs.rel = 'date'

		// set some textfield values
		attrs.maxlength = (attrs.maxlength) ? attrs.maxlength : 10
		attrs.addExampleElement = true

		// render a normal text field
		//out << textFieldElement(attrs,body)
		textFieldElement.call(
			attrs,
			body
		)
	}

	/**
	 * render a dateElement
	 * NOTE: datepicker is attached through wizard.js!
	 * @param Map attrs
	 * @param Closure body  (help text)
	 */
	def timeElement = { attrs, body ->
		// transform value?
		if (attrs.value instanceof Date) {
			// transform date instance to formatted string (dd/mm/yyyy)
			attrs.value = String.format('%td/%<tm/%<tY %<tH:%<tM', attrs.value)
		}

		// add 'rel' field to identity the field using javascript
		attrs.rel = 'datetime'

		attrs.addExampleElement = true
		attrs.addExample2Element = true
		attrs.maxlength = 16

		// render a normal text field
		//out << textFieldElement(attrs,body)
		textFieldElement.call(
			attrs,
			body
		)
	}

	/**
	 * Button form element
	 * @param Map attributes
	 * @param Closure help content
	 */
	def buttonElement = { attrs, body ->
		// render template element
		baseElement.call(
			'ajaxButton',
			attrs,
			body
		)
	}


	/**
	 * Term form element
	 * @param Map attributes
	 * @param Closure help content
	 */
	def termElement = { attrs, body ->
		// render term element
		baseElement.call(
			'termSelect',
			attrs,
			body
		)
	}

	/**
	 * Term select element
	 * @param Map attributes
	 */
	// TODO: change termSelect to use Term accessions instead of preferred names, to make it possible to track back
	// terms from multiple ontologies with possibly the same preferred name
	def termSelect = { attrs ->
		def from = []

		// got ontologies?
		if (attrs.ontologies) {
			// are the ontologies a string?
			if (attrs.ontologies instanceof String) {
				attrs.ontologies.split(/\,/).each() { ncboId ->
					// trim the id
					ncboId.trim()

					// fetch all terms for this ontology
					def ontology = Ontology.findAllByNcboId(ncboId)

					// does this ontology exist?
					if (ontology) {
						ontology.each() {
							Term.findAllByOntology(it).each() {
								// key = ncboId:concept-id
								from[ from.size() ] = it.name
							}
						}
					}
				}
			} else if (attrs.ontologies instanceof Set) {
				// are they a set instead?
				def ontologyList = ""

				// iterate through set
				attrs.ontologies.each() { ontology ->
					if (ontology) {
						ontologyList += ontology.ncboId + ","

						Term.findAllByOntology(ontology).each() {
							from[ from.size() ] = it.name
						}

						// strip trailing comma
						attrs.ontologies = ontologyList[0..-2]
					}
				}
			}

			// sort alphabetically
			from.sort()

			// add a dummy field?
			if (attrs.remove('addDummy')) {
				from.add(0,'')
			}

			// define 'from'
			attrs.from = from

			// add 'rel' attribute
			attrs.rel = 'term'

			// got an ajaxOnChange defined?
			attrs = getAjaxOnChange.call(
				attrs
			)

			out << select(attrs)
		} else {
			out << "<b>ontologies missing!</b>"
		}
	}

	/**
	 * Ontology form element
	 * @param Map attributes
	 * @param Closure help content
	 */
	def ontologyElement = { attrs, body ->
		// @see http://www.bioontology.org/wiki/index.php/NCBO_Widgets#Term-selection_field_on_a_form
		// @see ontology-chooser.js, table-editor.js
		baseElement.call(
			'textField',
			[
			    name: attrs.name,
				value: attrs.value,
				description: attrs.description,
				rel: 'ontology-' + ((attrs.ontology) ? attrs.ontology : 'all'),
				size: 25
			],
			body
		)
		out << hiddenField(
			name: attrs.name + '-concept_id'
		)
		out << hiddenField(
			name: attrs.name + '-ontology_id'
		)
		out << hiddenField(
			name: attrs.name + '-full_id'
		)
	}

	/**
	 * Study form element
	 * @param Map attributes
	 * @param Closure help content
	 */
	def studyElement = { attrs, body ->
		// render study element
		baseElement.call(
			'studySelect',
			attrs,
			body
		)
	}

	/**
	 * render a study select element
	 * @param Map attrs
	 */
	def studySelect = { attrs ->
		// Find all studies the user has access to (max 100)
		attrs.from = Study.giveWritableStudies(AuthenticationService.getLoggedInUser(), 100);

		// got a name?
		if (!attrs.name) {
			attrs.name = "study"
		}

		// got result?
		if (attrs.from.size() > 0) {
			out << select(attrs)
		} else {
			// no, return false to make sure this element
			// is not rendered in the template
			return false
		}
	}

	/**
	 * Template form element
	 * @param Map attributes
	 * @param Closure help content
	 */
	def templateElement = { attrs, body ->
		// render template element
		baseElement.call(
			'templateSelect',
			attrs,
			body
		)
	}

	/**
	 * render a template select element
	 * @param Map attrs
	 */
	def templateSelect = { attrs ->
		def entity = attrs.remove('entity')

		// add the entity class name to the element
		// do we have crypto information available?
		if (grailsApplication.config.crypto) {
			// generate a Blowfish encrypted and Base64 encoded string.
			attrs['entity'] = URLEncoder.encode(
				Blowfish.encryptBase64(
					entity.toString().replaceAll(/^class /, ''),
					grailsApplication.config.crypto.shared.secret
				)
			)
		} else {
			// base64 only; this is INSECURE! As this class
			// is instantiated elsewehere. Possibly exploitable!
			attrs['entity'] = URLEncoder.encode(entity.toString().replaceAll(/^class /, '').bytes.encodeBase64())
		}
		
		// fetch templates
		attrs.from = (entity) ? Template.findAllByEntity(entity) : Template.findAll()

		// got a name?
		if (!attrs.name) {
			attrs.name = 'template'
		}

		// add a rel element if it does not exist
		if (!attrs.rel) {
			attrs.rel = 'template'
		}

		// got an ajaxOnChange defined?
		attrs = getAjaxOnChange.call(
			attrs
		)

		// got result?
		if (attrs.from.size() > 0 || attrs.get('addDummy')) {
			// transform all values into strings
			def from = []
			attrs.from.each { from[ from.size() ] = it.toString() }

			// sort alphabetically
			from.sort()

			// add a dummy field?
			if (attrs.remove('addDummy')) {
				from.add(0,'')
			}

			// set attributes
			attrs.from = from
			attrs.value = (attrs.value) ? attrs.value.toString() : ''

			// output select element
			out << select(attrs)
		} else {
			// no, return false to make sure this element
			// is not rendered in the template
			return false
		}
	}


	/**
	 * File form element
	 * @param Map attributes
	 * @param Closure help content
	 */
	def fileFieldElement = { attrs, body ->
		// render term element
		baseElement.call(
			'fileField',
			attrs,
			body
		)
	}

	/**
	 * file field.
	 * @param attributes
	 */
	def fileField = { attrs ->
		/*
		out << '<input type="file" name="' + attrs.name + '"/>'
		if( attrs.value ) {
			out << '<a href="' + resource(dir: '') + '/file/get/' + attrs.value + '" class="isExample">Now contains: ' + attrs.value + '</a>'
		}
		*/

		out << '<div id="upload_button_' + attrs.name + '" class="upload_button">Upload</div>';
		out << '<input type="hidden" name="' + attrs.name + '" id="' + attrs.name + '" value="' + attrs.value + '">';
		out << '<div id="' + attrs.name + 'Example" class="upload_info"></div>';
		out << '<a id="' + attrs.name + 'Delete" class="upload_del" href="#" onClick="if( confirm( \'Are you sure to delete this file?\' ) ) { deleteFile( \'' + attrs.name + '\' ); } return false;"><img src="' + resource( dir: 'images/icons', file: 'delete.png', plugin: 'famfamfam' ) + '"></a>';
		out << '<script type="text/javascript">';
		out << '  $(document).ready( function() { ';
		out << '    var filename = "' + attrs.value + '";';
		out << '    fileUploadField( "' + attrs.name + '" );';
		out << '    if( filename != "" ) {';
		out << '      $("#' + attrs.name + 'Delete").show();';
		out << '      $("#' + attrs.name + 'Example").html("Current file: " + createFileHTML( filename ) )';
		out << '    }';
		out << '  } );';
		out << "</script>\n";
	}

	/**
	 * Protocol form element
	 * @param Map attributes
	 * @param Closure help content
	 */
	def protocolElement = { attrs, body ->
		// render protocol element
		baseElement.call(
			'protocolSelect',
			attrs,
			body
		)
	}

	/**
	 * render a protocol select element
	 * @param Map attrs
	 */
	def protocolSelect = { attrs ->
		// fetch all protocold
		attrs.from = Protocol.findAll()	// for now, all protocols

		// got a name?
		if (!attrs.name) {
			attrs.name = 'protocol'
		}

		out << select(attrs)
	}

	def show = { attrs ->
		// is object parameter set?
		def o = attrs.object

		println o.getProperties();
		o.getProperties().each {
			println it
		}

		out << "!! test version of 'show' tag !!"
	}

	/**
	 * render table headers for all subjectFields in a template
	 * @param Map attributes
	 */
	def templateColumnHeaders = { attrs ->
		def entity		= (attrs.get('entity'))
		def template	= (entity && entity instanceof TemplateEntity) ? entity.template : null
		def columnWidths= (attrs.get('columnWidths')) ? attrs.remove('columnWidths') : []

		// got a template?
		if (template) {
			// render template fields
			entity.giveFields().each() {
				// Format the column name by:
				// - separating combined names (SampleName --> Sample Name)
				// - capitalizing every seperate word
				def ucName = it.name.replaceAll(/[a-z][A-Z][a-z]/) {
					it[0] + ' ' + it[1..2]
				}.replaceAll(/\w+/) {
					it[0].toUpperCase() + ((it.size() > 1) ? it[1..-1] : '')
				}

				// strip spaces
				def ucNameSpaceless = ucName.replaceAll(/ /) { '' }

				// do we have to use a specific width for this column?
				if (columnWidths[ucName]) {
					out << '<div class="' + attrs.get('class') + '" style="width:' + columnWidths[ucNameSpaceless] + 'px;" rel="resized">' + ucName + (it.unit ? " (${it.unit})" : '')
				} else {
					out << '<div class="' + attrs.get('class') + '">' + ucName + (it.unit ? " (${it.unit})" : '')
				}
				if (it.comment) {
					out << '<div class="helpIcon"></div>'
					out << '<div class="helpContent">' + it.comment + '</div>'
				}
				out << '</div>'
			}
		}
	}

	def templateColumns = { attrs ->
		// render template fields as columns
		attrs.renderType = 'column'
		out << renderTemplateFields(attrs)
	}

	def templateElements = { attrs ->
		// render template fields as form elements
		attrs.renderType = 'element'
		out << renderTemplateFields(attrs)
	}

	/**
	 * render form elements based on an entity's template
	 * @param Map attributes
	 * @param String body
	 */
	def renderTemplateFields = { attrs ->
		def renderType	= attrs.remove('renderType')
		def entity		= (attrs.get('entity'))
		def prependName	= (attrs.get('name')) ? attrs.remove('name')+'_' : ''
		def template	= (entity && entity instanceof TemplateEntity) ? entity.template : null
		def inputElement= null
		def addDummy	= (attrs.get('addDummy')) ? true : false

		// got a template?
		if (template) {
			// render template fields
			entity.giveFields().each() {
				def fieldValue	= entity.getFieldValue(it.name)
				def helpText	= (it.comment && renderType == 'element') ? it.comment : ''
				def ucName		= it.name[0].toUpperCase() + it.name.substring(1)

				// output column opening element?
				if (renderType == 'column') {
					out << '<div class="' + attrs.get('class') + '">'
				}

				switch (it.type.toString()) {
					case ['STRING', 'DOUBLE', 'LONG']:
						inputElement = (renderType == 'element') ? 'textFieldElement' : 'textField'
						out << "$inputElement"(
							description	: ucName,
							name		: prependName + it.escapedName(),
							value		: fieldValue,
							required	: it.isRequired()
						){helpText}
						break
					case 'TEXT':
						inputElement = (renderType == 'element') ? 'textAreaElement' : 'textField'
						out << "$inputElement"(
							description	: ucName,
							name		: prependName + it.escapedName(),
							value		: fieldValue,
							required	: it.isRequired()
						){helpText}
						break
					case 'STRINGLIST':
						inputElement = (renderType == 'element') ? 'selectElement' : 'select'
						if (!it.listEntries.isEmpty()) {
							out << "$inputElement"(
								description	: ucName,
								name		: prependName + it.escapedName(),
								from		: it.listEntries,
								value		: fieldValue,
								required	: it.isRequired()
							){helpText}
						} else {
							out << '<span class="warning">no values!!</span>'
						}
						break
					case 'ONTOLOGYTERM':
						// @see http://www.bioontology.org/wiki/index.php/NCBO_Widgets#Term-selection_field_on_a_form
						// @see ontology-chooser.js
						inputElement = (renderType == 'element') ? 'termElement' : 'termSelect'

						// override addDummy to always add the dummy...
						addDummy = true

						if (it.ontologies) {
							out << "$inputElement"(
								description	: ucName,
								name		: prependName + it.escapedName(),
								value		: fieldValue.toString(),
								ontologies	: it.ontologies,
								addDummy	: addDummy,
								required	: it.isRequired()
							){helpText}
						} else {
							out << "$inputElement"(
								description	: ucName,
								name		: prependName + it.escapedName(),
								value		: fieldValue.toString(),
								addDummy	: addDummy,
								required	: it.isRequired()
							){helpText}
						}
						break
					case 'ONTOLOGYTERM-old':
						// @see http://www.bioontology.org/wiki/index.php/NCBO_Widgets#Term-selection_field_on_a_form
						// @see ontology-chooser.js
						inputElement = (renderType == 'element') ? 'textFieldElement' : 'textField'
						out << "$inputElement"(
							name	: prependName + it.escapedName(),
							value	: fieldValue,
							rel		: 'ontology-all',
							size	: 100,
							required: it.isRequired()
						)
						out << hiddenField(
							name: prependName + it.name + '-concept_id',
							value: fieldValue
						)
						out << hiddenField(
							name: prependName + it.escapedName() + '-ontology_id',
							value: fieldValue
						)
						out << hiddenField(
							name: prependName + it.escapedName() + '-full_id',
							value: fieldValue
						)
						break
					case 'DATE':
						inputElement = (renderType == 'element') ? 'dateElement' : 'textField'

						// transform value?
						if (fieldValue instanceof Date) {
							if (fieldValue.getHours() == 0 && fieldValue.getMinutes() == 0) {
								// transform date instance to formatted string (dd/mm/yyyy)
								fieldValue = String.format('%td/%<tm/%<tY', fieldValue)
							} else {
								// transform to date + time
								fieldValue = String.format('%td/%<tm/%<tY %<tH:%<tM', fieldValue)
							}
						}

						// render element
						out << "$inputElement"(
							description	: ucName,
							name		: prependName + it.escapedName(),
							value		: fieldValue,
							rel			: 'date',
							required	: it.isRequired()
						){helpText}
						break
					case ['RELTIME']:
						inputElement = (renderType == 'element') ? 'textFieldElement' : 'textField'
						out << "$inputElement"(
							description			: ucName,
							name				: prependName + it.escapedName(),
							value				: new RelTime( fieldValue ).toString(),
                            addExampleElement	: true,
                            onBlur				: 'showExampleReltime(this)',
							required			: it.isRequired()
						){helpText}
						break
					case ['FILE']:
						inputElement = (renderType == 'element') ? 'fileFieldElement' : 'fileField'
						out << "$inputElement"(
							description			: ucName,
							name				: prependName + it.escapedName(),
							value				: fieldValue ? fieldValue : "",
                            addExampleElement	: true,
							required			: it.isRequired()
						){helpText}
						break
					case ['BOOLEAN']:
						inputElement = (renderType == 'element') ? 'checkBoxElement' : 'checkBox'
						out << "$inputElement"(
							description	: ucName,
							name		: prependName + it.escapedName(),
							value		: fieldValue,
							required	: it.isRequired()
						){helpText}
						break
					case ['TEMPLATE']:
						inputElement = (renderType == 'element') ? 'templateElement' : 'templateSelect'
						out << "$inputElement"(
							description	: ucName,
							name		: prependName + it.escapedName(),
							addDummy	: true,
							entity		: it.entity,
							value		: fieldValue,
							required	: it.isRequired()
						){helpText}
						break
					case ['MODULE']:
						inputElement = (renderType == 'element') ? 'selectElement' : 'select'
						out << "$inputElement"(
							description	: ucName,
							name		: prependName + it.escapedName(),
							from		: AssayModule.findAll(),
							value		: fieldValue,
							required	: it.isRequired()
						){helpText}
					break
						break
					default:
						// unsupported field type
						out << '<span class="warning">!' + it.type + '</span>'
						break
				}

				// output column closing element?
				if (renderType == 'column') {
					out << '</div>'
				}
			}
		}
	}

	def PublicationSelectElement = { attrs, body ->
		attrs.description = 'Publications';
		// render list with publications currently available
		baseElement.call(
			'_publicationList',
			attrs,
			body
		)

		attrs.description = '';

		// render 'Add publication button'
		baseElement.call(
			'_publicationAddButton',
			attrs,
			body
		)
	}

	/**
	 * Renders a input box for publications
	 */
	def publicationSelect = { attrs, body ->
		if (attrs.get('value') == null) {
			attrs.value = [];
		}
		if (attrs.get('description') == null) {
			attrs.description = '';
		}
		out << '<form id="' + attrs.name + '_form" onSubmit="return false;">';
		out << textField(
			name: attrs.get("name"),
			value: '',
			rel: 'publication-pubmed',
			style: 'width: 400px;'
		);
		out << '</form>';
		out << '<script type="text/javascript">';
		out << '  var onSelect = function( chooserObject, inputElement, event, ui ) { selectPubMedAdd( chooserObject, inputElement, event, ui ); enableButton( ".' + attrs.name + '_publication_dialog", "Add", true ); };'
		out << '  iField = $( "#' + attrs.get('name') + '" );';
		out << '  new PublicationChooser().initAutocomplete( iField, { "select" : onSelect } );';
		out << '</script>';
	}

	def _publicationList = { attrs, body ->
		def display_none = 'none';
		if (!attrs.get('value') || attrs.get('value').size() == 0) {
			display_none = 'inline';
		}

		// Add a unordered list
		out << '<ul class="publication_list" id="' + attrs.name + '_list">';

		out << '<li>';
		out << '<span class="publication_none" id="' + attrs.name + '_none" style="display: ' + display_none + ';">';
		out << 'No publications selected';
		out << '</span>';
		out << '</li>';

		out << '</ul>';

		// Add the publications using javascript
		out << '<script type="text/javascript">'
		if (attrs.get('value') && attrs.get('value').size() > 0) {
			def i = 0;
			attrs.get('value').each {
				out << 'showPublication( ';
				out << '  "' + attrs.name + '",';
				out << '  ' + it.id + ',';
				out << '  "' + it.title + '",';
				out << '  "' + it.authorsList + '",';
				out << '  ' + i++;
				out << ');';
			}
		}
		out << '</script>';

		def ids;
		if (attrs.get('value') && attrs.get('value').size() > 0) {
			ids = attrs.get('value').id.join(',')
		} else {
			ids = '';
		}
		out << '<input type="hidden" name="' + attrs.name + '_ids" value="' + ids + '" id="' + attrs.name + '_ids">';
	}

	def _publicationAddButton = { attrs, body ->

		// Output the dialog for the publications
		out << '<div id="' + attrs.name + '_dialog">';
		out << '<p>Search for a publication on pubmed. You can search on a part of the title or authors. </p>';
		out << publicationSelect(attrs, body);
		out << '</div>';
		out << '<script type="text/javascript">';
		out << '  createPublicationDialog( "' + attrs.name + '" );'
		out << '</script>';

		out << '<input type="button" onClick="openPublicationDialog(\'' + attrs.name + '\' );" value="Add Publication">';
	}

	def ContactSelectElement = { attrs, body ->

		attrs.description = 'Contacts';
		// render list with publications currently available
		baseElement.call(
			'_contactList',
			attrs,
			body
		)

		attrs.description = '';

		// render 'publications list'
		out << '<div id="' + attrs.name + '_dialog" class="contacts_dialog" style="display: none;">'
		baseElement.call(
			'_personSelect',
			attrs,
			body
		)
		baseElement.call(
			'_roleSelect',
			attrs,
			body
		)
		baseElement.call(
			'_contactAddButtonAddition',
			attrs,
			body
		)
		out << '</div>';

		// render 'Add contact button'
		baseElement.call(
			'_contactAddDialogButton',
			attrs,
			body
		)
	}

	def _contactList = { attrs, body ->
		def display_none = 'none';
		if (!attrs.get('value') || attrs.get('value').size() == 0) {
			display_none = 'inline';
		}

		// Add a unordered list
		out << '<ul class="contact_list" id="' + attrs.name + '_list">';

		out << '<li>';
		out << '<span class="contacts_none" id="' + attrs.name + '_none" style="display: ' + display_none + ';">';
		out << 'No contacts selected';
		out << '</span>';
		out << '</li>';

		out << '</ul>';

		// Add the contacts using javascript
		out << '<script type="text/javascript">'
		if (attrs.get('value') && attrs.get('value').size() > 0) {
			def i = 0;
			attrs.get('value').each {
				out << 'showContact( ';
				out << '  "' + attrs.name + '",';
				out << '  "' + it.person.id + '-' + it.role.id + '",';
				out << '  "' + it.person.lastName + ', ' + it.person.firstName + (it.person.prefix ? ' ' + it.person.prefix : '') + '",';
				out << '  "' + it.role.name + '",';
				out << '  ' + i++;
				out << ');';
			}
		}
		out << '</script>';

		def ids = '';
		if (attrs.get('value') && attrs.get('value').size() > 0) {
			ids = attrs.get('value').collect { it.person.id + '-' + it.role.id }
			ids = ids.join(',');
		}
		out << '<input type="hidden" name="' + attrs.name + '_ids" value="' + ids + '" id="' + attrs.name + '_ids">';
	}

	def _contactAddSelect = { attrs, body ->
		out << _personSelect(attrs) + _roleSelect(attrs);
	}

	def _contactAddButtonAddition = { attrs, body ->
		out << '<input type="button" onClick="if( addContact ( \'' + attrs.name + '\' ) ) { $(\'#' + attrs.name + '_dialog\').hide(); $( \'#' + attrs.name + '_dialogButton\' ).show(); }" value="Add">';
		out << '<input type="button" onClick="$(\'#' + attrs.name + '_dialog\').hide(); $( \'#' + attrs.name + '_dialogButton\' ).show();" value="Close">';
	}

	def _contactAddDialogButton = { attrs, body ->
		out << '<input type="button" onClick="$( \'#' + attrs.name + '_dialog\' ).show(); $(this).hide();" id="' + attrs.name + '_dialogButton" value="Add Contact">';
	}
	/**
	 * Person select element
	 * @param Map attributes
	 */
	def _personSelect = { attrs ->
		def selectAttrs = new LinkedHashMap();

		// define 'from'
		def persons = Person.findAll().sort({ a, b -> a.lastName == b.lastName ? (a.firstName <=> b.firstName) : (a.lastName <=> b.lastName) } as Comparator);
		selectAttrs.from = persons.collect { it.lastName + ', ' + it.firstName + (it.prefix ? ' ' + it.prefix : '') }
		selectAttrs.keys = persons.id;

		// add 'rel' attribute
		selectAttrs.rel = 'person'
		selectAttrs.name = attrs.name + '_person';

		// add a dummy field
		selectAttrs.from.add(0,'')
		selectAttrs.keys.add(0,'')

		out << "Person: " + select(selectAttrs)
	}

	/**
	 * Role select element
	 * @param Map attributes
	 */
	def _roleSelect = { attrs ->
		def selectAttrs = new LinkedHashMap();

		// define 'from'
		def roles = PersonRole.findAll();
		selectAttrs.from = roles.collect { it.name };
		selectAttrs.keys = roles.id;

		// add 'rel' attribute
		selectAttrs.rel = 'role'
		selectAttrs.name = attrs.name + '_role';

		// add a dummy field
		selectAttrs.from.add(0,'')
		selectAttrs.keys.add(0,'')

		out << "Role: " + select(selectAttrs)
	}


	def UserSelectElement = { attrs, body ->
		// render list with publications currently available
		baseElement.call(
			'_userList',
			attrs,
			body
		)

		attrs.description = '';

		// render 'Add user button'
		baseElement.call(
			'_userAddButton',
			attrs,
			body
		)
	}

	/**
	 * Renders an input box for publications
	 */
	def userSelect = { attrs, body ->
		if (attrs.get('value') == null) {
			attrs.value = [];
		}
		if (attrs.get('description') == null) {
			attrs.description = '';
		}
                
		out << '<form id="' + attrs.name + '_form" onSubmit="return false;">';
		out << select(
			name: attrs.get("name"),
			value: '',
                        from: SecUser.list(),
                        optionValue: 'username',
                        optionKey: 'id',
			style: 'width: 400px;'
		);
		out << '</form>';
	}

	def _userList = { attrs, body ->
		def display_none = 'none';
		if (!attrs.get('value') || attrs.get('value').size() == 0) {
			display_none = 'inline';
		}

		// Add a unordered list
		out << '<ul class="user_list" id="' + attrs.name + '_list">';

		out << '<li>';
		out << '<span class="user_none" id="' + attrs.name + '_none" style="display: ' + display_none + ';">';
		out << '-';
		out << '</span>';
		out << '</li>';

		out << '</ul>';

		// Add the publications using javascript
		out << '<script type="text/javascript">'
		if (attrs.get('value') && attrs.get('value').size() > 0) {
			def i = 0;
			attrs.get('value').each {
				out << 'showUser( ';
				out << '  "' + attrs.name + '",';
				out << '  ' + it.id + ',';
				out << '  "' + it.username + '",';
				out << '  ' + i++;
				out << ');';
			}
		}
		out << '</script>';

		def ids;
		if (attrs.get('value') && attrs.get('value').size() > 0) {
			ids = attrs.get('value').id.join(',')
		} else {
			ids = '';
		}
		out << '<input type="hidden" name="' + attrs.name + '_ids" value="' + ids + '" id="' + attrs.name + '_ids">';
	}

	def _userAddButton = { attrs, body ->

		// Output the dialog for the publications
		out << '<div id="' + attrs.name + '_dialog">';
		out << '<p>Select a user from the database.</p>';
		out << userSelect(attrs, body);
		out << '</div>';
		out << '<script type="text/javascript">';
		out << '  createUserDialog( "' + attrs.name + '" );'
		out << '</script>';

		out << '<input type="button" onClick="openUserDialog(\'' + attrs.name + '\' );" value="Add User">';
	}

	def showTemplateField = { attrs, body ->
		def field = attrs.get( 'field' );
		def entity = attrs.get( 'entity' );
		def fieldName = '';
		def fieldType = '';
		def fieldUnit = '';
		
		if( entity ) {
			if( field instanceof String ) {
				fieldName = field;
				fieldType = '';
				fieldUnit = '';
			} else if( field instanceof TemplateField ) {
				fieldName = field.name
				fieldType = field.type.toString();
				fieldUnit = field.unit
			} else {
				return;
			}

			def value = entity.getFieldValue( fieldName );

			// Show a link if the field is a FILE field
			if( fieldType == 'FILE' && value != "" ) {
			    out << '<a href="' + g.createLink( controller: "file", action: "get",  id: value ) + '">' + value + '</a>';
			} else {
				out << value;
			}

			// Show the unit (if a unit is present and a value was shown)
			if( fieldUnit && value != null && value != "" )
				out << " " + fieldUnit

		}
	}

}