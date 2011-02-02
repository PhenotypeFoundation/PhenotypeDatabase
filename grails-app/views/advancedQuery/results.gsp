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
	Your search for:
</p>
<ul id="criteria">
	<g:each in="${search.getCriteria()}" var="criterion">
		<li>
			<span class="entityfield">${criterion.entityField()}</span>
			<span class="operator">${criterion.operator}</span>
			<span class="value">${criterion.value}</span>
		</li>
	</g:each>
</ul>
<p> 
	resulted in ${search.getNumResults()} results.
</p>

<g:if test="${search.getNumResults() > 0}">
	<% 
		def resultFields = search.getShowableResultFields();
		def extraFields = resultFields[ search.getResults()[ 0 ].id ]?.keySet();
	%>
	<table id="searchresults">
		<thead>
			<tr>
				<th>Type</th>
				<th>Id</th>
				<g:each in="${extraFields}" var="fieldName">
					<th>${fieldName}</th>
				</g:each>
			</tr>
		</thead>
		<g:each in="${search.getResults()}" var="result">
			<tr>
				<td>${search.entity}</td>
				<td>${result.id}</td>
				<g:each in="${extraFields}" var="fieldName">
					<td>
						<% 
							def fieldValue = resultFields[ result.id ]?.get( fieldName );
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
	</table>
</g:if>
<g:render template="resultbuttons" model="[queryId: queryId]" />

</body>
</html>
