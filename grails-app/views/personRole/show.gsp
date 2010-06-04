
<%@ page import="dbnp.studycapturing.PersonRole" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${layout}" />
        <g:set var="entityName" value="${message(code: 'personRole.label', default: 'PersonRole')}" />
        <title><g:message code="default.show.label" args="['Role']" /></title>
    </head>
    <body>
    
        <div class="body">
            <h1><g:message code="default.show.label" args="['Role']" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="personRole.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: personRoleInstance, field: "name")}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${personRoleInstance?.id}" />
                     <g:each in="${extraparams}" var="param">
                       <input type="hidden" name="${param.key}" value="${param.value}">
                     </g:each>
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                    <span class="button"><g:link class="backToList" action="list" params="${extraparams}">Back to list</g:link></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
