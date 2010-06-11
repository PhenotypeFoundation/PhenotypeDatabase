<g:set var="numUses" value="${templateField.numUses()}" />
<span class="listButtons">
  <img onClick="showTemplateFieldForm( 'templateField_' + ${templateField.id}); this.blur(); return false;" src="${createLinkTo( dir: 'images', file: 'icons/famfamfam/application_edit.png')}" alt="Edit template field properties" title="Edit template field properties">
  <img class="disabled" src="${createLinkTo( dir: 'images', file: 'icons/famfamfam/delete.png')}" alt="Deleting this field is not possible. Field is used in ${numUses} templates." title="Deleting this field is not possible. Field is used in ${numUses} templates.">
  <img onClick="addTemplateField( ${templateField.id}, null, true );" src="${createLinkTo( dir: 'images', file: 'icons/famfamfam/add.png')}" alt="Add field to template" title="Add field to template">
</span>

    <b>${templateField.name}</b>
    (<g:if test="${templateField.unit}">${templateField.unit}, </g:if><g:render template="elements/${templateField.type.toString().toLowerCase().replaceAll(/ /,'_')}" model="[templateField: templateField]"/>)
    
<form class="templateField_form" id="templateField_${templateField.id}_form" action="updateField">
	<g:hiddenField name="id" value="${templateField.id}" />
	<g:hiddenField name="version" value="${templateField.version}" />
	<p class="noEditsPossible">Editing not possible. Field is used in ${numUses} template(s).</p>
	<g:render template="elements/disabledFieldForm" model="['templateField': templateField, 'ontologies': ontologies, 'fieldTypes': fieldTypes]"/>
	<div class="templateFieldButtons">
		<input type="button" value="Close" onClick="hideTemplateFieldForm( ${templateField.id} );">
	</div>
</form>

