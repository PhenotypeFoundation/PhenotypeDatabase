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
	* $Rev$
	* $Author$
	* $Date$
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

		<div class="templateEditorStep" id="step1_template">
			<h3>Select template</h3>
			<p>Showing templates for <b>${humanReadableEntity}</b>.</p>
			<p>Please select a template to edit or create a new template</p>

			<ul id="templates">
				<li class="empty ui-state-default" <g:if test="${templates.size() > 0 }">style='display: none;'</g:if>>There are no templates for ${humanReadableEntity}. Use the 'Add template' button to add fields.</li>
				<g:each in="${templates}" var="currentTemplate">
				  <g:render template="elements/liTemplate" model="['template': currentTemplate]"/>
				</g:each>
			</ul>

			<div id="addNew">
				<a href="#" onClick="editTemplate( 'new' ); this.blur(); return false;">
					<b>Create new template</b>
				</a>

				<form class="templateField_form" id="template_new_form" action="createTemplate">
					<g:hiddenField name="entity" value="${encryptedEntity}" />
					<g:hiddenField name="ontologies" value="${ontologies}" />
					<g:render template="elements/templateForm" model="['template': null]"/>
					<div class="templateFieldButtons">
						<input type="button" value="Save" onClick="createTemplate( 'new' );">
						<input type="button" value="Cancel" onClick="hideTemplateForm( 'new' );">
					</div>
				</form>
			</div>

			<g:form action="template" name="templateChoice" method="GET">
				<g:hiddenField name="entity" value="${encryptedEntity}" />
				<g:hiddenField name="ontologies" value="${ontologies}" />
				<input type="hidden" name="template" id="templateSelect" value="${template?.id}">
			</g:form>
		</div>

		<div id="wait" class="wait">
		  &nbsp;
		</div>
		<div class="wait_text wait">
		  <img src="<g:resource dir="images" file="spinner.gif" />"> Please wait
		</div>


	</body>
</html>