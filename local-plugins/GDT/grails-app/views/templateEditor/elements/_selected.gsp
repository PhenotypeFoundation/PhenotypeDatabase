<g:if test='${templateadmin}'>
  <li class="ui-state-default <g:if test="${templateField.required}">required</g:if>" id="templateField_${templateField.id}">
    <g:render template="elements/liFieldSelected" model="['templateField': templateField, 'template': template, 'ontologies': ontologies, 'fieldTypes': fieldTypes]"/>
  </li>
</g:if>

<g:else>
  <li class="ui-state-default" id="templateField_${templateField.id}">
    <g:render template="elements/liFieldNonEditable" model="['templateField': templateField, 'template': template, 'ontologies': ontologies, 'fieldTypes': fieldTypes]"/>
  </li>
</g:else>
