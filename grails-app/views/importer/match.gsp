<%! import grails.converters.JSON %>
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
				Match data
			</span>
			<g:render template="steps" model="[active: 'matchData']" />
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
			<span class="title">Match the rows and columns from your file</span> 
			Below you can specify where and how to store the data you provided.
		</span>
		 
		<g:form action="match" name="matchData">
			<g:hiddenField name="_action" />
			<g:hiddenField name="key" value="${sessionKey}" />
			
			<div id="data-with-headers" data-url="${g.createLink(action: 'datapreview')}" data-match-url="${g.createLink(action: 'matchHeaders')}">
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
