<html>

<head>
      <meta name="layout" content="main" />
      <g:set var="entityName" value="${message(code: 'event.label', default: 'Event')}" />
      <g:setProvider library="jquery"/>
      <script src="${createLinkTo(dir: 'js', file: 'timepicker-0.2.1.js')}" type="text/javascript"></script>
</head>


<body>


        <div class="body">

            <h1><g:message code="default.create.label" args="[entityName]" /></h1>

            <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
            </g:if>
	    <g:hasErrors bean="${eventInstance}">
		<div class="errors"> <g:renderErrors bean="${eventInstance}" as="list" /> </div>
            </g:hasErrors>



            <g:form action="save" method="post" >

                <div class="dialog">
                    <table>
                        <tbody>


                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="subject"><g:message code="event.subject.label" default="Subject" /></label>
                                </td>
                                <td valign="top" class="value" >
				    <g:select id="subject" name="subject.id" from="${dbnp.studycapturing.Subject.list()}" optionKey="id" optionValue="name" value="${eventInstance?.subject?.id}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="startTime"><g:message code="event.startTime" default="Start Time" /></label>
                                </td>
                                <td valign="top" class="name">
			            <script> $(function() { $('#startTime').datepicker({ duration: '', showTime: true, constrainInput: false });}); </script>
	                            <%  def displayStartTime =String.format("%tm/", sDate ) %>
	                            <%      displayStartTime+=String.format("%td/", sDate ) %>
	                            <%      displayStartTime+=String.format("%tY", sDate ) %>
	                            <%      displayStartTime+=String.format(" %tI:", sDate ) %>
	                            <%      displayStartTime+=String.format("%tM ", sDate ) %>
	                            <%      displayStartTime+=String.format("%tp", sDate ) %>
				    <g:textField name="startTime" value="${displayStartTime}" />
                                </td>
                            </tr>



                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="endTime"><g:message code="event.endTime.label" default="End Time" /></label>
                                </td>
                                <td valign="top" class="name">
			            <script> $(function() { $('#endTime').datepicker({ duration: '', showTime: true, constrainInput: false });}); </script>
	                            <%  def displayEndTime =String.format("%tm/", eDate ) %>
	                            <%      displayEndTime+=String.format("%td/", eDate ) %>
	                            <%      displayEndTime+=String.format("%tY", eDate ) %>
	                            <%      displayEndTime+=String.format(" %tI:", eDate ) %>
	                            <%      displayEndTime+=String.format("%tM ", eDate ) %>
	                            <%      displayEndTime+=String.format("%tp", eDate ) %>
				    <g:textField name="endTime" value="${displayEndTime}" />
                                </td>
                            </tr>

                            <g:render template="../common/eventDescriptionTableRows" model="${[description:description, event:event]}"/>


                        </tbody>



                             <tbody id="samplePartial"> </tbody>



                    </table>
                </div>


                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>


            </g:form>
        </div>
</body>
</html>
