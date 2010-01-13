<%
/**
 * Wizard template
 *
 * @author  Jeroen Wesbeek
 * @since   20100113
 * @package wizard
 * @see     dbnp.studycapturing.WizardTagLib
 * @see     dbnp.studycapturing.WizardController
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
  <g:setProvider library="jquery"/>
  <div id="wizard" style="border:1px solid black;">
    <h1>Proof of concept AJAXified Grails Webflow Wizard</h1>
    <g:form action="pages" name="_wizard" >
    <div id="wizardContent" style="border:1px solid blue;">
      <g:render template="pages/one"/>
    </div>
    <div id="wizardError" style="border:1px solid red;">errors go in here</div>
    </g:form>
  </div>