
<%@ page import="org.dbnp.gdt.AssayModule" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'assayModule.label', default: 'AssayModule')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="nav" role="navigation">
			<ul>
				<li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-assayModule" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list assayModule">
			
				<g:if test="${assayModule?.UUID}">
				<li class="fieldcontain">
					<span id="UUID-label" class="property-label"><g:message code="assayModule.UUID.label" default="UUID" /></span>
					
						<span class="property-value" aria-labelledby="UUID-label"><g:fieldValue bean="${assayModule}" field="UUID"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${assayModule?.notify}">
				<li class="fieldcontain">
					<span id="notify-label" class="property-label"><g:message code="assayModule.notify.label" default="Notify" /></span>
					
						<span class="property-value" aria-labelledby="notify-label"><g:formatBoolean boolean="${assayModule?.notify}" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${assayModule?.openInFrame}">
				<li class="fieldcontain">
					<span id="openInFrame-label" class="property-label"><g:message code="assayModule.openInFrame.label" default="Open In Frame" /></span>
					
						<span class="property-value" aria-labelledby="openInFrame-label"><g:formatBoolean boolean="${assayModule?.openInFrame}" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${assayModule?.baseUrl}">
				<li class="fieldcontain">
					<span id="baseUrl-label" class="property-label"><g:message code="assayModule.baseUrl.label" default="Base Url" /></span>
					
						<span class="property-value" aria-labelledby="baseUrl-label"><g:fieldValue bean="${assayModule}" field="baseUrl"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${assayModule?.name}">
				<li class="fieldcontain">
					<span id="name-label" class="property-label"><g:message code="assayModule.name.label" default="Name" /></span>
					
						<span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${assayModule}" field="name"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${assayModule?.url}">
				<li class="fieldcontain">
					<span id="url-label" class="property-label"><g:message code="assayModule.url.label" default="Url" /></span>
					
						<span class="property-value" aria-labelledby="url-label"><g:fieldValue bean="${assayModule}" field="url"/></span>
					
				</li>
				</g:if>
			
			</ol>
			<g:form url="[resource:assayModule, action:'delete']" method="DELETE">
				<fieldset class="buttons">
					<g:link class="edit" action="edit" resource="${assayModule}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
