package dbnp.studycapturing

import org.codehaus.groovy.grails.plugins.web.taglib.JavascriptTagLib
import dbnp.studycapturing.*
import dbnp.data.*
import cr.co.arquetipos.crypto.Blowfish

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
class WizardTagLib extends JavascriptTagLib {
	// define the tag namespace (e.g.: <wizard:action ... />
	static namespace = "wizard"

	// define the AJAX provider to use
	static ajaxProvider = "jquery"

	// define default text field width
	static defaultTextFieldSize = 25;

	/**
	 * ajaxButton tag, this is a modified version of the default
	 * grails submitToRemote tag to work with grails webflows.
	 * Usage is identical to submitToRemote with the only exception
	 * that a 'name' form element attribute is required. E.g.
	 * <wizard:ajaxButton name="myAction" value="myButton ... />
	 *
	 * you can also provide a javascript function to execute after
	 * success. This behaviour differs from the default 'after'
	 * action which always fires after a button press...
	 *
	 * @see http://blog.osx.eu/2010/01/18/ajaxifying-a-grails-webflow/
	 * @see http://www.grails.org/WebFlow
	 * @see http://www.grails.org/Tag+-+submitToRemote
	 * @todo perhaps some methods should be moved to a more generic
	 *        'webflow' taglib or plugin
	 * @param Map attributes
	 * @param Closure body
	 */
	def ajaxButton = {attrs, body ->
		// get the jQuery version
		def jQueryVersion = grailsApplication.getMetadata()['plugins.jquery']

		// fetch the element name from the attributes
		def elementName = attrs['name'].replaceAll(/ /, "_")

		// javascript function to call after success
		def afterSuccess = attrs['afterSuccess']

		// src parameter?
		def src = attrs['src']
		def alt = attrs['alt']

		// generate a normal submitToRemote button
		def button = submitToRemote(attrs, body)

		/**
		 * as of now (grails 1.2.0 and jQuery 1.3.2.4) the grails webflow does
		 * not properly work with AJAX as the submitToRemote button does not
		 * handle and submit the form properly. In order to support webflows
		 * this method modifies two parts of a 'normal' submitToRemote button:
		 *
		 * 1) replace 'this' with 'this.form' as the 'this' selector in a button
		 *    action refers to the button and / or the action upon that button.
		 *    However, it should point to the form the button is part of as the
		 *    the button should submit the form data.
		 * 2) prepend the button name to the serialized data. The default behaviour
		 *    of submitToRemote is to remove the element name altogether, while
		 *    the grails webflow expects a parameter _eventId_BUTTONNAME to execute
		 *    the appropriate webflow action. Hence, we are going to prepend the
		 *    serialized formdata with an _eventId_BUTTONNAME parameter.
		 */
		if (jQueryVersion =~ /^1.([1|2|3]).(.*)/) {
			// fix for older jQuery plugin versions
			button = button.replaceFirst(/data\:jQuery\(this\)\.serialize\(\)/, "data:\'_eventId_${elementName}=1&\'+jQuery(this.form).serialize()")
		} else {
			// as of jQuery plugin version 1.4.0.1 submitToRemote has been modified and the
			// this.form part has been fixed. Consequently, our wrapper has changed as well... 
			button = button.replaceFirst(/data\:jQuery/, "data:\'_eventId_${elementName}=1&\'+jQuery")
		}

		// add an after success function call?
		// usefull for performing actions on success data (hence on refreshed
		// wizard pages, such as attaching tooltips)
		if (afterSuccess) {
			button = button.replaceFirst(/\.html\(data\)\;/, '.html(data);' + afterSuccess + ';')
		}

		// got an src parameter?
		if (src) {
			def replace = 'type="image" src="' + src + '"'

			if (alt) replace = replace + ' alt="' + alt + '"'

			button = button.replaceFirst(/type="button"/, replace)
		}

		// replace double semi colons
		button = button.replaceAll(/;{2,}/, ';')

		// render button
		out << button
	}

	/**
	 * generate a ajax submit JavaScript
	 * @see WizardTagLib::ajaxFlowRedirect
	 * @see WizardTagLib::baseElement (ajaxSubmitOnChange)
	 */
	def ajaxSubmitJs = {attrs, body ->
		// define AJAX provider
		setProvider([library: ajaxProvider])

		// got a function name?
		def functionName = attrs.remove('functionName')
		if (functionName && !attrs.get('name')) {
			attrs.name = functionName
		}

		// generate an ajax button
		def button = this.ajaxButton(attrs, body)

		// strip the button part to only leave the Ajax call
		button = button.replaceFirst(/<[^\"]*onclick=\"/, '')
		button = button.replaceFirst(/return false.*/, '')

		// change form if a form attribute is present
		if (attrs.get('form')) {
			button = button.replaceFirst(/this\.form/,
				"\\\$('" + attrs.get('form') + "')"
			)
		}

		out << button
	}

	/**
	 * generate ajax webflow redirect javascript
	 *
	 * As we have an Ajaxified webflow, the initial wizard page
	 * cannot contain a wizard form, as upon a failing submit
	 * (e.g. the form data does not validate) the form should be
	 * shown again. However, the Grails webflow then renders the
	 * complete initial wizard page into the success div. As this
	 * ruins the page layout (a page within a page) we want the
	 * initial page to redirect to the first wizard form to enter
	 * the webflow correctly. We do this by emulating an ajax post
	 * call which updates the wizard content with the first wizard
	 * form.
	 *
	 * Usage: <wizard:ajaxFlowRedirect form="form#wizardForm" name="next" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" />
	 * form = the form identifier
	 * name = the action to execute in the webflow
	 * update = the divs to update upon success or error
	 *
	 * OR: to generate a JavaScript function you can call yourself, use 'functionName' instead of 'name'
	 *
	 * Example initial webflow action to work with this javascript:
	 * ...
	 * mainPage {* 	render(view: "/wizard/index")
	 * 	onRender {* 		flow.page = 1
	 *}* 	on("next").to "pageOne"
	 *}* ...
	 *
	 * @param Map attributes
	 * @param Closure body
	 */
	def ajaxFlowRedirect = {attrs, body ->
		// generate javascript
		out << '<script type="text/javascript">'
		out << '$(document).ready(function() {'
		out << ajaxSubmitJs(attrs, body)
		out << '});'
		out << '</script>'
	}

	/**
	 * render the content of a particular wizard page
	 * @param Map attrs
	 * @param Closure body  (help text)
	 */
	def pageContent = {attrs, body ->
		// define AJAX provider
		setProvider([library: ajaxProvider])

		// render new body content
		//	- this JavaScript variable is used by baseElement to workaround an IE
		//	  specific issue (double submit on onchange events). The hell with IE!
		//	  @see baseElement
		out << '<script type="text/javascript">var lastRequestTime = 0;</script>'
		out << render(template: "/wizard/common/tabs")
		out << '<div class="content">'
		out << body()
		out << '</div>'
		out << render(template: "/wizard/common/navigation")
		out << render(template: "/wizard/common/error")
	}

	/**
	 * generate a base form element
	 * @param String inputElement name
	 * @param Map attributes
	 * @param Closure help content
	 */
	def baseElement = {inputElement, attrs, help ->
println ".rendering [" + inputElement + "] with name [" + attrs.get('name') + "] and value [" + ((attrs.value) ? attrs.get('value').toString() : "-") + "]"
		// work variables
		def internetExplorer = (request.getHeader("User-Agent") =~ /MSIE/)
		def description = attrs.remove('description')
		def addExampleElement = attrs.remove('addExampleElement')
		def addExample2Element = attrs.remove('addExample2Element')
		def helpText = help().trim()

		// got an ajax onchange action?
		def ajaxOnChange = attrs.remove('ajaxOnChange')
		if (ajaxOnChange) {
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

		// execute inputElement call
		def renderedElement = "$inputElement"(attrs)

		// if false, then we skip this element
		if (!renderedElement) return false

		// render a form element
		if (attrs.get('elementId')) {
		out << '<div class="element" id="'+ attrs.remove('elementId') +'">'
		} else {
			out << '<div class="element">'
		}
		out << ' <div class="description">'
		out << description
		out << ' </div>'
		out << ' <div class="input">'
		out << renderedElement
		if (helpText.size() > 0) {
			out << '	<div class="helpIcon"></div>'
		}

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
	def textFieldElement = {attrs, body ->
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
	def textAreaElement = {attrs, body ->
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
	def selectElement = {attrs, body ->
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
	def checkBoxElement = {attrs, body ->
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
	def dateElement = {attrs, body ->
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
	def timeElement = {attrs, body ->
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
	def buttonElement = {attrs, body ->
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
            out << '<script type="text/javascript">';
            out << '  $(document).ready( function() { ';
            out << '    var filename = "' + attrs.value + '";';
            out << '    fileUploadField( "' + attrs.name + '" );';
            out << '    if( filename != "" ) {';
            out << '      $("#' + attrs.name + 'Example").html("Current file: " + createFileHTML( filename ) )';
            out << '    }';
            out << '  } );';
            out << "</script>\n";
        }

	/**
	 * Term select element
	 * @param Map attributes
	 */
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
					ontologyList += ontology.ncboId + ","

					Term.findAllByOntology(ontology).each() {
						from[ from.size() ] = it.name
					}

					// strip trailing comma
					attrs.ontologies = ontologyList[0..-2]
				}
			}

			// sort alphabetically
			from.sort()
			
			// define 'from'
			attrs.from = from

			// add 'rel' attribute
			attrs.rel = 'term'

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
		// for now, just fetch all studies
		attrs.from = Study.findAll()

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
	def templateElement = {attrs, body ->
		// add a rel element if it does not exist
		if (!attrs.rel) {
			attrs.rel = 'template'
		}
		
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
	def templateSelect = {attrs ->
		def entity = attrs.remove('entity')

		// add the entity class name to the element
		// do we have crypto information available?
		if (grailsApplication.config.crypto) {
			// generate a Blowfish encrypted and Base64 encoded string.
			attrs['entity'] = Blowfish.encryptBase64(
				entity.toString().replaceAll(/^class /, ''),
				grailsApplication.config.crypto.shared.secret
			)
		} else {
			// base64 only; this is INSECURE! As this class
			// is instantiated elsewehere. Possibly exploitable!
			attrs['entity'] = entity.toString().replaceAll(/^class /, '').bytes.encodeBase64()
		}

		// fetch templates
		if (attrs.remove('addDummy')) {
			attrs.from = ['']
			if (entity && entity instanceof Class) {
				Template.findAllByEntity(entity).each() {
					attrs.from[attrs.from.size()] = it
				}
			}
		} else {
			attrs.from = (entity) ? Template.findAllByEntity(entity) : Template.findAll()
		}

		// got a name?
		if (!attrs.name) {
			attrs.name = 'template'
		}

		// got result?
		if (attrs.from.size() > 0) {
			// transform all values into strings
			def from = []
			attrs.from.each { from[ from.size() ] = it.toString() }

			// sort alphabetically
			from.sort()

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
	 * Protocol form element
	 * @param Map attributes
	 * @param Closure help content
	 */
	def protocolElement = {attrs, body ->
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
	def protocolSelect = {attrs ->
		// fetch all protocold
		attrs.from = Protocol.findAll()	// for now, all protocols

		// got a name?
		if (!attrs.name) {
			attrs.name = 'protocol'
		}

		out << select(attrs)
	}

	def show = {attrs ->
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

		// got a template?
		if (template) {
			// render template fields
			entity.giveFields().each() {
				def ucName		= it.name[0].toUpperCase() + it.name.substring(1)

				out << '<div class="' + attrs.get('class') + '">' + ucName + (it.unit ? " (${it.unit})" : '')
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
					case ['STRING', 'INTEGER', 'FLOAT', 'DOUBLE']:
						inputElement = (renderType == 'element') ? 'textFieldElement' : 'textField'
						out << "$inputElement"(
							description: ucName,
							name: prependName + it.escapedName(),
							value: fieldValue
						){helpText}
						break
					case 'TEXT':
						inputElement = (renderType == 'element') ? 'textAreaElement' : 'textField'
						out << "$inputElement"(
							description: ucName,
							name: prependName + it.escapedName(),
							value: fieldValue
						){helpText}
						break
					case 'STRINGLIST':
						inputElement = (renderType == 'element') ? 'selectElement' : 'select'
						if (!it.listEntries.isEmpty()) {
							out << "$inputElement"(
								description: ucName,
								name: prependName + it.escapedName(),
								from: it.listEntries,
								value: fieldValue
							){helpText}
						} else {
							out << '<span class="warning">no values!!</span>'
						}
						break
					case 'ONTOLOGYTERM':
						// @see http://www.bioontology.org/wiki/index.php/NCBO_Widgets#Term-selection_field_on_a_form
						// @see ontology-chooser.js
						inputElement = (renderType == 'element') ? 'termElement' : 'termSelect'

						if (it.ontologies) {
							out << "$inputElement"(
								description	: ucName,
								name		: prependName + it.escapedName(),
								value		: fieldValue.toString(),
								ontologies	: it.ontologies
							){helpText}
						} else {
							out << "$inputElement"(
								description	: ucName,
								name		: prependName + it.escapedName(),
								value		: fieldValue.toString()
							){helpText}
						}
						break
					case 'ONTOLOGYTERM-old':
						// @see http://www.bioontology.org/wiki/index.php/NCBO_Widgets#Term-selection_field_on_a_form
						// @see ontology-chooser.js
						inputElement = (renderType == 'element') ? 'textFieldElement' : 'textField'
						out << "$inputElement"(
							name: prependName + it.escapedName(),
							value: fieldValue,
							rel: 'ontology-all',
							size: 100
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
							description: ucName,
							name: prependName + it.escapedName(),
							value: fieldValue,
							rel: 'date'
						){helpText}
						break
					case ['RELTIME']:
						inputElement = (renderType == 'element') ? 'textFieldElement' : 'textField'
						out << "$inputElement"(
							description: ucName,
							name: prependName + it.escapedName(),
							value: new RelTime( fieldValue ).toString(),
                                                        addExampleElement: true,
                                                        onBlur: 'showExampleReltime(this)'
						){helpText}
						break
					case ['FILE']:
						inputElement = (renderType == 'element') ? 'fileFieldElement' : 'fileField'
						out << "$inputElement"(
							description: ucName,
							name: prependName + it.escapedName(),
							value: fieldValue ? fieldValue : "",
                                                        addExampleElement: true
						){helpText}
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
}