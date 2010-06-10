<g:set var="numUses" value="${templateField.numUses()}" />
<span class="listButtons">
  <img class="disabled" src="${createLinkTo( dir: 'images', file: 'icons/famfamfam/application_edit.png')}" alt="Editing not possible. Field is used in ${numUses} templates." title="Editing not possible. Field is used in ${numUses} templates.">
  <img class="disabled" src="${createLinkTo( dir: 'images', file: 'icons/famfamfam/delete.png')}" alt="Deleting this field is not possible. Field is used in ${numUses} templates." title="Deleting this field is not possible. Field is used in ${numUses} templates.">
  <img onClick="addTemplateField( ${templateField.id} ); moveFieldListItem( ${templateField.id}, '#selectedTemplateFields' );" src="${createLinkTo( dir: 'images', file: 'icons/famfamfam/add.png')}" alt="Add field to template" title="Add field to template">
</span>

    <b>${templateField.name}</b>
    (<g:if test="${templateField.unit}">${templateField.unit}, </g:if><g:render template="elements/${templateField.type.toString().toLowerCase().replaceAll(/ /,'_')}" model="[templateField: templateField]"/>)
    

