







            <% def list = [] %>
            <g:each in="${description.protocol.parameters}" > <% list.add( it )%> </g:each>
            <% list.sort{ a,b -> a.name <=> b.name }%>



	    <g:each in="${list}" var="${parameter}">

                 <tr class="prop">
                 <td valign="top" class="name" width=200>
                 <label for="parameter"><g:message code="${parameter.name}" /></label>
                 </td>

                 <td valign="top" class="name">

                 <g:if test="${parameter.type==dbnp.studycapturing.ProtocolParameterType.STRINGLIST}">
                          <g:select name="parameterValue.${parameter.id}" id="protocol" from="${parameter.listEntries}" optionKey="id" optionValue="${{it.name}}" value="${{it?.id}}" />
                 </g:if>
                 <g:else>
			  <% def value = '' %>
                          <g:if test="${event!=null}">
			  <% if(  parameter.type==dbnp.studycapturing.ProtocolParameterType.FLOAT  ) { value= event.parameterFloatValues[parameter.name]   } %>
			  <% if(  parameter.type==dbnp.studycapturing.ProtocolParameterType.STRING ) { value= event.parameterStringValues[parameter.name]  } %>
			  <% if(  parameter.type==dbnp.studycapturing.ProtocolParameterType.INTEGER) { value= event.parameterIntegerValues[parameter.name] } %>
                          </g:if>
                          <g:textField name="protocolParameter.${parameter.id}" value="${value}" />
			  ...
                 </g:else>
                 </td>

                 </tr>

            </g:each>






			    <% showSample=true %>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="endTime"><g:message code="event.endTime.label" default="This is a sampling event" /></label>
                                </td>
                                <td valign="top" class="name">
	                             <g:if test="${description.isSamplingEvent}">
                                     yes
	                             </g:if>
                                     <g:else>
				     no
                                     </g:else>
                                </td>
                            </tr>








