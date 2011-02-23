<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Simple study wizard</title>
	
	<g:render template="javascripts" />
</head>
<body>
	<div class="simpleWizard">
		<h1>Columns</h1>
		
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
	
		<g:form class="simpleWizard" name="columns" action="columns" controller="simpleWizard">
			<input type="hidden" name="wizard" value="true" />
			<input type="hidden" name="event" value="refresh" />
	
		   	<span class="info"> 
				<span class="title">Assign properties to columns</span> 
				You uploaded: ${filename}. This list shows the first ${excel.data.dataMatrix?.size()} rows of the uploaded file for reference.
				Please match the columns from the excel file with the fields in the database.
			</span> 
			    
			<div class="importcolumns">
				<table>
					<thead>
						<tr>
							<g:each in="${excel.data.header}" var="header">
								<th>${header.name}</th>
							</g:each>
						</tr>
					</thead>
					<tr class="matchWith">
						<g:each in="${excel.data.header}" var="mappingcolumn" status="i">
							<td>
								<g:set var="selected" value="${mappingcolumn.property}"/>
								<importer:propertyChooser name="matches" mappingcolumn="${mappingcolumn}" matchvalue="${mappingcolumn.name}" 
									selected="${selected}" fuzzymatching="true" treshold="0.8" template_id="${template.id}" "allfieldtypes="true"/>
							</td>
						</g:each>
					</tr>
					<g:each in="${excel.data.dataMatrix}" var="exampleRow">
						<tr class="example">
							<g:each in="${exampleRow}" var="exampleCell">
								<td>
									${exampleCell}
								</td>
							</g:each>
						</tr>
					</g:each>
				</table>
			</div>
		</g:form>
			
		<p class="options">
			<a href="#" onClick="submitForm( 'columns', 'previous' ); return false;" class="previous">Previous</a>
			<a href="#" onClick="submitForm( 'columns', 'next' ); return false;" class="next">Next</a>
		</p>
	</div>
</body>
</html>
