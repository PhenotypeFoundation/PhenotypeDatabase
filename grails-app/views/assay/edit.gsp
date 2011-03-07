
<%@ page import="dbnp.studycapturing.Assay" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'assay.label', default: 'Assay')}" />
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
            <g:hasErrors bean="${assayInstance}">
            <div class="errors">
                <g:renderErrors bean="${assayInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${assayInstance?.id}" />
                <g:hiddenField name="version" value="${assayInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="module"><g:message code="assay.module.label" default="Module" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: assayInstance, field: 'module', 'errors')}">
                                    <g:select name="module.id" from="${dbnp.studycapturing.AssayModule.list()}" optionKey="id" value="${assayInstance?.module?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="assay.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: assayInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${assayInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="parent"><g:message code="assay.parent.label" default="Parent" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: assayInstance, field: 'parent', 'errors')}">
                                    <g:select name="parent.id" from="${dbnp.studycapturing.Study.list()}" optionKey="id" value="${assayInstance?.parent?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="samples"><g:message code="assay.samples.label" default="Samples" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: assayInstance, field: 'samples', 'errors')}">
                                    <g:select name="samples" from="${dbnp.studycapturing.Sample.list()}" multiple="yes" optionKey="id" size="5" value="${assayInstance?.samples}" />
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
