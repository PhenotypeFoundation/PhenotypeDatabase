
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

			<div class="data">
				<dt:dataTable id="fList" class="paginate sortable filter serverside" rel="${g.createLink( controller: 'Platform', action: 'datatables_list', params: [module: module] )}">
					<thead>
					<tr>
						<th>Name</th>
						<th>Comments</th>
						<th>Type</th>
						<th>Version</th>
						<th>Template</th>
						<th class="nonsortable"></th>
						<th class="nonsortable"></th>
					</tr>
					</thead>
				</dt:dataTable>
			</div>
		</div>
	</body>
</html>
