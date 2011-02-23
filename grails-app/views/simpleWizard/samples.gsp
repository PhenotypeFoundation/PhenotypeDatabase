<%@ page import="org.dbnp.gdt.GdtService" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Simple study wizard</title>
	
	<g:render template="javascripts" />
	
</head>
<body>
	<div class="simpleWizard">
		<h1>Samples</h1>
		
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
		
		<g:form class="simpleWizard" name="samples" action="samples" controller="simpleWizard">
			<input type="hidden" name="wizard" value="true" />
			<input type="hidden" name="event" value="refresh" />
		
			<g:if test="${study.samples?.size()}">
				<p>Current samples</p>
				<g:each in="${study.samples}" var="sample">
					${sample.name}<br />
				</g:each>
				
				<p class="options">
					<a href="#" onClick="$('#samplesDialog').dialog('open'); return false;" class="add">Add samples</a>
					<a href="#" onClick="$('#samplesDialog').dialog('open'); return false;" class="update">Update samples</a>
				</p>
				
				<% /* If samples are already present, the dialog should not be opened by default */ %>
				<script type="text/javascript">
					$( '#samplesDialog' ).dialog({
						title   : "Add/update samples",
						autoOpen: false,
						width   : 800,
						height  : 400,
						modal   : true,
						position: "center",
						buttons : {
							Add  : function() {
								//addUser(element_id);
								$(this).dialog("close");
							},
							Update  : function() {
								//addUser(element_id);
								$(this).dialog("close");
							},
							Close  : function() {
								$(this).dialog("close");
							}
						},
						close   : function() {
							/* closeFunc(this); */
						}
					});		
				</script>
			</g:if>
			<div id="samplesDialog">
		    	<span class="info"> 
					<span class="title">Import sample data</span> 
					You can import your Excel data to the server by choosing a file from your local harddisk in the form below.
				</span> 
		    
				<table border="0">
			    	<tr>
				    <td width="100px">
						Choose your Excel file to import:
				    </td>
				    <td width="100px">
						<af:fileField name="importfile" value="${sampleForm?.importFile}"/>
				    </td>
					</tr>
					<tr>
					    <td width="100px">
						Use data from sheet:
					    </td>
					    <td width="100px">
						<g:select name="sheetindex" from="${1..25}" value="${sampleForm?.sheetIndex}"/>
					    </td>
					</tr>
					<tr>
					    <td width="100px">
						Columnheader starts at row:
					    </td>
					    <td width="100px">
						<g:select name="headerrow" from="${1..10}" value="${sampleForm?.headerRow}"/>
					    </td>
					</tr>
					<tr>
					    <td width="100px">
						Data starts at row:
					    </td>
					    <td width="100px">
						<g:select name="datamatrix_start" from="${2..10}" value="${sampleForm?.dataMatrixStart}"/>
					    </td>
					</tr>	
					<tr>
					    <td>
						<div id="datatemplate">Choose type of sample template:</div>
					    </td>
					    <td>
							<g:select rel="template" entity="${encodedEntity}" name="template_id" optionKey="id" optionValue="name" from="${sampleTemplates}" value="${sampleForm?.templateId}"/>
					    </td>
					</tr>
				</table>	
			
			</div>
		
		</g:form>
		
		<p class="options">
			<a href="#" onClick="submitForm( 'samples', 'previous' ); return false;" class="previous">Previous</a>
			<a href="#" onClick="submitForm( 'samples', 'next' ); return false;" class="next">Next</a>
			<a class="skip" href="#" onClick="submitForm( 'samples', 'skip' ); return false;">Skip</a>
		</p>
	</div>	
</body>
</html>
