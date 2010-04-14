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
 <ul>
  <g:each status="i" var="item" in="${pages}">
	  <li<g:if test="${(i == (page-1))}"> class="active"</g:if>>
		<g:if test="${(i > 0)}"><img src="../images/wizard/arrowR.gif" align="absmiddle" class="arrow"></g:if><g:else>&nbsp;</g:else>
		<span class="content">${i+1}. ${item.title}</span>
		<g:if test="${(i < (pages.size() - 1))}"><img src="../images/wizard/arrowL.gif" align="absmiddle" class="arrow"></g:if><g:else>&nbsp;</g:else>
	  </li>
  </g:each>
 </ul>
</div>