
<%@ page import="dbnp.studycapturing.Person" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
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
                            <td valign="top" class="name"><g:message code="person.title.label" default="Title" /></td>

                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "title")}</td>

                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="person.gender.label" default="Gender" /></td>

                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "gender")}</td>

                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="person.firstName.label" default="First Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "firstName")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="person.initials.label" default="Initials" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "initials")}</td>
                            
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="person.prefix.label" default="Prefx" /></td>

                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "prefix")}</td>

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
                            <td valign="top" class="name"><g:message code="person.phone.label" default="Work Phone" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "phone")}</td>
                            
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="person.mobile.label" default="Mobile Phone" /></td>

                            <td valign="top" class="value">${fieldValue(bean: personInstance, field: "mobile")}</td>

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
                                    <li><g:link controller="personAffiliation" action="show" id="${a.id}">${a?.encodeAsHTML()}</g:link></li>
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
                    <span class="button"><g:link class="backToList" action="list">Back to list</g:link></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
