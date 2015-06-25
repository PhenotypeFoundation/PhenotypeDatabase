
<%@ page import="dbnp.studycapturing.Person" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${layout}" />
        <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1>Persons</h1>
  			<g:render template="/common/flashmessages" />
         	<div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn params="${extraparams}" property="firstName" title="${message(code: 'person.firstName.label', default: 'First Name')}" />
                        
                            <g:sortableColumn params="${extraparams}" property="prefix" title="${message(code: 'person.prefix.label', default: 'Prefix')}" />
                        
                            <g:sortableColumn params="${extraparams}" property="lastName" title="${message(code: 'person.lasttName.label', default: 'Last Name')}" />

                            <g:if test="${layout!='dialog'}">
                            <g:sortableColumn params="${extraparams}" property="phone" title="${message(code: 'person.phone.label', default: 'Work Phone')}" />
                        
                            <g:sortableColumn params="${extraparams}" property="email" title="${message(code: 'person.email.label', default: 'Email')}" />
                            </g:if>

                            <th>Affiliations</th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${personInstanceList}" status="i" var="personInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td>${fieldValue(bean: personInstance, field: "firstName")}</td>
                        
                            <td>${fieldValue(bean: personInstance, field: "prefix")}</td>
                        
                            <td><g:link params="${extraparams}" action="show" id="${personInstance.id}">${fieldValue(bean: personInstance, field: "lastName")}</g:link></td>

                            <g:if test="${layout!='dialog'}">
                              <td>${fieldValue(bean: personInstance, field: "phone")}</td>

                              <td>${fieldValue(bean: personInstance, field: "email")}</td>
                            </g:if>

                            <td>
                              <g:each in="${personInstance.affiliations}" var="affiliation" status="affiliationNr">
                                <g:if test="${affiliationNr>0}">,</g:if>
                                ${affiliation}
                              </g:each>
                            </td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <span class="button"><g:link class="create" action="create" params="${extraparams}"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
				<span class="button"><g:link class="otherList" controller="personAffiliation" action="list" params="${extraparams}">Edit Affiliations</g:link></span>
			</div>
            <div class="paginateButtons">
                <g:paginate total="${personInstanceTotal}" prev="&laquo; Previous" next="&raquo; Next" params="${extraparams}" />
            </div>

        </div>
    </body>
</html>
