
<%@ page import="dbnp.studycapturing.Person" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
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
                            <td valign="top" class="name"><g:message code="person.firstName.label" default="First Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "firstName")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="person.midInitials.label" default="Mid Initials" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "midInitials")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="person.lastName.label" default="Last Name" /></td>

                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "lastName")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="person.address.label" default="Address" /></td>

                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "address")}</td>

                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="person.phone.label" default="Phone" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "phone")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="person.fax.label" default="Fax" /></td>

                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "fax")}</td>

                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="person.email.label" default="Email" /></td>

                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "email")}</td>

                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="person.affiliations.label" default="Affiliations" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${personInstance.affiliations}" var="a">
                                    <li><g:link controller="personAffiliation" action="show" id="${a.id}">${a?.name.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${personInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
