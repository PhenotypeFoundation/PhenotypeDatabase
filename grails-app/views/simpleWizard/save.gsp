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

		${study}
		
		<g:form class="simpleWizard" name="saved" action="saved" controller="simpleWizard">
			<input type="hidden" name="wizard" value="true" />
			<input type="hidden" name="event" value="refresh" />
		
			<p class="options">
				<g:link class="view" controller="study" action="show" id="${study.id}">View study</g:link>
				<g:link class="edit" controller="simpleWizard" action="study" id="${study.id}">Edit study</g:link>
				<g:link class="restart" controller="simpleWizard" action="study">Add another study</g:link>
			</p>
			
		</g:form>
	</div>
</body>
</html>
