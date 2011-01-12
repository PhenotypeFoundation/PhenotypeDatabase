<span class="listButtons">
  <img onClick="showTemplateFieldForm( 'templateField_' + ${templateField.id}); this.blur(); return false;" src="${resource( dir: 'images/icons', file: 'application_edit.png', plugin: 'famfamfam' )}" alt="Show template field properties" title="Show template field properties">
  <g:if test="${templateField.isFilledInTemplate(template)}">
	<img class="disabled" src="${resource( dir: 'images/icons', file: 'delete.png', plugin: 'famfamfam' )}" alt="This field can not be removed from the template, as this field is already in use." title="This field can not be removed from the template, as this field is already in use.">
  </g:if>
  <g:else>
	<img onClick="removeTemplateField( ${templateField.id} ); moveFieldListItem( ${templateField.id}, '#availableTemplateFields' );" src="${resource( dir: 'images/icons', file: 'delete.png', plugin: 'famfamfam' )}" alt="Remove this template field from the template" title="Remove this template field from the template">
  </g:else>
</span>

  <span class="ui-icon ui-icon-arrowthick-2-n-s"></span>
  <b>${templateField.name}</b>
  (<g:if test="${templateField.unit}">${templateField.unit}, </g:if>${templateField.type.name})

  <form class="templateField_form" id="templateField_${templateField.id}_form" action="updateField">
	  <g:if test="${templateField.isEditable()}">
		<g:render template="elements/fieldForm" model="['templateField': templateField, 'template': template, 'is_selected': true, 'ontologies': ontologies, 'fieldTypes': fieldTypes]" />
	  </g:if>
	<g:else>
		<g:render template="elements/disabledFieldForm" model="['templateField': templateField, 'template': template, 'is_selected': true, 'ontologies': ontologies, 'fieldTypes': fieldTypes]"/>
	</g:else>
  </form>