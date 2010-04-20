<%
/**
 * Wizard tab header
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
<div class="tabs">
  <g:each status="i" var="item" in="${pages}"><div class="element<g:if test="${(i == (page-1))}"> active</g:if>">${i+1}. ${item.title}</div></g:each>
</div>