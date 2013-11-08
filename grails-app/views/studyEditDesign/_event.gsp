<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="dialog" />
	<title>Study edit wizard</title>
	
	<r:require modules="studyEdit" />
</head>
<body>
	<div class="studyEdit event">
		<h1>
			<g:if test="${entity?.id}">
				Edit event [${entity.name.encodeAsHTML()}]
			</g:if>
			<g:else>
				New event
			</g:else>
		</h1>
		
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
		
		<g:if test="${flash.validationErrors}">
			<div class="errormessage">
				<g:each var="error" in="${flash.validationErrors}">
					${error.encodeAsHTML()}<br />
				</g:each>
			</div>
		</g:if>  
		 
		<g:form action="${actionName}" name="eventDetails">
			<g:hiddenField name="_action" />
			<g:if test="${entity?.id}">
				<g:hiddenField name="id" value="${entity?.id}" />
			</g:if>
			<g:if test="${entity?.parent?.id}">
				<g:hiddenField name="parentId" value="${entity?.parent?.id}" />
			</g:if>
			
			<af:templateElement name="template" description="Template"
				value="${entity?.template}" entity="${dbnp.studycapturing.Event}"
				addDummy="true" onChange="if(\$( this ).val() != '') { \$( '#eventDetails' ).submit(); }">
				Choose the type of event you would like to create.
				Depending on the chosen template specific fields can be filled out. If none of the templates contain all the necessary fields, a new template can be defined (based on other templates).
			</af:templateElement>
		
			<g:if test="${entity}">
				<g:if test="${entity.template?.description}">
					<div class="element">
						<div class="templatedescription">
							${entity.template?.description?.encodeAsHTML()}
						</div>
					</div>
				</g:if>
				<af:templateElements entity="${entity}" />
			</g:if>
		
		</g:form>
	
	</div>
</body>
</html>
