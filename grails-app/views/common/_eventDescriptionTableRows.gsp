
<!-- Rows for a two column table -->




        <tr class="prop">
            <td valign="top" class="name">
              <label for="protocol"><g:message code="eventDescription.protocol.label" default="Protocol" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: description, field: 'protocol', 'errors')}">
                <g:select name="protocol.id" id="protocol" from="${dbnp.studycapturing.Protocol.list()}" optionKey="id" optionValue="${{it.id}}" value="${description?.protocol?.id}" onchange="${remoteFunction(action:'showMyProtocol', controller:'eventDescription', id:description.id, update:'preview', onComplete:'Effect.Appear(preview)', params:'\'protocol=\' + this.value' )}" />
            </td>
        </tr>


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


	<!-- changes here on select -->
	<tbody id="preview">
	    <g:each in="${description.protocol.values}">

                 <tr class="prop">
                 <td valign="top" class="name">
                 <label for="protocolInstance"><g:message code="${it.protocolParameter.name}" /></label>
                 </td>

                 <td valign="top" class="name">
                 <g:textField name="protocolInstance.${it.id}" value="${it.value}" />
                 </td>

                 </tr>
            </g:each>
	</tbody>
