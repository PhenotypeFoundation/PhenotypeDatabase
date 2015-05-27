<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Import data</title>
	
	<r:require modules="gscfimporter" />
</head>
<body>
	<div class="basicTabLayout importer uploadFile">
		<h1>
			<span class="truncated-title">
				Upload file
			</span>
			<g:render template="steps" model="[active: 'uploadFile']" />
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
			<span class="title">Upload file and specify parameters</span> 
			Below you can upload an excel file, csv file or tab separated file. 
			Please also specify the parameters for the import.
		</span>
		
		<g:if test="${flash.validationErrors}">
			<div class="errormessage">
				<g:each var="error" in="${flash.validationErrors}">
					${error.value}<br />
				</g:each>
			</div>
		</g:if>  
		 
		<g:form action="upload" name="uploadFile">
			<g:hiddenField name="_action" />
			<g:if test="${study?.id}">
				<g:hiddenField name="id" value="${study?.id}" />
			</g:if>
			<fieldset>
				<legend>Upload file</legend>
				
				<af:fileFieldElement name="file"
									 description="Choose a file"
									 id="importFileName"/>
	
				<div class="element">
					<div class="description">Use data from sheet</div>
					<div class="input">
						<g:select name="upload.sheetIndex" value="${refreshParams?.sheetIndex}"
								  from="${sheetList}"
								  optionKey="${{it-1}}"
								  onchange="Importer.updateDatamatrixPreview()"/>
					</div>
				</div>				
				<div class="element">
					<div class="description">Column header at line</div>
					<div class="input">
						<g:select name="upload.headerRow" value="${refreshParams?.headerRow}"
								  from="${1..9}"
								  optionKey="${{it-1}}" />
					</div>
				</div>
				
				<div class="element">
					<div class="description">Separator</div>
					<div class="input">
						<g:select name="upload.separator" value="${refreshParams?.separator}"
								  keys="${[ ",", ";", "\\t"]}"
								  from="${[ ",", ";", "{tab}"]}" />
					</div>
				</div>				
								
				<div class="element">
					<div class="description">Date format</div>
					<div class="input">
						<g:select name="upload.dateFormat" value="${refreshParams?.dateFormat}"
								  from="${['dd/MM/yyyy (EU/India/South America/North Africa/Asia/Australia)', 'yyyy/MM/dd (China/Korea/Iran/Japan)', 'MM/dd/yyyy (US)']}"
								  keys="${['dd/MM/yyyy','yyyy/MM/dd','MM/dd/yyyy']}"/>
					</div>
				</div>				
			</fieldset>

			<fieldset id="exampleData" style="display: none;">
				<legend>Data preview</legend>
				<div id="datapreview" data-url="${g.createLink(action: 'datapreview')}">
				</div>
			</fieldset>
			
			<g:if test="${importer.getParameters()}">
				<fieldset>
					<legend>Import</legend>
					<g:each in="${importer.parameters}" var="parameter">
						<div class="element">
							<div class="description">${parameter.label}</div>
							<div class="input">
								<input type="text" name="parameter.${parameter.name}" />
							</div>
						</div>				
					</g:each>
				</fieldset>			
			</g:if>		
			
			<br clear="all" />

			<p class="options">
				<g:link action="chooseType" class="previous">Previous</g:link>
				<a href="#" onClick="Importer.form.submit( 'uploadFile', 'next' ); return false;" class="next">Next</a>
			</p>
			
		</g:form>

	</div>
</body>
</html>
