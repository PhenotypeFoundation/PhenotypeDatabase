<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Query results</title>
	<link rel="stylesheet" href="<g:resource dir="css" file="advancedQuery.css" />" type="text/css"/>
	<g:javascript src="advancedQuery.js" />
</head>
<body>

<h1>Query results</h1>

<p>
	Your search for:
</p>
<ul id="criteria">
	<g:each in="${search.getCriteria()}" var="criterium">
		<li>
			<span class="entityfield">${criterium.entityfield}</span>
			<span class="operator">${criterium.operator}</span>
			<span class="value">${criterium.value}</span>
		</li>
	</g:each>
</ul>
<p> 
	resulted in ${search.getNumResults()} results.
</p>

<g:if test="${search.getNumResults() > 0}">

	<table id="searchresults">
		<thead>
			<tr>
				<th>Type</th>
				<th>Id</th>
			</tr>
		</thead>
		<g:each in="${search.getResults()}" var="result">
			<tr>
				<td>${search.entity}</td>
				<td>${result.id}</td>
			</tr>
		</g:each>
	</table>
</g:if>
<p>
	<g:link action="index">Search again</g:link>
</p>
</body>
</html>
