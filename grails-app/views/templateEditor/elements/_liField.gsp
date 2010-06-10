<g:if test="${templateField.inUse()}">
	<g:render template="elements/liFieldInUse" model="['templateField': templateField, 'fieldTypes': fieldTypes]"/>
</g:if>
<g:else>
	<g:render template="elements/liFieldNotInUse" model="['templateField': templateField, 'fieldTypes': fieldTypes]"/>
</g:else>