
<%@ page import="dbnp.studycapturing.Event" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'event.label', default: 'Event')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="event.eventDescription.label" default="Event Description" /></td>
                            <td valign="top" class="value"><g:link controller="eventDescription" action="show" id="${eventInstance?.eventDescription?.id}">${eventInstance?.eventDescription?.name?.encodeAsHTML()}</g:link></td>
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="event.subject.label" default="Subject" /></td>
                            <td valign="top" class="value"><g:link controller="subject" action="show" id="${eventInstance?.subject?.id}">${eventInstance?.subject?.name?.encodeAsHTML()}</g:link></td>
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="event.startTime.label" default="Start Time" /></td>
                            <td valign="top" class="value"><g:message code="${eventInstance.startTime.toString()}" /></td>
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="event.endTime.label" default="End Time" /></td>
                            <td valign="top" class="value"><g:message code="${eventInstance.endTime.toString()}" /></td>
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="event.duration.label" default="Duration" /></td>
                            <td valign="top" class="value">${eventInstance.getDurationString()}</td>
                        </tr>
                   
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${eventInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
