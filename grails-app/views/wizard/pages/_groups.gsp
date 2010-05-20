<%
/**
 * Subjects page
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
<wizard:pageContent>

	<div class="table">
			<div class="header">
				<div class="column"></div>
				<g:if test="${eventGroups}"><g:each var="eventGroup" status="g" in="${eventGroups}">
				<div class="column">${eventGroup.name}</div>
				</g:each></g:if>
			</div>
	</div>

</wizard:pageContent>