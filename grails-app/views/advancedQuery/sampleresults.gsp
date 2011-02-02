<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Query results</title>
	<link rel="stylesheet" href="<g:resource dir="css" file="advancedQuery.css" />" type="text/css"/>
</head>
<body>

<h1>Query results</h1>

<p>
	Your search for samples with:
</p>
<g:render template="criteria" model="[criteria: search.getCriteria()]" />
<p> 
	resulted in ${search.getNumResults()} <g:if test="${search.getNumResults() == 1}">sample</g:if><g:else>samples</g:else>.
</p>

<g:if test="${search.getNumResults() > 0}">
	<% 
		def resultFields = search.getShowableResultFields();
		def extraFields = resultFields[ search.getResults()[ 0 ].id ]?.keySet();
	%>
	<table id="searchresults" class="paginate">
		<thead>
		<tr>
			<th>Study</th>
			<th>Name</th>
			<g:each in="${extraFields}" var="fieldName">
				<th>${fieldName}</th>
			</g:each>
		</tr>
		</thead>
		<tbody>
		<g:each in="${search.getResults()}" var="sampleInstance" status="i">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td><g:link controller="study" action="show" id="${sampleInstance?.parent?.id}">${sampleInstance?.parent?.title}</g:link></td>
				<td>${fieldValue(bean: sampleInstance, field: "name")}</td>
				<g:each in="${extraFields}" var="fieldName">
					<td>
						<% 
							def fieldValue = resultFields[ sampleInstance.id ]?.get( fieldName );
							if( fieldValue ) { 
								if( fieldValue instanceof Collection )
									fieldValue = fieldValue.collect { it.toString() }.findAll { it }.join( ', ' );
								else
									fieldValue = fieldValue.toString();
							}
						%>
						${fieldValue}
					</td>
				</g:each>
			</tr>
		</g:each>
		</tbody>
	</table>

</g:if>
<g:render template="resultbuttons" model="[queryId: queryId]" />
</body>
</html>
