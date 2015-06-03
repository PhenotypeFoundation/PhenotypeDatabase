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
				Validation results
			</span>
			<g:render template="steps" model="[active: 'validation']" />
		</h1>
		
		<g:render template="/common/flashmessages" />
		
		<g:form action="validation" name="validation">
			<g:hiddenField name="_action" />
			<g:hiddenField name="key" value="${sessionKey}" />

			<g:if test="${validationErrors}">
				<div class="message errormessage">
					<span class="title">Validation results</span>
					<p>
						The following problems were found when validating your data
					</p>
					<ul>
						<g:each var="error" in="${validationErrors}">
							<li>${error}</li>
						</g:each>
					</ul>
				</div>
				
				<p> 
					Please review these problems. You can either choose to fix your original datafile and upload it again, or to continue importing. 
					If you continue importing, the rows that do not validate will be ignored.
				</p>
			</g:if>
			<g:else>
				<div class="message okay">
					<span class="title">Your data looks OK</span>
					Your data was validated and no errors were found. You can safely continue importing this data.
				</div>
			</g:else>  
			 
			<br clear="all" />

			<p class="options">
				<g:link action="upload" params="${[key: sessionKey]}" class="restart">Back to upload</g:link>
				<g:link action="match" params="${[key: sessionKey]}" class="previous">Back to mapping</g:link>
				<a href="#" onClick="Importer.form.submit( 'validation', 'import' ); return false;" class="import">Import</a>
			</p>
		</g:form>

	</div>
</body>
</html>
