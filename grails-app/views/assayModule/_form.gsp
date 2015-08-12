<%@ page import="org.dbnp.gdt.AssayModule" %>



<div class="fieldcontain ${hasErrors(bean: assayModule, field: 'UUID', 'error')} ">
	<label for="UUID">
		<g:message code="assayModule.UUID.label" default="UUID" />
		
	</label>
	<g:textArea name="UUID" cols="40" rows="5" maxlength="255" value="${assayModule?.UUID}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: assayModule, field: 'notify', 'error')} ">
	<label for="notify">
		<g:message code="assayModule.notify.label" default="Notify" />
		
	</label>
	<g:checkBox name="notify" value="${assayModule?.notify}" />

</div>

<div class="fieldcontain ${hasErrors(bean: assayModule, field: 'openInFrame', 'error')} ">
	<label for="openInFrame">
		<g:message code="assayModule.openInFrame.label" default="Open In Frame" />
		
	</label>
	<g:checkBox name="openInFrame" value="${assayModule?.openInFrame}" />

</div>

<div class="fieldcontain ${hasErrors(bean: assayModule, field: 'baseUrl', 'error')} required">
	<label for="baseUrl">
		<g:message code="assayModule.baseUrl.label" default="Base Url" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="baseUrl" required="" value="${assayModule?.baseUrl}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: assayModule, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="assayModule.name.label" default="Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="name" required="" value="${assayModule?.name}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: assayModule, field: 'url', 'error')} required">
	<label for="url">
		<g:message code="assayModule.url.label" default="Url" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="url" required="" value="${assayModule?.url}"/>

</div>

