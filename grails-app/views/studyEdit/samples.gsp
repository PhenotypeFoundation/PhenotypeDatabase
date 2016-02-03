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
			<g:render template="steps" model="[study: study, active: 'samples']"  />
		</h1>
		
		<g:render template="/common/flashmessages" />

		<g:if test="${study.sampleCount != 0}">
			<span class="message info">
				<span class="title">Edit samples or import more</span>
				Review the list of samples and edit their details. You can also import more samples from an excel sheet.
			</span>
		</g:if>
		<g:else>
			<span class="message info">
				<span class="title">Generate samples</span>
				Click 'Generate samples' to initiate the sample generation based on your study design.
			</span>
		</g:else>

		
		<g:render template="/common/flash_validation_messages" />

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
				
				<div id="samplesTable_${template.id}_prototype" style="display: none" class="editable prototype">
					<g:render template="prototypes" model="[ template: template]" />
				</div>							
			</g:each>			
			
			<p class="options">
				<g:link controller="studyEditDesign" action="index" id="${study.id}" class="previous">Previous</g:link>
				<g:link controller="studyEdit" action="assays" id="${study.id}" class="next">Next</g:link>
				
				<g:link controller="studyEdit" action="generateSamples" id="${study.id}" class="performAction separator generateSamples">Generate samples</g:link>
				<g:link controller="studyEdit" action="regenerateSampleNames" id="${study.id}" class="refresh separator">Regenerate sample names</g:link>
				
	            <g:link class="import" controller="importer" action="upload" params="['initial.study': study?.id, importer: 'Samples']">
	                Import
	            </g:link>				
	            
	            <a href="#" class="delete" onClick="StudyEdit.samples.deleteItem(); return false;">Delete</a>					
			</p>				
			
			<br clear="all" />
		</g:form>

		<g:form action="editSamples" name="sampleForm"><g:hiddenField class="original" name="id" value="${study.id}" /></g:form>
		<g:form action="deleteSamples" id="${study.id}" name="deleteSamples"></g:form> 
		<div id="addDialog"></div>		
		
		<div id="generateSamplesDialog">
			<span class="message info"> 
				<span class="title">Sample generation based on study design</span>
				Samples can be generated based on your design. For each subject that had a sample treatment, one sample will be generated. You will be able to 
				change the sample properties afterwards.<br /> 
				<strong>Please note</strong>: Samples that had been created but are obsolete now, will be deleted.
			</span>
					
			<g:form action="generateSamples">
				<g:hiddenField name="id" value="${study.id}" />
			
				<ul id="studydesign">
					<g:each in="${study.subjectGroups}" var="subjectGroup">
						<g:if test="${subjectGroup.subjectEventGroups}">
							<li>
								<input type="checkbox" name="subjectgroup" value="${subjectGroup.id}" id="subjectgroup_${subjectGroup.id}" checked="checked" /> 
								<label for="subjectgroup_${subjectGroup.id}">Subjectgroup ${subjectGroup.name}</label>
								
								<ul class="subjectEventGroup">
									<g:each in="${subjectGroup.subjectEventGroups}" var="subjectEventGroup">
										<li>
											<input type="checkbox" name="subjectEventGroup" value="${subjectEventGroup.id}" id="subjectEventGroup_${subjectEventGroup.id}" checked="checked" /> 
											<label for="subjectEventGroup_${subjectEventGroup.id}">${subjectEventGroup.eventGroup.name} at ${subjectEventGroup.startTimeString}</label>
										</li>
									</g:each>
								</ul>
							</li>
						</g:if>
					</g:each>
				</ul>
			</g:form>			
			
		</div>
		
		<r:script>
			$(function() {
				StudyEdit.datatables.initialize( ".samplesTable" );
				StudyEdit.samples.initialize();
			});
		</r:script>
	</div>
</body>
</html>
