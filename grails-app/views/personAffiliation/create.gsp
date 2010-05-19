
<%@ page import="dbnp.studycapturing.PersonAffiliation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'personAffiliation.label', default: 'PersonAffiliation')}" />
        <title><g:message code="default.create.label" args="['Affiliation']" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.create.label" args="['Affiliation']" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${personAffiliationInstance}">
            <div class="errors">
                <g:renderErrors bean="${personAffiliationInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><g:message code="personAffiliation.institute.label" default="Institute" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personAffiliationInstance, field: 'institute', 'errors')}">
                                    <g:textField name="institute" value="${personAffiliationInstance?.institute}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><g:message code="personAffiliation.department.label" default="Department" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personAffiliationInstance, field: 'department', 'errors')}">
                                    <g:textField name="department" value="${personAffiliationInstance?.department}" />
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
