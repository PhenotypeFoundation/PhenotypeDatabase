<li class="ui-state-default <g:if test="${template.inUse()}">inUse</g:if>" id="templateField_${it.id}">
    <g:render template="elements/liFieldSelected" model="['templateField': it, 'template': template, 'fieldTypes': fieldTypes]"/>
</li>