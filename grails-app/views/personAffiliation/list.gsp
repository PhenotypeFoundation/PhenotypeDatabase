
<%@ page import="dbnp.studycapturing.PersonAffiliation" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${layout}" />
        <g:set var="entityName" value="${message(code: 'personAffiliation.label', default: 'PersonAffiliation')}" />
        <title><g:message code="default.list.label" args="['Affiliation']" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.list.label" args="['Affiliation']" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn params="${extraparams}"  property="institute" title="${message(code: 'personAffiliation.institute.label', default: 'Institute')}" />
                            <g:sortableColumn params="${extraparams}"  property="department" title="${message(code: 'personAffiliation.department.label', default: 'Department')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${personAffiliationInstanceList}" status="i" var="personAffiliationInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link params="${extraparams}"  action="show" id="${personAffiliationInstance.id}">${fieldValue(bean: personAffiliationInstance, field: "institute")}</g:link></td>
                            <td>${fieldValue(bean: personAffiliationInstance, field: "department")}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <span class="button"><g:link class="create" params="${extraparams}"  action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
				<span class="button"><g:link class="otherList" controller="person" action="list" params="${extraparams}">Edit Persons</g:link></span>
            </div>
            <div class="paginateButtons">
                <g:paginate params="${extraparams}"  total="${personAffiliationInstanceTotal}" prev="&laquo; Previous" next="&raquo; Next" />
            </div>
        </div>
    </body>
</html>
