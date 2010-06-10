<%
	/**
	* Template Editor overview template
	*
	* @author Jeroen Wesbeek
	* @since 20100422
	* @package wizard
	* @see dbnp.studycapturing.TemplateEditorController
	*
	* Revision information:
	* $Rev: 428 $
	* $Author: duh $
	* $Date: 2010-05-18 11:09:55 +0200 (di, 18 mei 2010) $
	*/
%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<meta name="layout" content="dialog"/>
		<title>template editor</title>
		<script src="${createLinkTo(dir: 'js', file: 'templateEditor.js')}" type="text/javascript"></script>
		<link rel="stylesheet" href="${createLinkTo(dir: 'css', file: 'templateEditor.css')}" />
	</head>
	<body>

		<script type="text/javascript">
			$(function() {
				// Enable sorting of template fields
				$("#selectedTemplateFields").sortable({
					placeholder: 'ui-state-highlight',
					cancel: '.empty, input, select, button, textarea, form, label',
					connectWith: '.templateFields',
					update: updateTemplateFieldPosition,
					remove: removeTemplateFieldEvent,
					receive: addTemplateFieldEvent
				}).disableSelection();


				$("#availableTemplateFields").sortable({
					placeholder: 'ui-state-highlight',
					cancel: '.empty, input, select, button, textarea, form, label',
					connectWith: '.templateFields'
				}).disableSelection();
				
			});
		</script>
		
		<g:form action="template" name="templateChoice">
			<g:hiddenField name="entity" value="${encryptedEntity}" />
			<input type="hidden" name="template" id="templateSelect" value="${template?.id}">
		</g:form>

		<g:if test="${template}">
			<div class="templateEditorStep" id="step2_selectedFields">
				<h3 class="templateName">${template.name} (<a class="switch" href="${createLink(action:'index')}?entity=${encryptedEntity}">switch</a>)</h3>

				<p>Currently, this template contains the following fields. Drag fields to reorder. Drag fields to the list of available fields to remove the field from the template.</p>
				<ol id="selectedTemplateFields" class="templateFields">
					<li class="empty ui-state-default" <g:if test="${template.fields?.size() > 0 }">style='display: none;'</g:if>>This template does not yet contain any fields. Drag a field to this list or use the 'Add field button'.</li>
					<g:render template="elements/selected" collection="${template.fields}" model="['template':template]"/>
				</ol>
			</div>
			<div class="templateEditorStep" id="step3_availableFields">
				<h3>Available fields</h3>

				<p>These fields are available for adding to the template. Drag a field to the template to add it.</p>
				<ol id="availableTemplateFields" class="templateFields">
					<li class="empty ui-state-default" <g:if test="${allFields.size() > template.fields.size()}">style='display: none;'</g:if>>There are no additional fields that can be added. Use the 'Create new field' button to create new fields.</li>
					<g:render template="elements/available" collection="${allFields - template.fields}" />
				</ol>

				<div id="addNew">
					<a href="#" onClick="showTemplateFieldForm( 'templateField_new' ); this.blur(); return false;">
						<b>Create new field</b>
					</a>

					<form class="templateField_form" id="templateField_new_form" action="createField">
						<g:hiddenField name="entity" value="${encryptedEntity}" />
						<g:render template="elements/fieldForm" model="['templateField': null, 'fieldTypes': fieldTypes]"/>
						<div class="templateFieldButtons">
							<input type="button" value="Save" onClick="createTemplateField( 'new' );">
							<input type="button" value="Cancel" onClick="hideTemplateFieldForm( 'new' );">
						</div>
					</form>
				</div>
			</div>
		</g:if>


	</body>
</html>