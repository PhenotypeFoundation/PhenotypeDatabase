<span class="listButtons">
  <img onClick="showTemplateFieldForm( 'templateField_' + ${templateField.id}); this.blur(); return false;" src="${createLinkTo( dir: 'images', file: 'icons/famfamfam/application_edit.png')}" alt="Edit template field properties" title="Edit template field properties">
  <img onClick="if( confirm( 'Are you sure?' ) ) { deleteTemplateField( ${templateField.id} ); }" src="${createLinkTo( dir: 'images', file: 'icons/famfamfam/delete.png')}" alt="Delete this template field" title="Delete this template field">
  <img onClick="addTemplateField( ${templateField.id} ); moveFieldListItem( ${templateField.id}, '#selectedTemplateFields' );" src="${createLinkTo( dir: 'images', file: 'icons/famfamfam/add.png')}" alt="Add field to template" title="Add field to template">
</span>

<b>${templateField.name}</b>
(<g:if test="${templateField.unit}">${templateField.unit}, </g:if><g:render template="elements/${templateField.type.toString().toLowerCase().replaceAll(/ /,'_')}" model="[templateField: templateField]" />)

<form class="templateField_form" id="templateField_${templateField.id}_form" action="updateField">
	<g:hiddenField name="id" value="${templateField.id}" />
	<g:hiddenField name="version" value="${templateField.version}" />
	<g:render template="elements/fieldForm" model="['templateField': templateField, 'fieldTypes': fieldTypes]"/>
	<div class="templateFieldButtons">
		<input type="button" value="Save" onClick="updateTemplateField( ${templateField.id} );">
		<input type="button" value="Close" onClick="hideTemplateFieldForm( ${templateField.id} );">
	</div>
</form>
