<span class="listButtons">
  <img onClick="showTemplateFieldForm( 'templateField_' + ${templateField.id}); this.blur(); return false;" src="${createLinkTo( dir: 'images/icons', file: 'application_edit.png', plugin: 'famfamfam' )}" alt="Show template field properties" title="Show template field properties">

  <g:if test="${template.inUse()}">
	<img class="disabled" src="${createLinkTo( dir: 'images/icons', file: 'delete.png', plugin: 'famfamfam' )}" alt="This field can not be removed from the template, as the template is still in use." title="This field can not be removed from the template, as the template is still in use.">
  </g:if>
  <g:else>
	<img onClick="removeTemplateField( ${templateField.id} ); moveFieldListItem( ${templateField.id}, '#availableTemplateFields' );" src="${createLinkTo( dir: 'images/icons', file: 'delete.png', plugin: 'famfamfam' )}" alt="Remove this template field from the template" title="Remove this template field from the template">
  </g:else>
</span>

  <span class="ui-icon ui-icon-arrowthick-2-n-s"></span>
  <b>${templateField.name}</b>
  (<g:if test="${templateField.unit}">${templateField.unit}, </g:if><g:render template="elements/${templateField.type.toString().toLowerCase().replaceAll(/ /,'_')}" model="[templateField: templateField]"/>)

  <form class="templateField_form" id="templateField_${templateField.id}_form">
	  <g:render template="elements/disabledFieldForm" model="['templateField': templateField, 'ontologies': ontologies, 'fieldTypes': fieldTypes]"/>
	  <div class="templateFieldButtons">
		  <input type="button" value="Close" onClick="hideTemplateFieldForm( ${templateField.id} );">
	  </div>
  </form>