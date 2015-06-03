
<%@ page import="dbnp.studycapturing.Publication" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'publication.label', default: 'Publication')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>

			<g:render template="/common/flashmessages" />
			<g:render template="/common/instance_errors" model="[instance: publicationInstance]" />

            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="title"><g:message code="publication.title.label" default="Title" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: publicationInstance, field: 'title', 'errors')}">
                                    <g:textField name="title" value="${publicationInstance?.title}" />
                                </td>
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="authorsList"><g:message code="publication.authorsList.label" default="Authors List" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: publicationInstance, field: 'authorsList', 'errors')}">
                                    <g:textField name="authorsList" value="${publicationInstance?.authorsList}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comments"><g:message code="publication.comments.label" default="Comments" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: publicationInstance, field: 'comments', 'errors')}">
                                    <g:textField name="comments" value="${publicationInstance?.comments}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="pubMedID"><g:message code="publication.pubMedID.label" default="Pub Med ID" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: publicationInstance, field: 'pubMedID', 'errors')}">
                                    <g:textField name="pubMedID" value="${publicationInstance?.pubMedID}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="DOI"><g:message code="publication.DOI.label" default="DOI" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: publicationInstance, field: 'DOI', 'errors')}">
                                    <g:textField name="DOI" value="${publicationInstance?.DOI}" />
                                </td>
                            </tr>

                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                   <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                   <span class="button"><g:link class="cancel" action="list">Cancel</g:link></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
