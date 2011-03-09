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
		
		<g:form class="simpleWizard" name="samples" action="simpleWizard">
			<input type="hidden" name="_eventId" value="refresh" />

			<div id="samplesDialog">
		    	<span class="info"> 
					<span class="title">Import sample data</span> 
					You can import your Excel data to the server by choosing a file from your local harddisk in the form below. The excel sheet should contain
					data on the first sheet, and the sheet should contain one row with headers.
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
					    <td>
						<div id="datatemplate">Choose type of sample template:</div>
					    </td>
					    <td>
							<g:select rel="template" entity="${encodedEntity.Sample}" name="sample_template_id" optionKey="id" optionValue="name" from="${templates.Sample}"/>
					    </td>
					</tr>
					<tr>
					    <td>
						<div id="datatemplate">Choose type of subject template:</div>
					    </td>
					    <td>
							<g:select rel="template" entity="${encodedEntity.Subject}" name="subject_template_id" optionKey="id" optionValue="name" from="${templates.Subject}"/>
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
