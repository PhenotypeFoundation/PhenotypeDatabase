<g:if test="${fieldIdsInUse.contains(templateField.id)}">
    <g:render template="elements/liFieldInUse" model="['templateField': templateField, 'ontologies': ontologies, 'fieldTypes': fieldTypes]"/>
</g:if>
<g:else>
    <g:render template="elements/liFieldNotInUse" model="['templateField': templateField, 'ontologies': ontologies, 'fieldTypes': fieldTypes]"/>
</g:else>


