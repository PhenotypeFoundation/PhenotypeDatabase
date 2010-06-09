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
				$("#templateFields").sortable({
					placeholder: 'ui-state-highlight',
					cancel: '.empty',

					update: updateTemplateFieldPosition
				});
				$("#templateFields").disableSelection();
				//$('#templateFields li').bind('dblclick', showTemplateFormEvent);
			});
		</script>

		<p>Please select a template to edit or create a new template</p>
		<g:form action="index" name="templateChoice">
			<g:hiddenField name="entity" value="${encryptedEntity}" />
			<select name="template" id="templateSelect" onChange="this.form.submit();">
				<option value=""></option>
				<g:each in="${templates}" var="currentTemplate">
					<g:if test="${currentTemplate.id==template?.id}">
						<option selected value="${currentTemplate.id}">${currentTemplate.name}</option>
					</g:if>
					<g:else>
						<option value="${currentTemplate.id}">${currentTemplate.name}</option>
					</g:else>
				</g:each>
			</select>
		</g:form>

		<g:if test="${template}">
			<p>Currently, this template contains the following fields. Drag fields to reorder and double click fields to edit.</p>
			<ul id="templateFields">
				<li class="empty ui-state-default" <g:if test="${template.fields?.size() > 0 }">style='display: none;'</g:if>>This template does not yet contain any fields. Use the 'Add new field' button to add fields.</li>
				<g:render template="elements/all" collection="${template.fields}" />
			</ul>

			<div id="addNew">
				<a href="#" onClick="showTemplateFieldForm( 'templateField_new' ); this.blur(); return false;">
					<b>Add new field</b>
				</a>

				<form class="templateField_form" id="templateField_new_form" action="addField">
					<g:render template="elements/fieldForm" model="['templateField': null, 'fieldTypes': fieldTypes]"/>
					<div class="templateFieldButtons">
						<input type="button" value="Save" onClick="addTemplateField( 'new' );">
						<input type="button" value="Cancel" onClick="hideTemplateFieldForm( 'new' );">
					</div>
				</form>
			</div>
		</g:if>

	</body>
</html>