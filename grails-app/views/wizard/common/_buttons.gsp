<%
/**
 * Wizard navigational buttons
 *
 * @author  Jeroen Wesbeek
 * @since   20100113
 * @package wizard
 * @see     dbnp.studycapturing.WizardTagLib::previousNext
 * @see     dbnp.studycapturing.WizardController
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
    <div id="wizardNavigation">
      <g:if test="${button.previous}">&laquo; <wizard:ajaxButton name="previous" value="prev" url="[controller:'wizard',action:'pages']" update="[success:'wizardContent',failure:'wizardError']" /></g:if>
      <g:if test="${button.previous && button.next}">|</g:if>
      <g:if test="${button.next}"><wizard:ajaxButton name="next" value="next" url="[controller:'wizard',action:'pages']" update="[success:'wizardContent',failure:'wizardError']" /> &raquo;</g:if>
    </div>
