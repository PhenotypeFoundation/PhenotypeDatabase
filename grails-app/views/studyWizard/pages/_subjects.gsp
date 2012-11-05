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

def speciesOntologies = new dbnp.studycapturing.Subject().giveDomainFields().find { it.name == 'species' }.ontologies.collect { it.ncboId }
%>
<af:page>
	<span class="info">
		<span class="title">Add subjects to your study</span>
		Describe the subjects studied with all details available. Use the template that contains the necessary fields. New templates can be defined (based on existing templates).
		To add subjects to the study, select the correct species and template, input the number of subjects you want to add, and click 'Add'. They will appear below the 'Add' button.
		As multiple species may be studied within one study, there is no hard link between the template and the species.
		<br/><i>Note that you can edit multiple subjects at once by selecting multiple rows by either ctrl-clicking them or dragging a selection over them in the space between the fields.</i>
		<br/><i>Note that depending on the size of your browser window and the template, additional fields can be reached by the slider at the bottom of the page.</i>		
	</span>

	<af:textFieldElement name="addNumber" description="Number of subjects to add" error="addNumber" value="${values?.addNumber}" size="4" maxlength="4">
		The number of subjects to add to your study
	</af:textFieldElement>
	<af:termElement name="species" description="of species" value="${values?.species}" ontologies="${speciesOntologies.join(',')}" addDummy="true">
		The species of the subjects you would like to add to your study
	</af:termElement>
	<af:templateElement name="template" description="with template" value="${values?.template}" error="template" entity="${dbnp.studycapturing.Subject}" ontologies="${speciesOntologies.join( ',' )}" addDummy="true">
		The template to use for these subjects
	</af:templateElement>
	<af:ajaxButtonElement name="add" value="Add" afterSuccess="onPage()">
	</af:ajaxButtonElement>

	<g:if test="${study.subjects}">
		<g:each var="template" in="${study.giveSubjectTemplates()}">
			<g:set var="showHeader" value="${true}" />
			<h1>${template} template</h1>
			<div class="tableEditor">
			<g:each var="subject" status="s" in="${study.giveSubjectsForTemplate(template)}">
				<g:if test="${showHeader}">
				<g:set var="showHeader" value="${false}" />
				<div class="header">
				  <div class="firstColumn"></div>
				  <af:templateColumnHeaders class="column" entity="${subject}" columnWidths="[Name:200, Species: 150]" />
				</div>
				</g:if>
				<div class="row" identifier="${subject.getIdentifier()}">
					<div class="firstColumn">
						<input type="button" value="" action="delete" class="delete" identifier="${subject.getIdentifier()}" />
					</div>
					<af:templateColumns class="column" entity="${subject}" name="subject_${subject.getIdentifier()}" />
				</div>
			</g:each>
			</div>
		</g:each>
	</g:if>

	<script type="text/javascript">
	$(document).ready(function() {
		if (tableEditor) {
			tableEditor.registerActionCallback('delete', function() {
				var subjects = (this.length>1) ? 'these ' + this.length + ' subjects' : 'this subject';
				if (confirm('are you sure you want to delete ' + subjects +'? Note that samples for ' + subjects + ' will also be deleted!')) {
					$('input[name="do"]').val(this);
					<af:ajaxSubmitJs name="delete" afterSuccess="onPage()" />
				}
			});
		}
	});
	</script>
</af:page>