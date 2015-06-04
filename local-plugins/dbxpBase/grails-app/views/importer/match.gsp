<%! import grails.converters.JSON %>
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
				Match data
			</span>
			<g:render template="/importer/steps" model="[active: 'matchData']" />
		</h1>
		
		<g:render template="/common/flashmessages" />
		
		<span class="message info"> 
			<span class="title">Match the rows and columns from your file</span> 
			Below you can specify where and how to store the data you provided.
		</span>
		 
		<g:form action="match" name="matchData" params="${defaultParams}">
			<g:hiddenField name="_action" />
			<g:hiddenField name="key" value="${sessionKey}" />
			
			<div id="data-with-headers" data-url="${g.createLink(action: 'datapreview', params: defaultParams)}" data-match-url="${g.createLink(action: 'matchHeaders', params: defaultParams)}">
			</div>
			
			<g:select 
				name="example-header-select" class="header-select" 
				from="${headerOptions}" optionKey="id" optionValue="name" 
				noSelection="${["": "[Don't import]"]}"
				/>
						
			<br clear="all" />

			<p class="options">
				<a href="#" onClick="Importer.form.submit( 'matchData', 'previous' ); return false;" class="previous">Previous</a>
				<a href="#" onClick="Importer.form.submit( 'matchData', 'validate' ); return false;" class="validate">Validate</a>
				<a href="#" onClick="Importer.form.submit( 'matchData', 'import' ); return false;" class="import">Import</a>
			</p>
		</g:form>

		<r:script>
			Importer.match.initialize( '${sessionKey}' <g:if test="${savedMapping}">, ${savedMapping as JSON}</g:if> );
		</r:script>
	</div>
</body>
</html>
