<g:each in="${protocolInstance.values}">

<tr class="prop">

    <td valign="top" class="name">
    <label for="protocolInstance"><g:message code="${it.protocolParameter.name}" /></label>
    </td>

    <td valign="top" class="name">
     <g:textField name="protocolInstance.${it.id}" value="${it.value}" />
     </td>

</tr>


</g:each>