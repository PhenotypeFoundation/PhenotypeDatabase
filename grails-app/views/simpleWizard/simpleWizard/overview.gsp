<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Simple study wizard</title>
	
	<g:render template="javascripts" />
</head>
<body>
	<div class="simpleWizard">
		<h1>
			Study overview
			<span class="stepNumber">(step 4 of 4)</span>
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
			<span class="title">Your study</span> 
			This page shows an overview of the study you entered  
		</span> 
		<g:form class="simpleWizard" name="overview" action="simpleWizard">
			<input type="hidden" name="_eventId" value="refresh" />
		</g:form>
		
		<div class="meta">
			<div id="publications" class="component">
				<h2>Publications</h2>
				<div class="content">
					<g:if test="${study.publications}">
						<g:each var="publication" in="${study?.publications}">
							${publication.title}, ${publication.authorsList}
						</g:each>
					</g:if>
					<g:else>
						No publications
					</g:else>
				</div>
			</div>
			<div id="contacts" class="component">
				<h2>Contacts</h2>
				<div class="content">
					<g:if test="${study.persons}">
						<g:each var="contact" in="${study?.persons}">
							${contact.person?.lastName + ", " + contact.person?.firstName} as ${contact.role.name}<br />
						</g:each>
					</g:if>
					<g:else>
						No contacts
					</g:else>
				</div>
			</div>
			<div id="authorization" class="component">
				<h2>Authorization</h2>
				<div class="content">
					<div class="element">
						<div class="description">Public</div><g:if test="${study?.publicstudy}">yes</g:if><g:else>no</g:else>
					</div>
					<div class="element">
						<div class="description">Published</div><g:if test="${study?.published}">yes</g:if><g:else>no</g:else>
					</div>
					<div class="element">
						<div class="description">Readers</div>${study?.readers?.username?.join( ", " )}
					</div>
					<div class="element">
						<div class="description">Writers</div>${study?.writers?.username?.join( ", " )}
					</div>
				</div>
			</div>
		</div>
	
		<div class="element">
			<div class="description">Study template</div>${study.template?.name}
		</div>
		<g:each var="field" in="${study.giveFields()}">
			<div class="element">
				<div class="description">${field.name}</div>${study.getFieldValue( field.name )}
			</div>
		</g:each>
		
		<br />
		
		<div class="element">
			<div class="description">Samples</div>
			
			<g:if test="${study.samples?.size()}">
				<g:each in="${study.samples.template.unique()}" var="currentSampleTemplate" status="j">
					<g:if test="${currentSampleTemplate}">
						<g:if test="${j > 0}">,</g:if>
						<%=study.samples.findAll { return it.template == currentSampleTemplate; }.size()%>
						${currentSampleTemplate.name}
					</g:if>
				</g:each>
			</g:if>
			<g:else>
				-
			</g:else>
		</div>
		<br />
		
		<div class="element">
			<div class="description">Subjects</div>
			
			<g:if test="${study.subjects?.size()}">
				<g:each in="${study.subjects.template.unique()}" var="currentSubjectTemplate" status="j">
					<g:if test="${currentSubjectTemplate}">
						<g:if test="${j > 0}">,</g:if>
						<%=study.subjects.findAll { return it.template == currentSubjectTemplate; }.size()%>
						${currentSubjectTemplate.name}
					</g:if>
				</g:each>
			</g:if>
			<g:else>
				-
			</g:else>
		</div>
		
		<br />
		<g:if test="${assay}">
			<div class="element">
				<div class="description">Assay template</div>${assay.template?.name}
			</div>
		</g:if>

		<br clear="all" />

		<p class="options">
			<a class="previous" href="#" onClick="submitForm( 'overview', 'previous' ); return false;">Previous</a>
			<a class="save separator" href="#" onClick="submitForm( 'overview', 'save' ); return false;">Save</a>
		</p>
			
	</div>
</body>
</html>
