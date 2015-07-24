<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Study ${study.code} - Samples</title>
	
	<r:require modules="studyView,gscf-datatables" />
</head>
<body>	
	<div class="basicTabLayout studyView studySamples">
		<h1>
			<span class="truncated-title">
				Study [${study.code?.encodeAsHTML()}]
			</span>
			<g:render template="steps" model="[study: study, active: 'samples']"  />
		</h1>
		
		<g:render template="/common/flashmessages" />
		
		<span class="message info"> 
			<span class="title">This view shows all samples</span> 
			For every template, a list of samples  is shown
		</span>
	
		<g:each in="${templates}" var="template">
			<h3>Template: ${template.name}</h3>
			<table id="samplesTable_${template.id}" data-templateId="${template.id}" data-fieldPrefix="sample" data-formId="sampleForm" class="samplesTable" rel="${g.createLink(action:"dataTableEntities", id: study.id, params: [template: template.id])}">
				<thead>
					<tr>
						<g:each in="${domainFields + template.getFields()}" var="field">
							<th data-fieldname="${field.escapedName()}">${field.name}</th>
						</g:each>
					</tr>
				</thead>
			</table>
		</g:each>			
			
		<p class="options">
			<g:if test="${study.canWrite(loggedInUser)}">
				<g:link class="edit" controller="studyEdit" action="samples" id="${study?.id}">edit</g:link>
			</g:if>
			<g:link class="back" controller="study" action="list" >back to list</g:link>
		</p>			
			
		<br clear="all" />

		<r:script>
			$(function() {
				StudyEdit.datatables.initialize( ".samplesTable" );
				StudyView.samples.initialize();
			});
		</r:script>
	</div>
</body>
</html>
