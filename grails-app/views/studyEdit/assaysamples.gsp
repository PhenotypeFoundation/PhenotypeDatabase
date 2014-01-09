<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Study edit wizard</title>
	
	<r:require modules="studyEdit,gscf-datatables" />
</head>
<body>
	<div class="studyEdit studySamples">
		<h1>
			Edit study [${study.title?.encodeAsHTML()}]
			<g:render template="steps" model="[study: study, active: 'assaysamples']"  />
		</h1>
		
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
			<span class="title">Associate samples to assays</span> 
			Review the list of samples and specify the assays they have been analysed in.
		</span>
		
		<g:if test="${flash.validationErrors}">
			<div class="errormessage">
				<g:each var="error" in="${flash.validationErrors}">
					${error.value}<br />
				</g:each>
			</div>
		</g:if>  
		 
		<g:form action="assaysamples" id="${study.id}" name="assaysamples">
			<g:hiddenField name="_action" />
			<g:hiddenField name="id" value="${study.id}" />
			
			<table id="samplestable" data-formId="sampleForm" class="samplesTable selectMulti" rel="${g.createLink(action:"dataTableAssaySamples", id: study.id)}">
				<thead>
					<tr>
					
						<th>Sample</th>
						<th>Subject</th>
						<th>Eventgroup</th>
						<th>Sampling event</th>
						<th>Sample template</th>
						<th>Starttime (combined)</th>
						<g:each in="${study.assays.sort {it.name} }" var="assay">
							<th class="assay" data-id="${assay.id}">${assay.name}</th>
						</g:each>
					</tr>
				</thead>
				<tfoot>
					<tr class="messagebar selectAll">
						<td colspan="${study.assays.size() + 5}">
							You selected all items on this page. Would you <a href="#">select all items on other pages</a> as well? 
						</td>
					</tr>
					<tr class="messagebar saveChanges">
						<td class="" colspan="${study.assays.size() + 5}">
							<span class="links">
								<a href="#" onClick="StudyEdit.datatables.editable.save(this); return false;">Save</a> or 
								<a href="#" onClick="StudyEdit.datatables.editable.discardChanges(this); return false;">Discard</a>
							</span>
							<span class="saving">Saving...</span>
						</td>
					</tr>
											
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
