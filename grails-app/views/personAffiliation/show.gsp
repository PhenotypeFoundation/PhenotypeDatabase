
<%@ page import="dbnp.studycapturing.PersonAffiliation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${layout}" />
        <g:set var="entityName" value="${message(code: 'personAffiliation.label', default: 'PersonAffiliation')}" />
        <title><g:message code="default.show.label" args="['Affiliation']" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.show.label" args="['Affiliation']" /></h1>
  			<g:render template="/common/flashmessages" />
  			
            <div class="dialog">
                <table>
                    <tbody>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="personAffiliation.institute.label" default="Institute" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: personAffiliationInstance, field: "institute")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="personAffiliation.department.label" default="Department" /></td>

                            <td valign="top" class="value">${fieldValue(bean: personAffiliationInstance, field: "department")}</td>

                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${personAffiliationInstance?.id}" />
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
