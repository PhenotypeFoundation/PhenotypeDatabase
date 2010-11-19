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
		<meta name="layout" content="${layout}"/>
		<title>template editor</title>
		<script src="${createLinkTo(dir: 'js', file: 'templateEditor.js')}" type="text/javascript"></script>
		<link rel="stylesheet" href="${createLinkTo(dir: 'css', file: 'templateEditor.css')}" />
		<g:if env="production">
			<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.ui.autocomplete.html.min.js')}"></script>
			<script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.min.js')}"></script>
		</g:if><g:else>
			<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.ui.autocomplete.html.js')}"></script>
		  <script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.js')}"></script>
		</g:else>

		<style type="text/css">
		  #content .templateEditorStep { font-size: 0.8em; }
		</style>
	</head>
	<body>

		<script type="text/javascript">
			$(function() {
				// Enable sorting of template fields
				$("#selectedTemplateFields").sortable({
					placeholder: 'ui-state-highlight',
					items: 'li:not(.domain)',
					cancel: '.empty, input, select, button, textarea, form, label',
					connectWith: '.templateFields',
					update: updateTemplateFieldPosition,
					receive: addTemplateFieldEvent,
					remove: removeTemplateFieldEvent,
					start: savePosition
				});


				$("#availableTemplateFields").sortable({
					placeholder: 'ui-state-highlight',
					cancel: '.empty, input, select, button, textarea, form, label',
					connectWith: '.templateFields',
					start: savePosition
				});

				$("#ontologyDialog").dialog({
				  autoOpen: false,
				  title: 'Search for ontology',
				  height: 290,
				  width: 350,
				  modal: true,
				  buttons: {
					  'Add': addOntology,
					  Cancel: function() {
						  $(this).dialog('close');
					  }
				  },
				  close: function() {
				  }

				});

			});
		</script>
		
		<g:form action="template" name="templateChoice">
			<g:hiddenField name="entity" value="${encryptedEntity}" />
			<g:hiddenField name="ontologies" value="${ontologies}" />
			<input type="hidden" name="template" id="templateSelect" value="${template?.id}">
		</g:form>

		<g:if test="${template}">
			<div class="templateEditorStep" id="step2_selectedFields">
				<h3 class="templateName">${template.name} (<a class="switch" href="${createLink(action:'index',params: [ 'entity': encryptedEntity ] + extraparams )}">switch</a>)</h3>

				<p>Currently, this template contains the following fields. Drag fields to reorder. Drag fields to the list of available fields to remove the field from the template.</p>
				<ol id="domainFields" class="templateFields <g:if test="${template.inUse()}">inUse</g:if>">
					<g:render template="elements/domainField" var="domainField" collection="${domainFields}" model="['template':template]"/>
				</ol>
				<ol id="selectedTemplateFields" class="templateFields <g:if test="${template.inUse()}">inUse</g:if>">
					<g:render template="elements/selected" var="templateField" collection="${template.fields}" model="['template':template]"/>
					<% /* NB: this empty field should always be the last in the list! */ %>
					<li class="empty ui-state-default" <g:if test="${template.fields?.size() > 0 }">style='display: none;'</g:if>>This template does not yet contain any fields. Drag a field to this list or use the 'Add field button'.</li>
				</ol>
			</div>
			<div class="templateEditorStep" id="step3_availableFields">
				<h3>Available fields</h3>

				<p>These fields are available for adding to the template. Drag a field to the template to add it.</p>
				<ol id="availableTemplateFields" class="templateFields">
					<g:render template="elements/available" var="templateField" collection="${allFields - template.fields}" />
					<li class="empty ui-state-default" <g:if test="${allFields.size() > template.fields.size()}">style='display: none;'</g:if>>There are no additional fields that can be added. Use the 'Create new field' button to create new fields.</li>
				</ol>

				<div id="addNew">
					<a href="#" onClick="showTemplateFieldForm( 'templateField_new' ); this.blur(); return false;">
						<b>Create new field</b>
					</a>

					<form class="templateField_form" id="templateField_new_form" action="createField">
						<g:render template="elements/fieldForm" model="['templateField': null, 'fieldTypes': fieldTypes, 'encryptedEntity': encryptedEntity, 'is_new': true]"/>
					</form>
				</div>
			</div>
		</g:if>
		<br clear="all" />
		<div id="ontologyDialog">
		  <g:render template="ontologyDialog" />
		</div>

		<div id="wait" class="wait">
		  &nbsp;
		</div>
		<div class="wait_text wait">
		  <img src="<g:resource dir="images" file="spinner.gif" />"> Please wait
		</div>
		</body>
</html>