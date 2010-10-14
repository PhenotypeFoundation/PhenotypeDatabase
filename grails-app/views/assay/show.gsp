
<%@ page import="dbnp.studycapturing.Assay" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'assay.label', default: 'Assay')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="assay.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: assayInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="assay.externalAssayID.label" default="External Assay ID" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: assayInstance, field: "externalAssayID")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="assay.module.label" default="Module" /></td>
                            
                            <td valign="top" class="value"><g:link controller="assayModule" action="show" id="${assayInstance?.module?.id}">${assayInstance?.module?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="assay.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: assayInstance, field: "name")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="assay.parent.label" default="Parent" /></td>
                            
                            <td valign="top" class="value"><g:link controller="study" action="show" id="${assayInstance?.parent?.id}">${assayInstance?.parent?.encodeAsHTML()}</g:link></td>
                            
                        </tr>

                        <g:each var="field" in="${assayInstance.giveTemplateFields()}">
							<tr class="prop">
								<td valign="top" class="name">${field.name}</td>

								<td valign="top" class="value">${assayInstance.getFieldValue(field.name)?.encodeAsHTML()}</td>
							</tr>
						</g:each>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="assay.samples.label" default="Samples" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${assayInstance.samples}" var="s">
                                    <li><g:link controller="sample" action="show" id="${s.id}">${s?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${assayInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
