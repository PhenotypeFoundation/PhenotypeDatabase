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
		
		<g:if test="${error}">
			<div class="errormessage">
				${error.toString().encodeAsHTML()}
			</div>
		</g:if>
		<g:if test="${message}">
			<div class="message">
				${message.toString().encodeAsHTML()}
			</div>
		</g:if>		

		<p class="options">
			<g:link class="view" controller="study" action="show" id="${study.id}">View study</g:link>
			<g:link class="edit" controller="simpleWizard" action="index" id="${study.id}">Edit study</g:link>
			<g:link class="restart" controller="simpleWizard" action="index">Add another study</g:link>
		</p>
	</div>
</body>
</html>
