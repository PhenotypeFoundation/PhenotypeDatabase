
<%@ page import="dbnp.studycapturing.Publication" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'publication.label', default: 'Publication')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            
			<g:render template="/common/flashmessages" />
			            
            <div class="list">
                <table>
                    <thead>
                        <tr>
                            <g:sortableColumn property="title" title="${message(code: 'publication.title.label', default: 'Title')}" />

                            <g:sortableColumn property="authorsList" title="${message(code: 'publication.authorsList.label', default: 'Authors')}" />

                            <g:sortableColumn property="comments" title="${message(code: 'publication.comments.label', default: 'Comments')}" />

<!--
                            <g:sortableColumn property="pubMedID" title="${message(code: 'publication.pubMedID.label', default: 'Pub Med ID')}" />
                        
                            <g:sortableColumn property="DOI" title="${message(code: 'publication.DOI.label', default: 'DOI')}" />
-->
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${publicationInstanceList}" status="i" var="publicationInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${publicationInstance.id}">${fieldValue(bean: publicationInstance, field: "title")}</g:link></td>

                            <td>${fieldValue(bean: publicationInstance, field: "authorsList")}</td>

                            <td>${fieldValue(bean: publicationInstance, field: "comments")}</td>

<!--
                            <td>${fieldValue(bean: publicationInstance, field: "pubMedID")}</td>
                        
                            <td>${fieldValue(bean: publicationInstance, field: "DOI")}</td>
-->
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <span class="button"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${publicationInstanceTotal}" prev="&laquo; Previous" next="&raquo; Next" />
            </div>
        </div>
    </body>
</html>
