
<%@ page import="dbnp.studycapturing.Assay" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'assay.label', default: 'Assay')}" />
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'assay.id.label', default: 'Id')}" />
                        
                            <th><g:message code="assay.module.label" default="Module" /></th>
                   	    
                            <g:sortableColumn property="name" title="${message(code: 'assay.name.label', default: 'Name')}" />
                        
                            <th><g:message code="assay.parent.label" default="Parent" /></th>
                   	    
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${assayInstanceList}" status="i" var="assayInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${assayInstance.id}">${fieldValue(bean: assayInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: assayInstance, field: "module")}</td>
                        
                            <td>${fieldValue(bean: assayInstance, field: "name")}</td>
                        
                            <td>${fieldValue(bean: assayInstance, field: "parent")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${assayInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
