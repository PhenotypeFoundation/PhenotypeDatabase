<%@ page import="org.dbxp.sam.Platform; org.dbnp.gdt.TemplateFieldType" %>
<!DOCTYPE html>
<html>
	<head>
        <meta name="layout" content="sammain"/>
		<g:set var="entityName" value="${message(code: 'platform.label', default: 'Platform')}" />
		<title><g:message code="default.module.label" args="[entityName, module]" /></title>
	</head>
	<body>
    <g:hasErrors bean="${platformInstance}">
        <div class="errors">
            <g:renderErrors bean="${platformInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <content tag="contextmenu">
        <g:render template="contextmenu" />
    </content>
    <div class="data">
			<h1><g:message code="default.samshow.label" args="[entityName, module]" /></h1>
            <table>
                <tbody>
                <% def ii = 0%>
                <g:each in="${platformInstance.giveFields()}" var="field" status="i">
                    <tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}">
                        <td valign="top">
                            ${field.name.capitalize()}
                        </td>
                        <td valign="top" >
                            <%-- Show false for booleans --%>
                            <g:if test="${field.type==TemplateFieldType.BOOLEAN && platformInstance.getFieldValue(field.toString())==null}">
                                false
                            </g:if>
                            <g:else>
                                ${platformInstance.getFieldValue(field.toString())}
                            </g:else>
                        </td>
                        <% ii = i + 1%>
                    </tr>
                </g:each>
                </tbody>
            </table>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${platformInstance?.id}" />
                    <g:hiddenField name="module" value="${module}" />
					<g:link class="edit" action="edit" id="${platformInstance?.id}" params="${[module: module]}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                    <g:link action="list" class="cancel" params="${[module: module]}">Back to list</g:link>
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
