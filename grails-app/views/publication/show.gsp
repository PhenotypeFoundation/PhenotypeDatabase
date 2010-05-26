
<%@ page import="dbnp.studycapturing.Publication" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'publication.label', default: 'Publication')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="publication.title.label" default="Title" /></td>

                            <td valign="top" class="value">${fieldValue(bean: publicationInstance, field: "title")}</td>

                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="publication.authorsList.label" default="Authors List" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: publicationInstance, field: "authorsList")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="publication.comments.label" default="Comments" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: publicationInstance, field: "comments")}</td>
                            
                        </tr>


                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="publication.pubMedID.label" default="Pub Med ID" /></td>

                            <td valign="top" class="value">${fieldValue(bean: publicationInstance, field: "pubMedID")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="publication.DOI.label" default="DOI" /></td>

                            <td valign="top" class="value">
                              <g:if test="${publicationInstance.DOI}">
                                <a href="http://dx.doi.org/${fieldValue(bean: publicationInstance, field: "DOI")}">
                                  ${fieldValue(bean: publicationInstance, field: "DOI")}
                                </a>
                              </g:if>
                            </td>

                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${publicationInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                    <span class="button"><g:link class="backToList" action="list">Back to list</g:link></span>

                </g:form>
            </div>
        </div>
    </body>
</html>
