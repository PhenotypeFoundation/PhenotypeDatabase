<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Study ${study.code} - Assays</title>
	
	<r:require modules="studyView,gscf-datatables" />
</head>
<body>	
	<div class="studyView studyAssays">
		<h1>
			<span class="truncated-title">
				Study [${study.code?.encodeAsHTML()}]
			</span>
			<g:render template="steps" model="[study: study, active: 'assays']"  />
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
			<span class="title">This view shows all assays</span> 
			For every template, a list of assays is shown
		</span>
	
		<g:each in="${templates}" var="template">
			<h3>Template: ${template.name}</h3>
			<table id="assaysTable_${template.id}" data-templateId="${template.id}" data-fieldPrefix="assay" data-formId="assayForm" class="assaysTable" rel="${g.createLink(action:"dataTableAssays", id: study.id, params: [template: template.id])}">
				<thead>
					<tr>
						<g:each in="${domainFields + template.getFields()}" var="field">
							<th data-fieldname="${field.escapedName()}">${field.name}</th>
						</g:each>
						<th class="nonsortable">Details</th>
					</tr>
				</thead>
				<tfoot>
					<tr class="messagebar selectAll">
						<td  colspan="${1 + domainFields.size() + template.getFields().size()}">
							You selected all items on this page. Would you <a href="#">select all items on other pages</a> as well? 
						</td>
					</tr>						
				</tfoot>
			</table>
		</g:each>			
			
		<p class="options">
			<g:if test="${study.canWrite(loggedInUser)}">
				<g:link class="edit" controller="studyEdit" action="assays" id="${study?.id}">edit</g:link>
			</g:if>
			<g:link class="back" controller="study" action="list" >back to list</g:link>
		</p>			
			
		<br clear="all" />

		<r:script>
			$(function() {
				StudyEdit.datatables.initialize( ".assaysTable" );
				StudyView.assays.initialize();
			});
		</r:script>
	</div>
</body>
</html>
