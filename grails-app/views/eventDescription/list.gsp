
<%@ page import="dbnp.studycapturing.EventDescription" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'eventDescription.label', default: 'EventDescription')}" />
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'eventDescription.id.label', default: 'Id')}" />
                        
                            <th><g:message code="eventDescription.protocol.label" default="Protocol" /></th>
                   	    
                            <g:sortableColumn property="description" title="${message(code: 'eventDescription.description.label', default: 'Description')}" />
                        
                            <g:sortableColumn property="name" title="${message(code: 'eventDescription.name.label', default: 'Name')}" />
                        
                            <g:sortableColumn property="classification" title="${message(code: 'eventDescription.classification.label', default: 'Classification')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${eventDescriptionInstanceList}" status="i" var="eventDescriptionInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${eventDescriptionInstance.id}">${fieldValue(bean: eventDescriptionInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: eventDescriptionInstance, field: "protocol")}</td>
                        
                            <td>${fieldValue(bean: eventDescriptionInstance, field: "description")}</td>
                        
                            <td>${fieldValue(bean: eventDescriptionInstance, field: "name")}</td>
                        
                            <td>${fieldValue(bean: eventDescriptionInstance, field: "classification")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${eventDescriptionInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
