<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Simple study wizard</title>
	
	<g:render template="javascripts" />
</head>
<body>
	<div class="simpleWizard">
		<h1>Study</h1>
		
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
			<span class="title">Define the basic properties of your study</span> 
			Enter all the basic information of your study. Keep in mind that the more specific the information that is
			filled out, the more valuable the system will be.
		</span>
		
		<g:if test="${flash.validationErrors}">
			<div class="errormessage">
				<g:each var="error" in="${flash.validationErrors}">
					${error.value}<br />
				</g:each>
			</div>
		</g:if>  
		 
		<g:form class="simpleWizard" name="study" action="study" controller="simpleWizard">
			<input type="hidden" name="wizard" value="true" />
			<input type="hidden" name="event" value="refresh" />
		
			<div class="meta">
				<div id="publications" class="component">
					<h2>Publications</h2>
					<div class="content">
						<af:publicationSelectElement noForm="true" name="publication" value="${study?.publications}"/>
					</div>
				</div>
				<div id="contacts" class="component">
					<h2>Contacts</h2>
					<div class="content">
						<af:contactSelectElement name="contacts" value="${study?.persons}"/>
					</div>
				</div>
				<div id="authorization" class="component">
					<h2>Authorization</h2>
					<div class="content">
						<div class="element">
							<div class="description">Public</div>
							<div class="input"><g:checkBox name="publicstudy" value="${study?.publicstudy}"/></div>
							<div class="helpIcon"></div>
							<div class="helpContent">Public studies are visible to anonymous users, not only to the readers specified below.</div>
						</div>
						<div class="element">
							<div class="description">Published</div>
							<div class="input"><g:checkBox name="published" value="${study?.published}"/></div>
							
							<div class="helpIcon"></div>
							<div class="helpContent">Determines whether this study is published (accessible for the study readers and, if the study is public, for anonymous users).</div>
						</div>
				
						<af:userSelectElement name="readers" noForm="true" description="Readers" value="${study?.readers}"/>
						<af:userSelectElement name="writers" noForm="true" description="Writers" value="${study?.writers}"/>
					</div>
				</div>
			</div>
			
			<af:templateElement name="template" description="Template"
				value="${study?.template}" entity="${dbnp.studycapturing.Study}"
				addDummy="true" onChange="if(\$( this ).val() != '') { submitForm( 'study' ); }">
				Choose the type of study you would like to create.
				Depending on the chosen template specific fields can be filled out. If none of the templates contain all the necessary fields, a new template can be defined (based on other templates).
			</af:templateElement>
		
			<g:if test="${study}">
				<af:templateElements ignore="published" entity="${study}" />
			</g:if>
		
			<br clear="all" />

			<p class="options">
				<a class="next" href="#" onClick="submitForm( 'study', 'next' ); return false;">Next</a>
			</p>
			
		</g:form>
		
		<af:publicationDialog name="publication" />
		<af:userDialog name="readers" />
		<af:userDialog name="writers" />
	</div>
</body>
</html>
