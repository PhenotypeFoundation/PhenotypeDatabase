<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Study edit wizard</title>
	
	<r:require modules="studyEdit,gscf-datatables" />
</head>
<body>
	<div class="basicTabLayout studyEdit studySamples">
		<h1>
			<span class="truncated-title">
				Edit study [${study.code?.encodeAsHTML()}]
			</span>
			<g:render template="steps" model="[study: study, active: 'assaysamples']"  />
		</h1>
		
		<g:render template="/common/flashmessages" />
		
		<span class="message info"> 
			<span class="title">Associate samples to assays</span> 
			Review the list of samples and specify the assays they have been analysed in.
		</span>
		
		<g:render template="/common/flash_validation_messages" />
		 
		<g:form action="assaysamples" id="${study.id}" name="assaysamples">
			<g:hiddenField name="_action" />
			<g:hiddenField name="id" value="${study.id}" />
			
			<table id="samplestable" data-formId="sampleForm" class="samplesTable selectMulti" rel="${g.createLink(action:"dataTableAssaySamples", id: study.id)}">
				<thead>
					<tr>
						<g:each in="${study.assays.sort {it.name} }" var="assay">
							<th class="assay" data-id="${assay.id}">${assay.name}</th>
						</g:each>
						<th>Sample</th>
						<th>Subject</th>
						<th>Group</th>
						<th>Sampling event</th>
						<th>Sample template</th>
						<th>Starttime (combined)</th>
					</tr>
				</thead>
				<tfoot>
					<tr><td>
						<div class="messagebar selectAll">
							You selected all items on this page. Would you like to <a href="#">select all items on other pages</a> as well?
						</div>						
						<div class="messagebar saveChanges">
							<span class="links">
								<a href="#" onClick="StudyEdit.datatables.editable.save(this); return false;">Save</a> or 
								<a href="#" onClick="StudyEdit.datatables.editable.discardChanges(this); return false;">Discard</a>
							</span>
							<span class="saving">Saving...</span>
						</div>
					</td></tr>
				</tfoot>
			</table>
				
			<p class="options">
				<g:link controller="studyEdit" action="assays" id="${study.id}" class="previous">Previous</g:link>
				<g:link controller="studyEdit" action="finished" id="${study.id}" class="next">Next</g:link>
			</p>				
			
			<br clear="all" />
		</g:form>

		<g:form action="editAssaySamples" name="sampleForm"><g:hiddenField class="original" name="id" value="${study.id}" /></g:form>
		
		<r:script>
			$(function() {
				StudyEdit.assaySamples.initialize();
			});
		</r:script>
	</div>
</body>
</html>
