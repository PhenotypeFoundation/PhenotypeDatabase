
<%@ page import="dbnp.studycapturing.Person" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${layout}" />
        <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            
  			<g:render template="/common/flashmessages" />
  			<g:render template="/common/instance_errors" model="[instance: personInstance]" />
            
            <g:form method="post" >
                <g:hiddenField name="id" value="${personInstance?.id}" />
                <g:hiddenField name="version" value="${personInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>

                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="title"><g:message code="person.title.label" default="Title" /></label>
                                </td>
                                <td colspan="3" valign="top" class="value ${hasErrors(bean: personInstance, field: 'title', 'errors')}">
                                    <g:textField name="title" value="${personInstance?.title}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="gender"><g:message code="person.gender.label" default="Gender" /></label>
                                </td>
                                <td colspan="3" valign="top" class="value ${hasErrors(bean: personInstance, field: 'gender', 'errors')}">
                                  <g:select name="gender" from="${possibleGenders}" noSelection="['' : 'Not specified']" value="${personInstance?.gender}" />
                                </td>
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="firstName"><g:message code="person.firstName.label" default="First Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'firstName', 'errors')}">
                                    <g:textField name="firstName" value="${personInstance?.firstName}" />
                                </td>
                                <td valign="top" class="name">
                                  <label for="initials"><g:message code="person.initials.label" default="Initials" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'initials', 'errors')}">
                                    <g:textField name="initials" value="${personInstance?.initials}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="lastName"><g:message code="person.lastName.label" default="Last Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'lastName', 'errors')}">
                                    <g:textField name="lastName" value="${personInstance?.lastName}" />
                                </td>
                                <td valign="top" class="name">
                                    <label for="prefix"><g:message code="person.prefix.label" default="Prefix" /> (e.g. 'van de')</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'prefix', 'errors')}">
                                    <g:textField name="prefix" value="${personInstance?.prefix}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="address"><g:message code="person.address.label" default="Address" /></label>
                                </td>
                                <td colspan="3" valign="top" class="value ${hasErrors(bean: personInstance, field: 'address', 'errors')}">
                                    <g:textField name="address" value="${personInstance?.address}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="phone"><g:message code="person.phone.label" default="Work Phone" /></label>
                                </td>
                                <td colspan="3" valign="top" class="value ${hasErrors(bean: personInstance, field: 'phone', 'errors')}">
                                    <g:textField name="phone" value="${personInstance?.phone}" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="mobile"><g:message code="person.mobile.label" default="Mobile Phone" /></label>
                                </td>
                                <td colspan="3" valign="top" class="value ${hasErrors(bean: personInstance, field: 'mobile', 'errors')}">
                                    <g:textField name="mobile" value="${personInstance?.phone}" />
                                </td>
                            </tr>

                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="fax"><g:message code="person.fax.label" default="Fax" /></label>
                                </td>
                                <td colspan="3" valign="top" class="value ${hasErrors(bean: personInstance, field: 'fax', 'errors')}">
                                    <g:textField name="fax" value="${personInstance?.fax}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="email"><g:message code="person.email.label" default="Email" /></label>
                                </td>
                                <td colspan="3" valign="top" class="value ${hasErrors(bean: personInstance, field: 'email', 'errors')}">
                                    <g:textField name="email" value="${personInstance?.email}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="affiliations"><g:message code="person.affiliations.label" default="Affiliations" /></label>
                                </td>
                                <td colspan="3" valign="top" class="value ${hasErrors(bean: personInstance, field: 'affiliations', 'errors')}">
                                    <g:select name="affiliations" from="${dbnp.studycapturing.PersonAffiliation.list()}" multiple="yes" optionKey="id" size="5" value="${personInstance?.affiliations}" />

									<g:link controller="personAffiliation" action="list" params="${extraparams}">Edit Affiliations</g:link>
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
                    <span class="button"><g:actionSubmit class="cancel" action="show" value="Cancel" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
