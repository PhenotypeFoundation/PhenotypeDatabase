<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Query results</title>
	<r:require module="advancedQuery" />
</head>
<body>

<h1>Search results: ${search.toString()}</h1>

<div class="searchoptions">
	${search.getNumResults()} <g:if test="${search.getNumResults() == 1}">assay</g:if><g:else>assays</g:else> found 
	<g:render template="criteria" model="[criteria: search.getCriteria()]" />
</div>
<g:if test="${search.getNumResults() > 0}">
	<table id="searchresults" class="datatables serverside sortable selectMulti" rel="${g.createLink(action:"results", id: search.id)}">
		<thead>
		<tr>
			<th>Name</th>
			<th>Study</th>
		</tr>
		</thead>
	</table>
	<g:render template="resultsform" />

</g:if>
<g:render template="resultbuttons" model="[queryId: queryId]" />
</body>
</html>
