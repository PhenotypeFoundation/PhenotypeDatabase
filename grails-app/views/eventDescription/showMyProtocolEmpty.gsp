<g:each in="${protocol.parameters}">

<tr class="prop">

    <td valign="top" class="name">
    <label for="startTime"><g:message code="${it.name}" /></label>
    </td>

    <td valign="top" class="name">
     <g:textField id="protocolParameterValue.${it.id}" value="${""}" />
     </td>

</tr>


</g:each>