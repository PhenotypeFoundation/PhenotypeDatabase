<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Simple study wizard</title>
	
	<g:render template="javascripts" />
</head>
<body>
	<div class="simpleWizard assayspage">
		<h1>
			Assay
			<span class="stepNumber">(step 3 of 4)</span>
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
		
		<span class="info"> 
			<span class="title">Enter data about the assay</span>
			Or otherwise press skip to fill in the assay later.			 
		</span> 
		
		<g:if test="${flash.validationErrors}">
			<div class="errormessage">
				<g:each var="error" in="${flash.validationErrors}">
					${error.value}<br />
				</g:each>
			</div>
		</g:if>  
		 		
		<g:form class="simpleWizard" name="assays" action="simpleWizard">
			<input type="hidden" name="_eventId" value="refresh" />
			
			<af:templateElement name="template" description="Template"
				value="${assay.template}" entity="${dbnp.studycapturing.Assay}"
				addDummy="true" onChange="if(\$( this ).val() != '') { submitForm( 'assays' ); }">
				Choose the type of assay you would like to perform.
				Depending on the chosen template specific fields can be filled out. If none of the templates contain all the necessary fields, a new template can be defined (based on other templates).
			</af:templateElement>
		
			<g:if test="${assay}">
				<g:if test="${assay.template?.description}">
					<div class="element">
						<div class="templatedescription">
							${assay.template?.description?.encodeAsHTML()}
						</div>
					</div>
				</g:if>			
				<af:templateElements ignore="externalassayid" entity="${assay}" />
			</g:if>
		
			<br clear="all" />

			<p class="options">
				<a class="previous" href="#" onClick="submitForm( 'assays', 'previous' ); return false;">Previous</a>
				<a class="next" href="#" onClick="submitForm( 'assays', 'next' ); return false;">Next</a>
				<a class="save separator" href="#" onClick="submitForm( 'assays', 'save' ); return false;">Save</a>
				<a class="skip" href="#" onClick="submitForm( 'assays', 'skip' ); return false;">Skip</a>
			</p>
			
		</g:form>
	</div>
</body>
</html>
