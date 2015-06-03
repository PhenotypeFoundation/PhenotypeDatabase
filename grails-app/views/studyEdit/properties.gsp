<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Study edit wizard</title>
	
	<r:require modules="studyEdit" />
</head>
<body>
	<div class="basicTabLayout studyEdit studyProperties">
		<h1>
			<span class="truncated-title">
			<g:if test="${study?.id}">
				Edit study [${study.code?.encodeAsHTML()}]
			</g:if>
			<g:else>
				New study
			</g:else>
			</span>
			<g:render template="steps" model="[study: study, active: 'study']" />
		</h1>
		
		<g:render template="/common/flashmessages" />
		
		<span class="message info"> 
			<span class="title">Define the basic properties of your study</span> 
			Enter all the basic information of your study. Keep in mind that the more specific the information that is
			filled out, the more valuable the system will be.
		</span>
		
		<g:render template="/common/flash_validation_messages" />
		 
		<g:form action="properties" name="studyProperties">
			<g:hiddenField name="_action" />
			<g:if test="${study?.id}">
				<g:hiddenField name="id" value="${study?.id}" />
			</g:if>
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
						%{--<div class="element">--}%
							%{--<div class="description">Published</div>--}%
							%{--<div class="input"><g:checkBox name="published" value="${study?.published}"/></div>--}%
							%{----}%
							%{--<div class="helpIcon"></div>--}%
							%{--<div class="helpContent">Determines whether this study is published (accessible for the study readers and, if the study is public, for anonymous users).</div>--}%
						%{--</div>--}%
				
						<af:userSelectElement name="readers" noForm="true" description="Readers" value="${study?.readers}"/>
                                                <af:userGroupSelectElement name="readerGroups" noForm="true" description="ReaderGroups" value="${study?.readerGroups}"/>
						<af:userSelectElement name="writers" noForm="true" description="Writers" value="${study?.writers}"/>
                                                <af:userGroupSelectElement name="writerGroups" noForm="true" description="WriterGroups" value="${study?.writerGroups}"/>
                                                
					</div>
				</div>
			</div>
			
			<af:templateElement name="template" description="Template"
				value="${study?.template}" entity="${dbnp.studycapturing.Study}"
				addDummy="true" onChange="if(\$( this ).val() != '') { \$( '#studyProperties' ).submit(); }">
				Choose the type of study you would like to create.
				Depending on the chosen template specific fields can be filled out. If none of the templates contain all the necessary fields, a new template can be defined (based on other templates).
			</af:templateElement>
		
			<g:if test="${study}">
				<g:if test="${study.template?.description}">
					<div class="element">
						<div class="templatedescription">
							${study.template?.description?.encodeAsHTML()}
						</div>
					</div>
				</g:if>
				<af:templateElements ignore="published" entity="${study}" />
			</g:if>
		
			<br clear="all" />

			<p class="options">
				<a href="#" onClick="StudyEdit.form.submit( 'studyProperties', 'next' ); return false;" class="next">Next</a>
				<a class="open separator" href="#" onClick="StudyEdit.showOpenStudyDialog(); return false;">Open</a>
				<a class="save" href="#" onClick="StudyEdit.form.submit( 'studyProperties', 'save' ); return false;">Save</a>
			</p>
			
		</g:form>
		
		<af:publicationDialog name="publication" />
		<af:userDialog name="readers" />
                <af:userGroupDialog name="readerGroups" />
		<af:userDialog name="writers" />
                <af:userGroupDialog name="writerGroups" />
		
		<div id="openStudyDialog">
			<p>
				Please select the study you want to edit form the list below. If your study is not in the list, you might
				not have sufficient privileges to edit the study.
			</p>
			
			<g:form class="simpleWizard" name="openstudy" action="edit">
				
			</g:form>
		</div>
		<script type="text/javascript"> 
			$("#openStudyDialog").dialog({
				title   : "Open study",
				autoOpen: false,
				width   : 400,
				height  : 200,
				modal   : true,
				position: "center",
				buttons : {
					Open: function() {
						if( confirm( "By opening a new study, changes to the current study are lost. Do you want to continue?" ) ) {
							StudyEdit.form.submit( 'openstudy' );
							$(this).dialog("close");
						}
					},
					Close  : function() {
						$(this).dialog("close");
					}
				},
			})
			
			$( function() {
				StudyEdit.initializePropertiesPage();
			});
		</script>
	</div>
</body>
</html>
