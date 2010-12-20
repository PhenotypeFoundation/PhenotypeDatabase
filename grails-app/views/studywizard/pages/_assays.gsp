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
 * $Rev: 998 $
 * $Author: j.a.m.wesbeek@umail.leidenuniv.nl $
 * $Date: 2010-10-26 17:03:37 +0200 (Tue, 26 Oct 2010) $
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
	</wizard:textFieldElement */ %>
	<wizard:templateElement name="template" description="Template" value="${assay?.template}" entity="${dbnp.studycapturing.Assay}" addDummy="true" ajaxOnChange="switchTemplate" afterSuccess="onPage()" >
		Choose the type of assay you would like to add
	</wizard:templateElement>
	<g:if test="${assay}">
	<wizard:templateElements entity="${assay}" />
	<wizard:ajaxButtonElement name="add" value="Add" afterSuccess="onPage()">
	</wizard:ajaxButtonElement>
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
					<wizard:templateColumnHeaders class="column" entity="${assay}" />
				</div>
				</g:if>

				<div class="row">
					<div class="firstColumn">
						<wizard:ajaxButton name="deleteAssay" src="${resource(dir: 'images/icons', file: 'delete.png', plugin: 'famfamfam')}" alt="delete this assay" class="famfamfam" value="-" before="\$(\'input[name=do]\').val(${assay.getIdentifier()});" afterSuccess="onPage()"/>
					</div>
					<wizard:templateColumns class="column" entity="${assay}" name="assay_${assay.getIdentifier()}" />
				</div>
			</g:each>
			</div>
			<div class="sliderContainer">
				<div class="slider"></div>
			</div>
		</g:each>
	</g:if>
</af:page>