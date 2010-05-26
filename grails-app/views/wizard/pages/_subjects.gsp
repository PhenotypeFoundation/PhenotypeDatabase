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
		Describe the subjects studied with all details available. Use the template that contains the necessary fields. New templates can be defined (based on existing templates).
		To add subjects to the study, select the correct species and template, input the number of subjects you want to add, and click 'Add'. They will appear below the 'Add' button.
		As multiple species may be studied within one study, there is no hard link between the template and the species.
		<br/><i>Note that you can edit multiple subjects at once by selecting multiple rows by either ctrl-clicking them or dragging a selection over them in the space between the fields.</i>
		<br/><i>Note that depending on the size of your browser window and the template, additional fields can be reached by the slider at the bottom of the page.</i>		
	</span>

	<wizard:textFieldElement name="addNumber" description="Number of subjects to add" error="addNumber" value="1" size="4" maxlength="4">
		The number of subjects to add to your study
	</wizard:textFieldElement>
	<wizard:termElement name="species" description="of species" value="" ontologies="1132">
		The species of the subjects you would like to add to your study
	</wizard:termElement>
	<wizard:templateElement name="template" description="with template" value="" error="template" entity="${dbnp.studycapturing.Subject}" >
		The template to use for these subjects
	</wizard:templateElement>
	<wizard:ajaxButtonElement name="add" value="Add" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()">
	</wizard:ajaxButtonElement>


<g:if test="${subjects}">
	<g:each var="sTemplate" in="${subjectTemplates}">
		<g:set var="showHeader" value="${true}" />
		<g:set var="subjectTemplate" value="${sTemplate.getValue()}" />
		<h1>${subjectTemplate.name} template</h1>
		<div class="table">
		<g:each status="i" var="sId" in="${subjectTemplate.subjects}">
		  <g:set var="subjectId" value="${sId.getValue()}" />
		  <g:if test="${subjects[ subjectId ]}">
		  	<g:if test="${showHeader}">
			<g:set var="showHeader" value="${false}" />
			<div class="header">
				<div class="firstColumn">#</div>
				<div class="firstColumn"></div>
				<wizard:templateColumnHeaders entity="${subjects[ subjectId ]}" class="column" />				
			</div>
			</g:if>
			<div class="row">
				<div class="firstColumn">${subjectId + 1}</div>
				<div class="firstColumn">
					<wizard:ajaxButton name="delete" src="../images/icons/famfamfam/delete.png" alt="delete this subject" class="famfamfam" value="-" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" before="\$(\'input[name=do]\').val(${subjectId});" afterSuccess="onWizardPage()" />
				</div>
				<wizard:templateColumns id="${subjectId}" entity="${subjects[ subjectId ]}" template="${subjects[ subjectId ].template}" name="subject_${subjectId}" class="column" subject="${subjects[ subjectId ]}" />
			</div>
		  </g:if>
		</g:each>
		</div>
		<div class="sliderContainer">
			<div class="slider" ></div>
		</div>
	</g:each>
</g:if>

	<g:if env="development">
	<span class="info">
		<span class="known">Known issues</span>
		<ul>
			<li>autocomplete fields (like ontologies) deselect the selected rows and hence don't replicate</li>
			<li>ontology fields should replicate value <i>and</i> hidden fields</li>
			<li>ontology hidden fields should be processed by the back-end as well (not yet implemented)</li>
			<li>ontology fields now show suggestions for <i>all</i> available ontologies. This has to be narrowed down in the future.</li>
			<li>ontology hidden fields should be handled by the taglibrary / controller as well</li>
		</ul>
	</span>
	</g:if>

</wizard:pageContent>