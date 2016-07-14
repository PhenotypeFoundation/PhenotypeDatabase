<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Study ${study.code}</title>
	
	<r:require modules="studyView" />
</head>
<body>
	<div class="basicTabLayout studyView studyProperties">
		<h1>
			<span class="truncated-title">
			Study [${study.code?.encodeAsHTML()}]
			</span>
			<g:render template="steps" model="[study: study, active: 'study']" />
		</h1>
		
		<g:render template="/common/flashmessages" />
		
		<span class="message info"> 
			<span class="title">Basic properties of your study</span>
			This page shows the basic information about your study, as well as information on the authorizations. 
		</span>

			<div class="meta">
				<div id="publications" class="component">
					<h2>Publications</h2>
					<div class="content">
						<ul class="publication_list" id="publication_list">
							<g:if test="${study.publications}">
								<g:each in="${study.publications}" var="publication" status="i">
									<li class="${i % 2 == 0 ? 'even' : 'odd'}">
										<div class="title">${publication.title}</div>
										<div class="authors">${publication.authorsList}</div>
									</li>					
								</g:each>
							</g:if>
							<g:else>
								<li class="empty first" id="publication_none"><span class="publication_none" style="display: none;">No publications selected</span></li>
							</g:else>
						</ul>
					</div>
				</div>
				<div id="contacts" class="component">
					<h2>Contacts</h2>
					<div class="content">
						<ul class="contact_list" id="contact_list">
							<g:if test="${study.persons}">
								<g:each in="${study.persons}" var="studyPerson" status="i">
									<li class="${i % 2 == 0 ? 'even' : 'odd'}">
										<div class="person">${studyPerson.person}</div>
										<div class="role">${studyPerson.role}</div>
									</li>					
								</g:each>
							</g:if>
							<g:else>
								<li class="empty first" id="contacts_none"><span class="contacts_none" style="display: none;">No contacts selected</span></li>
							</g:else>
						</ul>
					</div>
				</div>
				<div id="authorization" class="component">
					<h2>Authorization</h2>
					<div class="content">
						<div class="element">
							<div class="description">Public Design</div>
							<div class="input"><g:if test="${study?.publicstudy}">Yes</g:if><g:else>No</g:else></div>
						</div>
						
						<div class="element">
							<div class="description">Readers</div>
							<div class="input">
								<span class="readers users">
									<g:each in="${study?.readers}" var="user">
										${user}<br />
									</g:each>
								</span>
								<span class="readers groups">
									<g:each in="${study?.readerGroups}" var="group">
										${group.groupName}<br />
									</g:each>
								</span>
							</div>
						</div>
						<div class="element">
							<div class="description">Writers</div>
							<div class="input">
								<span class="writers users">
									<g:each in="${study?.writers}" var="user">
										${user}<br />
									</g:each>
								</span>
								<span class="writers groups">
									<g:each in="${study?.writerGroups}" var="group">
										${group.groupName}<br />
									</g:each>
								</span>
							</div>
						</div>
					</div>
				</div>
			</div>
			
			<div class="study-properties-list">
				<div class="element"> 
					<div class="description">Study title</div> 
					<div class="value">${study.title}</div>
				</div>			
				
				<div class="element"> 
					<div class="description">Template </div> 
					<div class="value">${study.template.name}</div>
				</div>			
					
				<g:each in="${study.giveFields()}" var="field">
					<g:if test="${field}">
						<% def fieldValue = study.getFieldValue( field.name ) %>
						<g:if test="${fieldValue}">
							<div class="element"> 
								<div class="description">${field.name}</div>
								<div class="value">
									<g:if test="${field.type == org.dbnp.gdt.TemplateFieldType.FILE}">
										<g:link controller="file" action="get" id="${fieldValue}">${fieldValue}</g:link>
									</g:if>
									<g:else>
										${fieldValue}
									</g:else>
								</div>
							</div>			
						</g:if>
					</g:if>
				</g:each>
			</div>
		
			<br clear="all" />

			<g:form controller="study" action="delete">
				<p class="options">
					<g:if test="${study.canWrite(loggedInUser)}">
						<g:link class="edit" controller="studyEdit" action="properties" id="${study?.id}">edit</g:link>
					
						<g:if test="${study.isOwner(loggedInUser) || loggedInUser?.hasAdminRights()}">
							<g:hiddenField name="id" value="${study?.id}"/>
							<g:link class="delete" onClick="if( confirm( 'Are you sure you want to delete this study? You cannot undo this operation!' ) ) { \$(this).closest('form').submit(); } return false;">delete</g:link>
						</g:if>
					</g:if>
					<g:link class="back" controller="study" action="list" >back to list</g:link>
				</p>
			</g:form>
		
		<script type="text/javascript"> 
			$( function() {
				StudyView.initializePropertiesPage();
			});
		</script>
	</div>
</body>
</html>
