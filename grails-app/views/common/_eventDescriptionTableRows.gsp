<!-- Rows for a two column table -->

        <tr class="prop">
            <td valign="top" class="name">
              <label for="name"><g:message code="eventDescription.name.label" default="Name" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: description, field: 'name', 'errors')}">
                <g:textField name="name" value="${description?.name}" />
            </td>
        </tr>


        <tr class="prop">
            <td valign="top" class="name">
              <label for="description"><g:message code="eventDescription.description.label" default="Description" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: description, field: 'description', 'errors')}">
                <g:textArea name="description" value="${description?.description}" cols="40" rows="6" />
            </td>
        </tr>


	<!-- select -->

        <tr class="prop">

            <td valign="top" class="name" width=200 >
              <label for="protocol"><g:message code="eventDescription.protocol.label" default="Protocol" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: description, field: 'protocol', 'errors')}">
                <g:select name="protocol.id" id="protocol" from="${dbnp.studycapturing.Protocol.list()}" optionKey="id" optionValue="${{it.name}}" value="${{it?.id}}" onchange="${remoteFunction(action:'showMyProtocol', controller:'eventDescription', update:'preview', onComplete:'Effect.Appear(preview)', params:'\'protocolid=\' + this.value', id:params['id'])}" />
            </td>
        </tr>


	<!-- this part changes dynamiccally on select -->

	<tbody id="preview">
            <% def list = [] %>
            <g:each in="${description.protocol.parameters}" > <% list.add( it )%> </g:each>
            <% list.sort{ a,b -> a.name <=> b.name }%>

	    <g:each in="${list}">

                 <tr class="prop">
                 <td valign="top" class="name" width=200>
                 <label for="parameter"><g:message code="${it.name}" /></label>
                 </td>

                 <td valign="top" class="name">
                 <g:textField name="protocolParameter.${it.id}" value="${eventInstance.parameterStringValues[it.name]}" />
                 </td>
                 </tr>

            </g:each>
	</tbody>
