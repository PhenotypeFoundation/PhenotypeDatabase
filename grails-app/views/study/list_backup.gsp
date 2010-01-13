
<%@ page import="dbnp.studycapturing.Study" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'study.id.label', default: 'Id')}" />
                        
                            <th><g:message code="study.template.label" default="Template" /></th>
                   	    
                            <g:sortableColumn property="startDate" title="${message(code: 'study.startDate.label', default: 'Start Date')}" />
                        
                            <g:sortableColumn property="lastUpdated" title="${message(code: 'study.lastUpdated.label', default: 'Last Updated')}" />
                        
                            <g:sortableColumn property="code" title="${message(code: 'study.code.label', default: 'Code')}" />
                        
                            <g:sortableColumn property="ecCode" title="${message(code: 'study.ecCode.label', default: 'Ec Code')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${studyInstanceList}" status="i" var="studyInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${studyInstance.id}">${fieldValue(bean: studyInstance, field: "id")}</g:link>
									</td>
                        
                            <td>${fieldValue(bean: studyInstance, field: "template")}</td>
                        
                            <td><g:formatDate date="${studyInstance.startDate}" /></td>
                        
                            <td><g:formatDate date="${studyInstance.lastUpdated}" /></td>
                        
                            <td>${fieldValue(bean: studyInstance, field: "code")}</td>
                        
                            <td>${fieldValue(bean: studyInstance, field: "ecCode")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${studyInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
