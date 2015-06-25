<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Import data</title>
	
	<r:require modules="gscfimporter" />
</head>
<body>
	<div class="basicTabLayout importer chooseImportType">
		<h1>
			<span class="truncated-title">
				Choose the type of import
			</span>
			<g:render template="/importer/steps" model="[active: 'chooseType']" />
		</h1>
		
		<g:render template="/common/flashmessages" />
		
		<span class="message info"> 
			<span class="title">Choose the type of import</span> 
			The Phenotype Database is capable of importing many types of data. Please select the type
			of data you want to import below.
		</span>
		 
		<g:form action="chooseType" name="chooseImportType" params="${defaultParams}">
			<g:hiddenField name="_action" />
			<div class="element">
				<div class="description">Type of import</div>
				<div class="input">
					<g:select name="importer" from="${importers}" />
				</div>
			</div>
				
			<br clear="all" />

			<p class="options">
				<a href="#" onClick="Importer.form.submit( 'chooseImportType', 'next' ); return false;" class="next">Next</a>
			</p>
		</g:form>
	</div>
</body>
</html>
