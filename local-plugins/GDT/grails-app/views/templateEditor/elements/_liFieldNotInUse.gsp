<span class="listButtons">
  <img onClick="showTemplateFieldForm( ${templateField.id}); this.blur(); return false;" src="${resource( dir: 'images/icons', file: 'application_edit.png', plugin: 'famfamfam' )}" alt="Edit template field properties" title="Edit template field properties">
  <img onClick="if( confirm( 'Are you sure?' ) ) { deleteTemplateField( ${templateField.id} ); }" src="${resource( dir: 'images/icons', file: 'delete.png', plugin: 'famfamfam' )}" alt="Delete this template field" title="Delete this template field">
  <img onClick="addTemplateField( ${templateField.id}, null, true );" src="${resource( dir: 'images/icons', file: 'add.png', plugin: 'famfamfam' )}" alt="Add field to template" title="Add field to template">
</span>

<b>${templateField.name}</b>
(<g:if test="${templateField.unit}">${templateField.unit}, </g:if>${templateField.type.name})

<form class="templateField_form" id="templateField_${templateField.id}_form" action="updateField">
	<g:render template="elements/fieldForm" model="['templateField': templateField, 'ontologies': ontologies, 'fieldTypes': fieldTypes]"/>
</form>
