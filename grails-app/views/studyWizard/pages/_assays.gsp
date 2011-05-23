<%
/**
 * Assays page
 *
 * @author  Jeroen Wesbeek
 * @since   20100817
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
<af:page>
	<span class="info">
		<span class="title">Add assays to your study</span>
		In this step you can define the various assays that were performed within this study.
		The actual (omics) data for these assays should reside in one of the assay modules that is coupled to this database (see Assay Modules).
	</span>

	<% /* wizard:textFieldElement name="addNumber" description="Number of assays to add" error="addNumber" value="${values?.addNumber}" size="4" maxlength="4">
		The number of assays to add to your study
	</af:textFieldElement */ %>
	<af:templateElement name="template" description="Template" value="${(assay && assay instanceof dbnp.studycapturing.Assay) ? assay.template : ''}" entity="${dbnp.studycapturing.Assay}" addDummy="true" ajaxOnChange="switchTemplate" afterSuccess="onPage()" >
		Choose the type of assay you would like to add
	</af:templateElement>
	<g:if test="${assay}">
	<af:templateElements entity="${assay}" />
	<af:ajaxButtonElement name="add" value="Add" afterSuccess="onPage()">
	</af:ajaxButtonElement>
	</g:if>

	<g:if test="${study.assays}">
		<g:each var="template" in="${study.giveAllAssayTemplates()}">
			<g:set var="showHeader" value="${true}" />
			<h1>${template}</h1>
			<div class="tableEditor">
			<g:each var="assay" in="${study.giveAssaysForTemplate(template)}">
				<g:if test="${showHeader}">
				<g:set var="showHeader" value="${false}" />
				<div class="header">
					<div class="firstColumn"></div>
					<div class="firstColumn"></div>
					<af:templateColumnHeaders class="column" entity="${assay}" />
				</div>
				</g:if>

				<div class="row" identifier="${assay.getIdentifier()}">
					<div class="firstColumn">
						<input type="button" value="" action="deleteAssay" class="delete" identifier="${assay.getIdentifier()}" />
					</div>
					<div class="firstColumn">
						<input type="button" value="" action="duplicate" class="clone" identifier="${assay.getIdentifier()}" />
					</div>
					<af:templateColumns class="column" entity="${assay}" name="assay_${assay.getIdentifier()}" />
				</div>
			</g:each>
			</div>
		</g:each>
	</g:if>

	<script type="text/javascript">
	$(document).ready(function() {
		if (tableEditor) {
			tableEditor.registerActionCallback('deleteAssay', function() {
				if (confirm('are you sure you want to delete ' + ((this.length>1) ? 'these '+this.length+' assays?' : 'this assay?'))) {
					$('input[name="do"]').val(this);
					<af:ajaxSubmitJs name="deleteAssay" afterSuccess="onPage()" />
				}
			});
			tableEditor.registerActionCallback('duplicate', function() {
				$('input[name="do"]').val(this);
				<af:ajaxSubmitJs name="duplicate" afterSuccess="onPage()" />
			});
		}
	});
	</script>
</af:page>