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
	Your search for studies with:
</p>
<ul id="criteria">
	<g:each in="${search.getCriteria()}" var="criterion">
		<li>
			<span class="entityfield">${criterion.entity}.${criterion.field}</span>
			<span class="operator">${criterion.operator}</span>
			<span class="value">${criterion.value}</span>
		</li>
	</g:each>
</ul>
<p> 
	resulted in ${search.getNumResults()} <g:if test="${search.getNumResults() == 1}">sample</g:if><g:else>samples</g:else>.
</p>


<g:if test="${search.getNumResults() > 0}">

	<table id="searchresults" class="paginate">
		<thead>
		<tr>
			<th>Study</th>
			<th>Name</th>
		</tr>
		</thead>
		<tbody>
		<g:each in="${search.getResults()}" var="sampleInstance" status="i">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

				<td><g:link controller="study" action="show" id="${sampleInstance?.parent?.id}">${sampleInstance?.parent?.title}</g:link></td>
				<td>${fieldValue(bean: sampleInstance, field: "name")}</td>
			</tr>
		</g:each>
		</tbody>
	</table>

</g:if>
<p>
	<g:link action="index">Search again</g:link>
</p>
</body>
</html>
