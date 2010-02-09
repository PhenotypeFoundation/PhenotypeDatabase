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
<wizard:ajaxButton name="add" value="Add" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />
<input name="addNumber" size="4" maxlength="4" value="1">
subjects of species
<wizard:speciesSelect name="addSpecies" />
<g:if test="${subjects}">
<div class="table">
	<div class="header">
		<div class="firstColumn">#</div>
		<div class="column">name</div>
		<div class="column">species</div>
		<wizard:templateColumnHeaders template="${study.template}" class="column" />
	</div>
<g:each var="subject" status="i" in="${subjects}">
	<div class="row">
		<div class="firstColumn">${i}</div>
		<div class="column"><g:textField name="subject_${i}_name" value="${subject.name}" size="12" maxlength="12" /></div>
		<div class="column">
			<wizard:speciesSelect value="${subject.species}" name="subject_${i}_species" />
		</div>
		<wizard:templateColumns id="${i}" template="${study.template}" name="subject_${i}" class="column" subject="${subject}" />
	</div>
</g:each>
</div>
<div class="sliderContainer">
	<div class="slider"/>
</div>
</g:if>
</wizard:pageContent>