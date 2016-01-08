<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Import data</title>
	
	<r:require modules="gscfimporter" />
</head>
<body>
	<div class="basicTabLayout importer uploadFile">
		<h1>
			<span class="truncated-title">
				Upload file: ${importer.identifier}
			</span>
			<g:render template="/importer/steps" model="[active: 'uploadFile']" />
		</h1>
		
		<g:render template="/common/flashmessages" />
		
		<span class="message info"> 
			<span class="title">Upload file and specify parameters</span> 
			Below you can upload an excel file, csv file or tab separated file. 
			Please also specify the parameters for the import.
		</span>
		 
		<g:form action="upload" name="uploadFile" params="${defaultParams}">
			<g:hiddenField name="_action" />
			<g:hiddenField name="importer" value="${importer.identifier}" />
			<fieldset id="uploadParameters" class="importerParameters">
				<legend>Upload file</legend>
				
				<af:fileFieldElement name="file"
									 description="Choose a file"
									 id="importFileName"
									 onUpload="Importer.upload.updateDataPreview" 
 									 value="${savedParameters?.file}"
 									 />
	
				<div class="element">
					<div class="description">Use data from sheet</div>
					<div class="input">
						<g:select name="upload.sheetIndex" value="${refreshParams?.sheetIndex}"
								  from="${1..50}"
								  optionKey="${{it-1}}" />
					</div>
					
					<div class="helpIcon"></div>
					<div class="helpContent">
						Choosing a sheet is only possible when uploading excel sheets. CSV or tab separated files only have a single sheet.
					</div>
				</div>				
				<div class="element">
					<div class="description">Column header at line</div>
					<div class="input">
						<g:select name="upload.headerRow" value="${savedParameters?.upload?.headerRow}"
								  from="${1..9}"
								  optionKey="${{it-1}}" />
					</div>
				</div>
				
				<div class="element">
					<div class="description">Separator</div>
					<div class="input">
						<g:select name="upload.separator" value="${savedParameters?.upload?.separator}"
								  keys="${[ ",", ";", "\\t"]}"
								  from="${[ ",", ";", "{tab}"]}" />
					</div>

					<div class="helpIcon"></div>
					<div class="helpContent">
						Choosing a separator is only applicable for text files (csv or tab separated). For excel files, this option will be ignored.
					</div>
				</div>				
								
				<div class="element">
					<div class="description">Date format</div>
					<div class="input">
						<g:select name="upload.dateFormat" value="${savedParameters?.upload?.dateFormat}"
								  from="${['dd/MM/yyyy (EU/India/South America/North Africa/Asia/Australia)', 'yyyy/MM/dd (China/Korea/Iran/Japan)', 'MM/dd/yyyy (US)']}"
								  keys="${['dd/MM/yyyy','yyyy/MM/dd','MM/dd/yyyy']}"/>
					</div>
				</div>
			</fieldset>

			<fieldset id="exampleData" style="display: none;">
				<legend>Data preview</legend>
				<div id="datapreview" data-url="${g.createLink(action: 'datapreview', params: defaultParams)}">
				</div>
				<g:img class="spinner" dir="images" file="spinner.gif" />
			</fieldset>
			
			<g:if test="${importerParameters}">
				<fieldset id="importerParameters" class="importerParameters">
					<legend>Parameters</legend>
					<g:each in="${importerParameters}" var="parameter">
						<g:if test="${parameter.type == 'hidden'}">
							<g:hiddenField name="parameter.${parameter.name}" value="${savedParameters?.parameter?.get(parameter.name)}"/>
						</g:if>
						<g:else>
							<div class="element">
								<div class="description">${parameter.label}</div>
								<div class="input">
									<g:if test="${parameter.type == 'select'}">
										<g:select name="parameter.${parameter.name}" from="${parameter.values}" optionKey="id" value="${savedParameters?.parameter?.get(parameter.name)}"/>
									</g:if>
									<g:elseif test="${parameter.type == 'templates'}">
										<g:select rel="template" entity="${importer.getEncodedEntityName()}" data-entity="${importer.entity.name}" name="parameter.${parameter.name}" from="${parameter.values}" optionKey="id" value="${savedParameters?.parameter?.get(parameter.name)}"/>
									</g:elseif>
									<g:elseif test="${parameter.type == 'checkbox'}">
										<g:checkBox name="parameter.${parameter.name}" value="${savedParameters?.parameter?.get(parameter.name)}"/>
									</g:elseif>
									<g:else>
										<input type="text" name="parameter.${parameter.name}" value="${savedParameters?.parameter?.get(parameter.name)}" />
									</g:else>
								</div>
							</div>
						</g:else>				
					</g:each>
				</fieldset>			
			</g:if>		
			
			<br clear="all" />

			<p class="options">
				<g:link action="chooseType" params="${defaultParams}" class="previous">Previous</g:link>
				<a id="next-match" href="#" onClick="Importer.form.submit( 'uploadFile', 'match' ); return false;" class="next">Next</a>
				<a id="next-exact" href="#" onClick="Importer.form.submit( 'uploadFile', 'exact' ); return false;" class="next">Next</a>
			</p>

		</g:form>

		<r:script>
			Importer.upload.initialize();
		</r:script>
	</div>
</body>
</html>
