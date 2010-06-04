
<%@ page import="dbnp.studycapturing.PersonRole" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${layout}" />
        <g:set var="entityName" value="${message(code: 'personRole.label', default: 'PersonRole')}" />
        <title><g:message code="default.list.label" args="['Role']" /></title>
    </head>
    <body>

        <div class="body">
            <h1><g:message code="default.list.label" args="['Role']" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                            <g:sortableColumn property="name" title="${message(code: 'personRole.name.label', default: 'Name')}" />
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${personRoleInstanceList}" status="i" var="personRoleInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td>${fieldValue(bean: personRoleInstance, field: "name")}</td>
                            <td class="buttons">
                              <g:form>
                                 <g:each in="${extraparams}" var="param">
                                   <input type="hidden" name="${param.key}" value="${param.value}">
                                 </g:each>
                                  <g:hiddenField name="id" value="${personRoleInstance?.id}" />
                                  <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                                  <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                              </g:form>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <span class="button"><g:link params="${extraparams}" class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${personRoleInstanceTotal}" prev="&laquo; Previous" next="&raquo; Next" params="${extraparams}" />
            </div>
        </div>
    </body>
</html>
