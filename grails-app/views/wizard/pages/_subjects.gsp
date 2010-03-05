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
	using the
	<wizard:templateSelect name="template" description="Template" value="${study?.template}" entity="${dbnp.studycapturing.Subject}" />
	template

<g:if test="${subjects}">
	<g:each var="subjectTemplate" in="${subjectTemplates}">
		<h1>${subjectTemplate.getValue().name} template</h1>
		<div class="table">
			<div class="header">
				<div class="firstColumn">#</div>
				<div class="column">name</div>
				<div class="column">species</div>
				<wizard:templateColumnHeaders template="${subjectTemplate.getValue().template}" class="column" />
			</div>	
		<g:each var="subjectId" in="${subjectTemplate.getValue().subjects}">
			<div class="row">
				<div class="firstColumn">${subjectId}</div>
				<div class="column"><g:textField name="subject_${subjectId}_name" value="${subjects[ subjectId ].name}" size="12" maxlength="12" /></div>
				<div class="column">
					<wizard:speciesSelect value="${subjects[ subjectId ].species}" name="subject_${subjectId}_species" />
				</div>
				<wizard:templateColumns id="${subjectId}" template="${subjects[ subjectId ].template}" name="subject_${subjectId}" class="column" subject="${subjects[ subjectId ]}" />				
			</div>
		</g:each>
		</div>
		<div class="sliderContainer">
			<div class="slider" ></div>
		</div>
	</g:each>
</g:if>

</wizard:pageContent>