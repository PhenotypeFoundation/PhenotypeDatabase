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
<wizard:ajaxButton name="add" value="Add" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="attachHelpTooltips()" />
<input name="addNumber" size="4" maxlength="4" value="1">
subjects of species
<wizard:speciesSelect name="addSpecies" />
<g:if test="${subjects}">
<div class="subjects">
<g:each var="subject" status="i" in="${subjects}">
	<div class="subject<g:if test="${i>0}"> topborder</g:if>">
		<div class="row">subject ${i}</div>
		<div class="row">${subject}</div>
	</div>
</g:each>
</div>
</g:if>
</wizard:pageContent>