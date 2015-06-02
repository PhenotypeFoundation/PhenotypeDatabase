<%! import grails.converters.JSON %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Import data</title>
	
	<r:require modules="gscfimporter" />
</head>
<body>
	<div class="basicTabLayout importer uploadFile">
		<h1>
			<span class="truncated-title">
				Match data
			</span>
			<g:render template="steps" model="[active: 'validation']" />
		</h1>
		
		<g:if test="${flash.error}">
			<div class="errormessage">
				${flash.error.toString().encodeAsHTML()}
			</div>
		</g:if>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message.toString().encodeAsHTML()}
			</div>
		</g:if>	
		
		<span class="info"> 
			<span class="title">This page shows the validation results of your data</span> 
			Please review the message about your data. You can either choose to fix your original datafile and upload it again, or to continue importing. 
			If you continue importing, the rows that do not validate will be ignored.
		</span>
		
		<g:form action="match" name="validation">
			<g:hiddenField name="_action" />
			<g:hiddenField name="key" value="${sessionKey}" />

			<g:if test="${validationErrors}">
				<div class="errormessage">
					<g:each var="error" in="${validationErrors}">
						${error}<br />
					</g:each>
				</div>
			</g:if>
			<g:else>
				<div class="success">
					Your data was validated and no errors were found. You can safely continue importing this data.
				</div>
			</g:else>  
			 
			<br clear="all" />

			<p class="options">
				<g:link action="upload" params="${[key: sessionKey]}" class="upload">Back to upload</g:link>
				<g:link action="match" params="${[key: sessionKey]}" class="mapping">Back to mapping</g:link>
				<a href="#" onClick="Importer.form.submit( 'validation', 'import' ); return false;" class="import">Import</a>
			</p>
		</g:form>

	</div>
</body>
</html>
