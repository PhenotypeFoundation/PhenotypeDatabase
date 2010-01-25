
<%@ page import="dbnp.studycapturing.Event" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'event.label', default: 'Event')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
	<g:render template="../common/jquerysetup"/>
	<g:render template="../common/jqueryuisetup"/>
	<g:render template="../common/jquerytmsetup"/>

	
        <script type="text/javascript">
            function displayVals() {
               var singleValues = $("#protocol").val();
               var subjectValues = $("#protocol").val();
               $("p").html("<b>Single:</b> " + singleValues ); 
               $("select").change(displayVals);
            }
        </script>

        <script type="text/javascript">
        </script>

    </head>



<body>

        <p></p>

        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
        </div>


        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${eventInstance}">
            <div class="errors">
                <g:renderErrors bean="${eventInstance}" as="list" />
            </div>
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
				    <g:select id="subject" name="subject.id" from="${dbnp.studycapturing.Subject.list()}" optionKey="id" optionValue="name" value="${eventInstance?.subject?.id}"  onchange="displayVals()" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="startTime"><g:message code="event.startTime.label" default="Start Time" /></label>
                                </td>
                                <td valign="top" class="name">
			            <script> $(function() { $('#startTime').datepicker({ duration: '', showTime: true, constrainInput: false });}); </script>
	                            <%  def displayStartTime=sDate.toString() %>
				    <g:textField name="startTime" value="${displayStartTime}" />
                                </td>
                            </tr>

                             <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="endTime"><g:message code="event.endTime.label" default="End Time" /></label>
                                </td>
                                <td valign="top" class="name">
			            <script> $(function() { $('#endTime').datepicker({ duration: '', showTime: true, constrainInput: false });}); </script>
	                            <%  def displayEndTime=eDate.toString() %>
				    <g:textField name="endTime" value="${displayEndTime}" />
                                </td>
                            </tr>


                            <g:render template="../common/eventDescriptionTableRows" model="[description:description]"/>

                        </tbody>
                    </table>
                </div>


                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>


            </g:form>
        </div>
</body>
</html>
