
<%@ page import="org.dbxp.sam.Platform" %>
<!DOCTYPE html>
<html>
	<head>
        <meta name="layout" content="sammain"/>
		<g:set var="entityName" value="${message(code: 'platform.label', default: 'Platform')}" />
		<title><g:message code="default.module.label" args="[entityName, module]"/></title>
	</head>
	<body>
    <content tag="contextmenu">
        <g:render template="contextmenu" />
    </content>
		<div id="list-platform" class="content scaffold-list" role="main">
			<h1><g:message code="default.samlist.label" args="[entityName, module]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
					
						<g:sortableColumn property="name" title="${message(code: 'platform.name.label', default: 'Name')}" />
					
						<g:sortableColumn property="comments" title="${message(code: 'platform.comments.label', default: 'Comments')}" />
					
						<g:sortableColumn property="platformtype" title="${message(code: 'platform.platformtype.label', default: 'Platformtype')}" />
					
						<g:sortableColumn property="platformversion" title="${message(code: 'platform.platformversion.label', default: 'Platformversion')}" />

                        <g:sortableColumn property="template" title="${message(code: 'platform.template.label', default: 'Template')}" />
					</tr>
				</thead>
				<tbody>
				<g:each in="${platformInstanceList}" status="i" var="platformInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${platformInstance.id}" params="${[module: module]}">${fieldValue(bean: platformInstance, field: "name")}</g:link></td>
					
						<td>${fieldValue(bean: platformInstance, field: "comments")}</td>
					
						<td>${fieldValue(bean: platformInstance, field: "platformtype")}</td>
					
						<td>${fieldValue(bean: platformInstance, field: "platformversion")}</td>

                        <td>${fieldValue(bean: platformInstance, field: "template")}</td>
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${platformInstanceTotal}" params="${[module: module]}" />
			</div>
		</div>
	</body>
</html>
