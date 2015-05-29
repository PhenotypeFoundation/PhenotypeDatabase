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
			<g:render template="steps" model="[active: 'matchData']" />
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
			<span class="title">Match the rows and columns from your file</span> 
			Below you can specify where and how to store the data you provided.
		</span>
		
		<g:if test="${flash.validationErrors}">
			<div class="errormessage">
				<g:each var="error" in="${flash.validationErrors}">
					${error.value}<br />
				</g:each>
			</div>
		</g:if>  
		 
		<g:form action="match" name="matchData">
			<g:hiddenField name="_action" />
			<g:hiddenField name="importer" value="${importer.identifier}" />
			<g:hiddenField name="sessionKey" value="${sessionKey}" />
			
			
			${matrix}
			
			<br clear="all" />

			<p class="options">
				<a href="#" onClick="Importer.form.submit( 'matchData', 'previous' ); return false;" class="previous">Previous</a>
				<a href="#" onClick="Importer.form.submit( 'matchData', 'next' ); return false;" class="next">Next</a>
			</p>
			
		</g:form>

		<r:script>
			Importer.upload.initialize();
		</r:script>
	</div>
</body>
</html>
