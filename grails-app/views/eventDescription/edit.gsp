
<%@ page import="dbnp.studycapturing.EventDescription" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'eventDescription.label', default: 'EventDescription')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${eventDescriptionInstance}">
            <div class="errors">
                <g:renderErrors bean="${eventDescriptionInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${eventDescriptionInstance?.id}" />
                <g:hiddenField name="version" value="${eventDescriptionInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="protocol"><g:message code="eventDescription.protocol.label" default="Protocol" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: eventDescriptionInstance, field: 'protocol', 'errors')}">
                                    <g:select name="protocol.id" from="${dbnp.studycapturing.ProtocolInstance.list()}" optionKey="id" value="${eventDescriptionInstance?.protocol?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="description"><g:message code="eventDescription.description.label" default="Description" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: eventDescriptionInstance, field: 'description', 'errors')}">
                                    <g:textField name="description" value="${eventDescriptionInstance?.description}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="eventDescription.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: eventDescriptionInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${eventDescriptionInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="classification"><g:message code="eventDescription.classification.label" default="Classification" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: eventDescriptionInstance, field: 'classification', 'errors')}">
                                    <g:textField name="classification" value="${eventDescriptionInstance?.classification}" />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
