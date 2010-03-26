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
 * $Rev: 299 $
 * $Author: duh $
 * $Date: 2010-03-22 14:40:35 +0100 (Mon, 22 Mar 2010) $
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
		button = button.replaceFirst(/<[^\"]*\"jQuery.ajax/, 'jQuery.ajax')
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
		out << render(template: "/query/common/tabs")
		out << '<div class="content">'
		out << body()
		out << '</div>'
		out << render(template: "/query/common/navigation")
		out << render(template: "/query/common/error")
	}

	/**
	 * generate a base form element
	 * @param String inputElement name
	 * @param Map attributes
	 * @param Closure help content
	 */
	def baseElement = {inputElement, attrs, help ->
		// work variables
		def description = attrs.remove('description')
		def addExampleElement = attrs.remove('addExampleElement')
		def addExample2Element = attrs.remove('addExample2Element')

		// got an ajax onchange action?
		def ajaxOnChange = attrs.remove('ajaxOnChange')
		if (ajaxOnChange) {
			if (!attrs.onChange) attrs.onChange = ''

			// add onChange AjaxSubmit javascript
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

		// execute inputElement call
		def renderedElement = "$inputElement"(attrs)

		// if false, then we skip this element
		if (!renderedElement) return false

		// render a form element
		out << '<div class="element">'
		out << ' <div class="description">'
		out << description
		out << ' </div>'
		out << ' <div class="input">'
		out << renderedElement
		if (help()) {
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
		if (help()) {
			out << '  <div class="helpContent">'
			out << '    ' + help()
			out << '  </div>'
		}

		out << '</div>'
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
	 * Template form element
	 * @param Map attributes
	 * @param Closure help content
	 */
	def speciesElement = {attrs, body ->
		// render template element
		baseElement.call(
			'speciesSelect',
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
	 * render a species select element
	 * @param Map attrs
	 */
	def speciesSelect = {attrs ->
		// fetch the speciesOntology
		// note that this is a bit nasty, probably the ontologyName should
		// be configured in a configuration file... --> TODO: centralize species configuration
		def speciesOntology = Ontology.findByName('NCBI Taxonomy')

		// fetch all species
		attrs.from = Term.findAllByOntology(speciesOntology)

		// got a name?
		if (!attrs.name) {
			// nope, use a default name
			attrs.name = 'species'
		}

		out << select(attrs)
	}

	/**
	 * Template form element
	 * @param Map attributes
	 * @param Closure help content
	 */
	def templateElement = {attrs, body ->
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
			out << select(attrs)
		} else {
			// no, return false to make sure this element
			// is not rendered in the template
			return false
		}
	}

	/**
	 * Term form element
	 * @param Map attributes
	 * @param Closure help content
	 */
	def termElement = {attrs, body ->
		// render term element
		baseElement.call(
			'termSelect',
			attrs,
			body
		)
	}

	/**
	 * render a term select element
	 * @param Map attrs
	 */
	def termSelect = {attrs ->
		// fetch all terms
		attrs.from = Term.findAll()	// for now, all terms as we cannot identify terms as being treatment terms...

		// got a name?
		if (!attrs.name) {
			attrs.name = 'term'
		}

		out << select(attrs)
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
	def templateColumnHeaders = {attrs ->
		def template = attrs.remove('template')

		// output table headers for template fields
		template.fields.each() {
			out << '<div class="' + attrs.get('class') + '">' + it + '</div>'
		}
	}

	/**
	 * render table input elements for all subjectFields in a template
	 * @param Map attributes
	 */
	def templateColumns = {attrs, body ->
		def subject = attrs.remove('subject')
		def subjectId = attrs.remove('id')
		def template = attrs.remove('template')
		def intFields = subject.templateIntegerFields
		def stringFields = subject.templateStringFields
		def floatFields = subject.templateFloatFields
		def termFields = subject.templateTermFields

		// output columns for these subjectFields
		template.fields.each() {
			def fieldValue = subject.getFieldValue(it.name)

			// output div
			out << '<div class="' + attrs.get('class') + '">'

			// handle field types
			switch (it.type.toString()) {
				case ['STRING', 'TEXT', 'INTEGER', 'FLOAT', 'DOUBLE']:
					out << textField(
						name: attrs.name + '_' + it.escapedName(),
						value: fieldValue
					)
					break
				case 'STRINGLIST':
					// render stringlist subjectfield
					if (!it.listEntries.isEmpty()) {
						out << select(
							name: attrs.name + '_' + it.escapedName(),
							from: it.listEntries,
							value: fieldValue
						)
					} else {
						out << '<span class="warning">no values!!</span>'
					}
					break
				case 'DATE':
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

					// output a date field (not the 'rel' which makes the
					// javascript front-end bind the jquery-ui datepicker)
					out << textField(
						name: attrs.name + '_' + it.escapedName(),
						value: fieldValue,
						rel: 'date'
					)
					break
				case 'ONTOLOGYTERM':
					// @see http://www.bioontology.org/wiki/index.php/NCBO_Widgets#Term-selection_field_on_a_form
					// @see ontology-chooser.js, table-editor.js
					//out << it.getClass()
					out << textField(
						name: attrs.name + '_' + it.escapedName(),
						value: fieldValue,
						rel: 'ontology-all-name',
						size: 100
					)
					out << hiddenField(
						name: attrs.name + '_' + it.escapedName() + '-concept_id'
					)
					out << hiddenField(
						name: attrs.name + '_' + it.escapedName() + '-ontology_id'
					)
					out << hiddenField(
						name: attrs.name + '_' + it.escapedName() + '-full_id'
					)
					break
				default:
					// unsupported field type
					out << '<span class="warning">!' + it.type + '</span>'
					break
			}

			out << '</div>'
		}
	}

	/**
	 * render form elements based on an entity's template
	 * @param Map attributes
	 * @param String body
	 */
	def templateElements = {attrs ->
		def entity = (attrs.get('entity'))
		def template = (entity && entity instanceof TemplateEntity) ? entity.template : null

		// got a template?
		if (template) {
			// render template fields
			template.fields.each() {
				def fieldValue = entity.getFieldValue(it.name)

				switch (it.type.toString()) {
					case ['STRING', 'TEXT', 'INTEGER', 'FLOAT', 'DOUBLE']:
						out << textFieldElement(
							description: it.name,
							name: it.escapedName(),
							value: fieldValue
						)
						break
					case 'STRINGLIST':
						if (!it.listEntries.isEmpty()) {
							out << selectElement(
								description: it.name,
								name: it.escapedName(),
								from: it.listEntries,
								value: fieldValue
							)
						} else {
							out << '<span class="warning">no values!!</span>'
						}
						break
					case 'ONTOLOGYTERM':
						// @see http://www.bioontology.org/wiki/index.php/NCBO_Widgets#Term-selection_field_on_a_form
						// @see ontology-chooser.js
						out << textFieldElement(
							name: it.escapedName(),
							value: fieldValue,
							rel: 'ontology-all-name',
							size: 100
						)
						out << hiddenField(
							name: it.name + '-concept_id',
							value: fieldValue
						)
						out << hiddenField(
							name: it.escapedName() + '-ontology_id',
							value: fieldValue
						)
						out << hiddenField(
							name: it.escapedName() + '-full_id',
							value: fieldValue
						)
						break
					case 'DATE':
						out << dateElement(
							description: it.name,
							name: it.escapedName(),
							value: fieldValue
						)
						break
					default:
						out << "unkown field type '" + it.type + "'<br/>"
						break
				}
			}
		}
	}
}