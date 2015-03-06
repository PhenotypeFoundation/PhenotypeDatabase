<span class="listButtons">
  <img onClick="showTemplateFieldForm( ${templateField.id}); this.blur(); return false;" src="${resource( dir: 'images/icons', file: 'application_form_magnify.png', plugin: 'famfamfam' )}" alt="View template field properties" title="View template field properties">
  <img class="disabled" src="${resource( dir: 'images/icons', file: 'delete.png', plugin: 'famfamfam' )}" alt="Delete this template field" title="Delete this template field">
  <img class="disabled" src="${resource( dir: 'images/icons', file: 'add.png', plugin: 'famfamfam' )}" alt="Add field to template" title="Add field to template">
</span>

<b>${templateField.name}</b>
(<g:if test="${templateField.unit}">${templateField.unit}, </g:if>${templateField.type.name})

<form class="templateField_form" id="templateField_${templateField.id}_form" action="updateField">
	<g:render template="elements/alldisabledFieldForm" model="['templateField': templateField, 'ontologies': ontologies, 'fieldTypes': fieldTypes]"/>
</form>
