<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Simple study wizard</title>
	
	<g:render template="javascripts" />
</head>
<body>
	<div class="simpleWizard">
		<h1>Saved</h1>
		
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

		<g:form class="simpleWizard" name="saved" action="simpleWizard">
			<input type="hidden" name="_eventId" value="refresh" />
		
			<p class="options">
				<a href="#" onClick="submitForm( 'saved', 'previous' ); return false;" class="previous">Previous</a>
			</p>
			
		</g:form>
	</div>
</body>
</html>
