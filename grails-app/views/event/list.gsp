
<%@ page import="dbnp.studycapturing.Event" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'event.label', default: 'Event')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'event.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="startTime" title="${message(code: 'event.startTime.label', default: 'Start Time')}" />
                        
                            <th><g:message code="event.eventDescription.label" default="Event Description" /></th>
                   	    
                            <th><g:message code="event.subject.label" default="Subject" /></th>
                   	    
                            <g:sortableColumn property="endTime" title="${message(code: 'event.endTime.label', default: 'End Time')}" />
                        
                            <g:sortableColumn property="duration" title="${message(code: 'event.duration.label', default: 'Duration')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${eventInstanceList}" status="i" var="eventInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${eventInstance.id}">${fieldValue(bean: eventInstance, field: "id")}</g:link></td>
                        
                            <td><g:formatDate date="${eventInstance.startTime}" /></td>
                        
                            <td>${fieldValue(bean: eventInstance, field: "eventDescription")}</td>
                        
                            <td>${fieldValue(bean: eventInstance, field: "subject")}</td>
                        
                            <td><g:formatDate date="${eventInstance.endTime}" /></td>
                        
                            <td>${fieldValue(bean: eventInstance, field: "duration")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${eventInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
