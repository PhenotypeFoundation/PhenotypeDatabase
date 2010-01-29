<% def list = [] %>
<g:each in="${protocolInstance.parameters}" > <% list.add( it )%> </g:each>
<% list.sort{ a,b -> a.name <=> b.name }%>

<g:each in="${list}">

<tr class="prop">
    <td valign="top" class="name" width=200 >
    <label for="protocol"><g:message code="${it.name}" /></label>
    </td>
    <td valign="top" class="name">
    <g:textField name="protocolParameter.${it.id}" value="" />
    </td>
</tr>

</g:each>