<g:set var="templateFieldService" bean="templateFieldService"/>
<g:set var="numUses" value="${templateFieldService.numUses(templateField)}" />
<span class="listButtons">
  <img onClick="showTemplateFieldForm( ${templateField.id}); this.blur(); return false;" src="${resource( dir: 'images/icons', file: 'application_edit.png', plugin: 'famfamfam' )}" alt="Edit template field properties" title="Edit template field properties">
  <img class="disabled" src="${resource( dir: 'images/icons', file: 'delete.png', plugin: 'famfamfam' )}" alt="Deleting this field is not possible. Field is used in ${numUses} templates." title="Deleting this field is not possible. Field is used in ${numUses} templates.">
  <img onClick="addTemplateField( ${templateField.id}, null, true );" src="${resource( dir: 'images/icons', file: 'add.png', plugin: 'famfamfam' )}" alt="Add field to template" title="Add field to template">
</span>

    <b>${templateField.name}</b>
    (<g:if test="${templateField.unit}">${templateField.unit}, </g:if>${templateField.type.name})
    
<form class="templateField_form" id="templateField_${templateField.id}_form" action="updateField">
  <g:if test="${ templateField?.type.toString() == 'STRINGLIST' || templateField?.type.toString() == 'EXTENDABLESTRINGLIST' }">
    <p class="noEditsPossible">You can only add or remove list items that are not used. Field is used in ${numUses} template(s).</p>
  </g:if>
  <g:else>
    <g:if test="${templateField?.type.toString() == 'ONTOLOGYTERM' }">
    <p class="noEditsPossible">You can only add or remove ontologies that are not used. Field is used in ${numUses} template(s).</p>
	</g:if>
	<g:else>
	  <p class="noEditsPossible">Editing not possible. Field is used in ${numUses} template(s).</p>
	</g:else>
  </g:else>
  <g:render template="elements/disabledFieldForm" model="['templateField': templateField, 'ontologies': ontologies, 'fieldTypes': fieldTypes]"/>
</form>

