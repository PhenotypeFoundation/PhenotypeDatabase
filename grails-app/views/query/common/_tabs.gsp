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
 * $Rev: 299 $
 * $Author: duh $
 * $Date: 2010-03-22 14:40:35 +0100 (Mon, 22 Mar 2010) $
 */
%>
<div class="tabs">
  <g:each status="i" var="item" in="${pages}"><div class="element<g:if test="${(i == (page-1))}"> active</g:if>">${i+1}. ${item.title}</div></g:each>
</div>