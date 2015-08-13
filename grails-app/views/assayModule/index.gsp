
<%@ page import="org.dbnp.gdt.AssayModule" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'assayModule.label', default: 'AssayModule')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="nav" role="navigation">
			<ul>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-assayModule" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<g:sortableColumn property="UUID" title="${message(code: 'assayModule.UUID.label', default: 'UUID')}" />
					
						<g:sortableColumn property="notify" title="${message(code: 'assayModule.notify.label', default: 'Notify')}" />
					
						<g:sortableColumn property="openInFrame" title="${message(code: 'assayModule.openInFrame.label', default: 'Open In Frame')}" />
					
						<g:sortableColumn property="baseUrl" title="${message(code: 'assayModule.baseUrl.label', default: 'Base Url')}" />
					
						<g:sortableColumn property="name" title="${message(code: 'assayModule.name.label', default: 'Name')}" />
					
						<g:sortableColumn property="url" title="${message(code: 'assayModule.url.label', default: 'Url')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${assayModuleList}" status="i" var="assayModule">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${assayModule.id}">${fieldValue(bean: assayModule, field: "UUID")}</g:link></td>
					
						<td><g:formatBoolean boolean="${assayModule.notify}" /></td>
					
						<td><g:formatBoolean boolean="${assayModule.openInFrame}" /></td>
					
						<td>${fieldValue(bean: assayModule, field: "baseUrl")}</td>
					
						<td>${fieldValue(bean: assayModule, field: "name")}</td>
					
						<td>${fieldValue(bean: assayModule, field: "url")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${assayModuleCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
