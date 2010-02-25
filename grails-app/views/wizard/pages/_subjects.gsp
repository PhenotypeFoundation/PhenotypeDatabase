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
	<span class="info">
		<span class="title">Add subjects to your study</span>
		Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce laoreet leo nec leo vehicula quis scelerisque elit pulvinar. Vivamus arcu dui, adipiscing eu vestibulum id, consectetur et erat. Aenean risus mauris, placerat et lacinia vulputate, commodo eget ligula. Pellentesque ornare blandit metus ac dictum. Donec scelerisque feugiat quam, a congue ipsum malesuada nec. Donec vulputate, diam eget porta rhoncus, est mauris ullamcorper turpis, vitae dictum risus justo quis justo. Aenean blandit feugiat accumsan. Donec porttitor bibendum elementum.
	</span>

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