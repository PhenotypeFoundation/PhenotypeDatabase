<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Simple study wizard</title>
	
	<g:render template="javascripts" />
</head>
<body>
	<div class="simpleWizard">
		<h1>
			Imported file
			<span class="stepNumber">(step 2 of 4)</span>
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
	
		<g:form class="simpleWizard" name="columns" action="simpleWizard">
			<input type="hidden" name="_eventId" value="refresh" />
	
		   	<span class="info"> 
				<span class="title">Assign columns to template fields</span> 
				You uploaded: ${excel.filename}. This list shows the first ${excel.data.dataMatrix?.size()} rows of the uploaded file for reference.
				Please match the columns from the excel file with the fields in the database.
			</span> 
		   	<g:if test="${excel.numDataRows > 300}">
			   	<span class="info">
			   		<span class="error" style="background-position: 0 50%;">Many samples in excel file</span> 
					Your uploaded excel file contains more than 300 samples. This wizard might become less responsive when importing that many samples, but
					will still be working properly.<br />
					Please be patient when importing the data and saving your study.
				</span> 
			</g:if>
			    
			<div class="importcolumns">				
				<table cellspacing="0">
					<tr class="headerrow">
						<td nowrap class="explanation">Excel columns</td>
						<g:each in="${excel.data.header}" var="header">
							<th>${header.name}</th>
						</g:each>
					</tr>
					<g:each in="${excel.data.dataMatrix}" var="exampleRow" status="i"> 
						<tr class="example">
							<g:if test="${i == 0}">
								<td nowrap class="explanation">Example data</td>
							</g:if>
							<g:else>
								<td class="explanation"></td>
							</g:else>
							<g:each in="${exampleRow}" var="exampleCell">
								<td class="exampleCell"><div>
									${exampleCell}
									</div>
								</td>
							</g:each>
						</tr>
					</g:each>
					
					<tr class="matchWith">
						<td nowrap class="explanation">Template field</td>
					
						<g:each in="${excel.data.header}" var="mappingcolumn" status="i">
							<%
								def selectedValue;
								if( mappingcolumn.entityclass?.name && mappingcolumn.property )
									selectedValue = mappingcolumn.entityclass.name[ mappingcolumn.entityclass.name.lastIndexOf( "." ) + 1 .. -1 ] + "||"  + mappingcolumn.property;
							%>
							<td>
								<g:set var="selected" value="${mappingcolumn.property}"/>
								<% /* Put a select box with template fields of multiple entities */ %>
								<select name="matches.index.${mappingcolumn.index}" style="font-size: 10px;">
									<option value="dontimport">Don't import</option>
									<g:each in="${sampleForm.template}" var="entityTemplates">
										<g:if test="${entityTemplates.value}">
											<optgroup label="${entityTemplates.key}">
												<%
													def allFields = [] + domainFields[ entityTemplates.key ] + entityTemplates.value?.fields;
												%>
												<g:each in="${allFields}" var="field">
													<g:if test="${field}">
														<% 
															def value = entityTemplates.key + "||" + field.name
															def selected = ( value == selectedValue );
														%>
														<option value="${value}" <g:if test="${selected}">selected="selected"</g:if>>
															${field.name} <g:if test="${field.preferredIdentifier}">[identifier]</g:if>
														</option>
													</g:if>
													
												</g:each>
											</optgroup>
										</g:if>
									</g:each>
								</select>
							</td>
						</g:each>
					</tr>
					
				</table>
			</div>
			
			<p>
				<input type="checkbox" name="editAfterwards" value="1" /> Edit data after importing
			</p>
		</g:form>
			
		<p class="options">
			<a href="#" onClick="submitForm( 'columns', 'previous' ); return false;" class="previous">Previous</a>
			<a href="#" onClick="submitForm( 'columns', 'next' ); return false;" class="next">Next</a>
		</p>
	</div>
</body>
</html>
