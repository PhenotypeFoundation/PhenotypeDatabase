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
		In this screen you can add subjects to your study based on a given template. Note that the 'species' select will probably
		move to the template as an ontology reference and will not be asked anymore.
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

	<span class="info">
		<span class="title">Known issues</span>
		<ul>
			<li>autocomplete fields (like ontologies) deselect the selected rows and hence don't replicate</li>
			<li>ontology fields should replicate value <i>and</i> hidden fields</li>
			<li>no client-side validation is performed to check the type of an input field actually matched that of the datamodel</li>
			<li>table columns are randomized on view as they are currently not sorted</li>
			<li>ontology hidden fields should be processed by the back-end as well (not yet implemented)</li>
			<li>ontology fields now show suggestions for <i>all</i> available ontologies. This has to be narrowed down in the future.</li>
		</ul>
	</span>

</wizard:pageContent>