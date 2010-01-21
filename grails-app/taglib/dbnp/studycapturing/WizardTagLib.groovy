package dbnp.studycapturing

import org.codehaus.groovy.grails.plugins.web.taglib.JavascriptTagLib

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
	 *        'webflow' taglib
	 * @param Map attributes
	 * @param Closure body
	 */
	def ajaxButton = { attrs, body ->
		// get the jQuery version
		def jQueryVersion = grailsApplication.getMetadata()['plugins.jquery']

		// fetch the element name from the attributes
		def elementName = attrs['name'].replaceAll(/ /, "_")

		// javascript function to call after success
		def afterSuccess = attrs['afterSuccess']

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

		// replace double semi colons
		button = button.replaceAll(/;{2,}/, '!!!')
		
		// render button
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
	 * Example initial webflow action to work with this javascript:
	 * ...
	 * mainPage {
	 * 	render(view: "/wizard/index")
	 * 	onRender {
	 * 		flow.page = 1
	 * 	}
	 * 	on("next").to "pageOne"
	 * }
	 * ...
	 *
	 * @param Map attributes
	 * @param Closure body
	 */
	def ajaxFlowRedirect = { attrs, body ->
		// define AJAX provider
		setProvider([library: ajaxProvider])

		// generate an ajax button
		def button = this.ajaxButton(attrs, body)

		// strip the button part to only leave the Ajax call
		button = button.replaceFirst(/<[^\"]*\"jQuery.ajax/,'jQuery.ajax')
		button = button.replaceFirst(/return false.*/,'')

		// change form if a form attribute is present
		if (attrs.get('form')) {
			button = button.replaceFirst(/this\.form/,
				"\\\$('" + attrs.get('form') + "')"
			)
		}

		// generate javascript
		out << '<script language="JavaScript">'
		out << '$(document).ready(function() {'
		out << button
		out << '});'
		out << '</script>'
	}

	/**
	 * wizard navigation buttons render wrapper, in order to be able to add
	 * functionality in the future
	 */
	def previousNext = {attrs ->
		// define AJAX provider
		setProvider([library: ajaxProvider])

		// render navigation buttons
		out << render(template: "/wizard/common/buttons")
	}

	/**
	 * render the content of a particular wizard page
	 * @param Map attrs
	 * @param Closure body
	 */
	def pageContent = {attrs, body ->
		// define AJAX provider
		setProvider([library: ajaxProvider])

		// render new body content
		out << render(template: "/wizard/common/tabs")
		out << '<div class="content">'
		out << body()
		out << '</div>'
		out << render(template: "/wizard/common/navigation")
		out << render(template: "/wizard/common/error")
	}

	/**
	 * render a textFieldElement
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

		// render a text element
		out << '<div class="element">'
		out << ' <div class="description">'
		out << attrs.get('description')
		out << ' </div>'
		out << ' <div class="input">'
		out << textField(attrs)
		out << ' </div>'

		// add help icon?
		if (body()) {
			out << ' <div class="help">'
			out << '  <div class="icon"></div>'
			out << '  <div class="content">'
			out << '    ' + body()
			out << '  </div>'
			out << ' </div>'
		}

		out << '</div>'
	}
}