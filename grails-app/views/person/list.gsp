
<%@ page import="dbnp.studycapturing.Person" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="firstName" title="${message(code: 'person.firstName.label', default: 'First Name')}" />
                        
                            <g:sortableColumn property="midInitials" title="${message(code: 'person.midInitials.label', default: 'Mid Initials')}" />
                        
                            <g:sortableColumn property="lastName" title="${message(code: 'person.lasttName.label', default: 'Last Name')}" />

                            <g:sortableColumn property="phone" title="${message(code: 'person.phone.label', default: 'Phone')}" />
                        
                            <g:sortableColumn property="email" title="${message(code: 'person.email.label', default: 'Email')}" />

                            <th>Affiliations</th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${personInstanceList}" status="i" var="personInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${personInstance.id}">${fieldValue(bean: personInstance, field: "firstName")}</g:link></td>
                        
                            <td>${fieldValue(bean: personInstance, field: "midInitials")}</td>
                        
                            <td><g:link action="show" id="${personInstance.id}">${fieldValue(bean: personInstance, field: "lastName")}</g:link></td>

                            <td>${fieldValue(bean: personInstance, field: "phone")}</td>

                            <td>${fieldValue(bean: personInstance, field: "email")}</td>

                            <td>
                              <g:each in="${personInstance.affiliations}" var="affiliation" status="affiliationNr">
                                <g:if test="${affiliationNr>0}">,</g:if>
                                ${affiliation.name}
                              </g:each>
                            </td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${personInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
