<span class="listButtons">
  <g:if test="${template.inUse()}">
	<img class="disabled" src="${createLinkTo( dir: 'images', file: 'icons/famfamfam/delete.png')}" alt="This field can not be removed from the template, as the template is still in use." title="This field can not be removed from the template, as the template is still in use.">
  </g:if>
  <g:else>
	<img onClick="removeTemplateField( ${templateField.id} ); moveFieldListItem( ${templateField.id}, '#availableTemplateFields' );" src="${createLinkTo( dir: 'images', file: 'icons/famfamfam/delete.png')}" alt="Remove this template field from the template" title="Remove this template field from the template">
  </g:else>
</span>

  <span class="ui-icon ui-icon-arrowthick-2-n-s"></span>
  <b>${templateField.name}</b>
  (<g:if test="${templateField.unit}">${templateField.unit}, </g:if><g:render template="elements/${templateField.type.toString().toLowerCase().replaceAll(/ /,'_')}" model="[templateField: templateField]"/>)
    

