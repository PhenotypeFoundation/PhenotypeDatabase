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
   * @param map     attributes
   * @param string  body
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
   * render the wizard navigation button
   * @param map attrs (supports: previous="true/false" and next="true/false"
   */
  def previousNext = { attrs ->
    def buttons = new LinkedHashMap()
    buttons.previous  = (attrs.get('previous') == null || (attrs.get('previous') instanceof String && attrs.get('previous') == "true")) ? true : false
    buttons.next      = (attrs.get('next') == null || (attrs.get('next') instanceof String && attrs.get('next') == "true")) ? true : false

    out << render(template:"/wizard/common/buttons", model:[button:buttons])
  }
}