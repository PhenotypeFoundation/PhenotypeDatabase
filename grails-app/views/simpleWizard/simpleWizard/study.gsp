<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Simple study wizard</title>
	
	<g:render template="javascripts" />
</head>
<body>
	<div class="simpleWizard studypage">
		<h1>
			<g:if test="${study.id}">
				Edit study [${study.title?.encodeAsHTML()}]
			</g:if>
			<g:else>
				New study
			</g:else>
			<span class="stepNumber">(step 1 of 4)</span>
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
			<span class="title">Define the basic properties of your study</span> 
			Enter all the basic information of your study. Keep in mind that the more specific the information that is
			filled out, the more valuable the system will be.
		</span>
		
	   	<g:if test="${numExistingSamples > 300}">
		   	<span class="info">
		   		<span class="error" style="background-position: 0 50%;">Many samples in study</span> 
				Your study contains more than 300 samples. This wizard might become less responsive when editing that many samples, but will still function properly.<br />
				Please be patient when editing samples and saving your study.
			</span> 
		</g:if>
		
		
		<g:if test="${flash.validationErrors}">
			<div class="errormessage">
				<g:each var="error" in="${flash.validationErrors}">
					${error.value}<br />
				</g:each>
			</div>
		</g:if>  
		 
		<g:form class="simpleWizard" name="study" action="simpleWizard">
			<input type="hidden" name="_eventId" value="refresh" />
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
				<a href="#" onClick="submitForm( 'study', 'next' ); return false;" class="next">Next</a>
				<a class="open separator" href="#" onClick="$( '#openStudyDialog' ).dialog( 'open' ); return false;">Open</a>
				<a class="save" href="#" onClick="submitForm( 'study', 'save' ); return false;">Save</a>
			</p>
			
		</g:form>
		
		<af:publicationDialog name="publication" />
		<af:userDialog name="readers" />
		<af:userDialog name="writers" />
		
		<div id="openStudyDialog">
			<p>
				Please select the study you want to edit form the list below. If your study is not in the list, you might
				not have sufficient privileges to edit the study.
			</p>
			
			<g:form class="simpleWizard" name="openstudy" action="simpleWizard">
				<input type="hidden" name="_eventId" value="open" />			
				<g:select name="study" from="${studies}" optionKey="id" optionValue="title" />
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
							submitForm( 'openstudy' );
							$(this).dialog("close");
						}
					},
					Close  : function() {
						$(this).dialog("close");
					}
				},
			})	
		</script>
		
	</div>
</body>
</html>
