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
				Store data
			</span>
			<g:render template="/importer/steps" model="[active: 'store']" />
		</h1>
		
		<g:render template="/common/flashmessages" />
		
		<g:if test="${validationErrors}">
			<p>
				${numLinesImported} line(s) from your file have been successfully imported. However, some lines results in validation errors:
			</p>
				 
			<div class="message errormessage">
				<span class="title">Validation results</span>
				<ul>
					<g:each var="error" in="${validationErrors}">
						<li>${error}</li>
					</g:each>
				</ul>
			</div>
		</g:if>
		<g:else>
			<div class="message okay">
				<span class="title">Data imported succesfully</span>
				${numLinesImported} line(s) from your file have been succesfully imported and no errors were found. 
			</div>
		</g:else>
		
		<p>
			You can choose to: <g:link url="${resultLink.url}">${resultLink.label}</g:link>,
			<g:if test="${editLink}">
				<g:link url="${editLink.url}">${editLink.label}</g:link>
			</g:if>
			or <g:link action="upload" params="${defaultParams + [importer:importInfo.importer]}">restart the importer</g:link>.
		</p>
					 

	</div>
</body>
</html>
