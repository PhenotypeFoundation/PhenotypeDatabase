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
			<g:render template="steps" model="[study: study, active: 'samples']"  />
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
			<span class="title">Edit samples or import more</span> 
			Review the list of samples and edit their details. You can also import more samples from an excel sheet.
		</span>
		
		<g:if test="${flash.validationErrors}">
			<div class="errormessage">
				<g:each var="error" in="${flash.validationErrors}">
					${error.value}<br />
				</g:each>
			</div>
		</g:if>  
		 
		<g:form action="samples" name="samples">
			<g:hiddenField name="_action" />
			<g:hiddenField name="id" value="${study.id}" />
			
			<g:each in="${templates}" var="template">
				<h3>Template: ${template.name}</h3>
				<table id="samplesTable_${template.id}" data-templateId="${template.id}" data-fieldPrefix="sample" data-formId="sampleForm" class="samplesTable selectMulti" rel="${g.createLink(action:"dataTableEntities", id: study.id, params: [template: template.id])}">
					<thead>
						<tr>
							<g:each in="${domainFields + template.getFields()}" var="field">
								<th data-fieldname="${field.escapedName()}">${field.name}</th>
							</g:each>
						</tr>
					</thead>
					<tfoot>
						<tr class="messagebar selectAll">
							<td  colspan="${1 + domainFields.size() + template.getFields().size()}">
								You selected all items on this page. Would you <a href="#">select all items on other pages</a> as well? 
							</td>
						</tr>						
						<tr class="messagebar saveChanges">
							<td class="" colspan="${1 + domainFields.size() + template.getFields().size()}">
								<span class="links">
									<a href="#" onClick="StudyEdit.datatables.editable.save(this); return false;">Save</a> or 
									<a href="#" onClick="StudyEdit.datatables.editable.discardChanges(this); return false;">Discard</a>
								</span>
								<span class="saving">Saving...</span>
							</td>
						</tr>
					</tfoot>
				</table>
				
				<div id="samplesTable_${template.id}_prototype" style="display: none" class="editable prototype">
					<g:render template="prototypes" model="[ template: template]" />
				</div>							
			</g:each>			
			
			<br clear="all" />
		</g:form>

		<g:form action="editSamples" name="sampleForm">
			<g:hiddenField class="original" name="id" value="${study.id}" />
		</g:form>
		
		<r:script>
			$(function() {
				StudyEdit.datatables.initialize( ".samplesTable" );
			});
		</r:script>
	</div>
</body>
</html>
