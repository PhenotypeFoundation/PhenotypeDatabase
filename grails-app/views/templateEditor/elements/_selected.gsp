<li class="ui-state-default  <g:if test="${templateField.required}">required</g:if>" id="templateField_${templateField.id}">
  <g:render template="elements/liFieldSelected" model="['templateField': templateField, 'template': template, 'ontologies': ontologies, 'fieldTypes': fieldTypes]"/>
</li>