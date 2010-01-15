package dbnp.studycapturing
import org.codehaus.groovy.grails.plugins.web.taglib.JavascriptTagLib

/**
 * Wizard tag library
 *
 * @author  Jeroen Wesbeek
 * @since   20100113
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
   * @see   http://www.grails.org/WebFlow
   * @see   http://www.grails.org/Tag+-+submitToRemote
   * @todo  perhaps some methods should be moved to a more generic
   *        'webflow' taglib
   * @param Map     attributes
   * @param Closure body
   */
  def ajaxButton = { attrs, body ->
    // fetch the element name from the attributes
    def elementName = attrs['name'].replaceAll(/ /,"_")
    
    // generate a normal submitToRemote button
    def button = submitToRemote(attrs,body)

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
    button = button.replaceFirst(/data\:jQuery\(this\)\.serialize\(\)/, "data:\'_eventId_${elementName}=1&\'+jQuery(this.form).serialize()")

    // render button
    out << button
  }

  /**
   * wizard navigation buttons render wrapper, in order to be able to add
   * functionality in the future
   */
  def previousNext = { attrs ->
    // define AJAX provider
    setProvider([library:ajaxProvider])

    // render navigation buttons
    out << render(template:"/wizard/common/buttons")
  }

  /**
   * render the content of a particular wizard page
   * @param Map     attrs
   * @param Closure body
   */
  def pageContent = { attrs, body ->
    // define AJAX provider
    setProvider([library:ajaxProvider])

    // render new body content
    out << render(template:"/wizard/common/tabs")
    out << '<div class="content">'
    out << body()
    out << '</div>'
    out << render(template:"/wizard/common/navigation")
  }

  def textFieldElement = { attrs, body ->
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
    out << body()
    out << ' </div>'
    out << ' <div class="input">'
    out << textField(attrs)
    out << ' </div>'

    // add help icon?
    if (attrs.get('help')) {
      out << ' <div class="help">'
      out << '  <div class="icon"><img src="../images/icons/famfamfam/help.png"></div>'
      out << '  <div class="content">'
      out << '   <div class="text">'
      out << '    '+attrs.get('help')
      out << '   </div>'
      out << '  </div>'
      out << ' </div>'
    }
    
    out << '</div>'
  }
}