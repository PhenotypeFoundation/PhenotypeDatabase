
<%@ page import="dbnp.studycapturing.PersonRole" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${layout}" />
        <g:set var="entityName" value="${message(code: 'personRole.label', default: 'PersonRole')}" />
        <title><g:message code="default.edit.label" args="['Role']" /></title>
    </head>
    <body>

        <div class="body">
            <h1><g:message code="default.edit.label" args="['Role']" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${personRoleInstance}">
            <div class="errors">
                <g:renderErrors bean="${personRoleInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${personRoleInstance?.id}" />
                <g:hiddenField name="version" value="${personRoleInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="personRole.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personRoleInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${personRoleInstance?.name}" />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                     <g:each in="${extraparams}" var="param">
                       <input type="hidden" name="${param.key}" value="${param.value}">
                     </g:each>
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                    <span class="button"><g:actionSubmit class="cancel" action="list" value="Cancel" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
