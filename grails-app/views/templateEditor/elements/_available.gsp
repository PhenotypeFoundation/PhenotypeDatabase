<li class="ui-state-default <g:if test="${templateField.required}">required</g:if>" id="templateField_${templateField.id}">
    <g:render template="elements/liField" model="['templateField': templateField, 'ontologies': ontologies, 'fieldTypes': fieldTypes]"/>
</li>