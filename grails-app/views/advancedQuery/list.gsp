<%@ page import="dbnp.query.*" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Previous queries</title>
	<link rel="stylesheet" href="<g:resource dir="css" file="advancedQuery.css" />" type="text/css"/>
</head>
<body>

<h1>Previous queries</h1>

<g:if test="${searches.size() > 0}">
	<table id="searchresults">
		<thead>
			<tr>
				<th></th>
				<th>#</th>
				<th>Type</th>
				<th>Criteria</th>
				<th># results</th>
				<th>time</th>
				<th></th>
				<th></th>
			</tr>
		</thead>
		<g:each in="${searches}" var="search">
			<tr>
				<td><g:checkBox name="queryId" value="${search.id}" checked="${false}" /></td>
				<td>${search.id}</td>
				<td>${search.entity}</td>
				<td>
					<g:each in="${search.getCriteria()}" var="criterion" status="j">
						<g:if test="${j > 0}">, </g:if>
						<span class="entityfield">${criterion.entityField()}</span>
						<span class="operator">${criterion.operator}</span>
						<span class="value">
							<g:if test="${criterion.value instanceof Search}">
								<g:link action="show" id="${criterion.value.id}">${criterion.value}</g:link>
							</g:if>
							<g:else>
								${criterion.value}
							</g:else>
						</span>
					</g:each>
				</td>
				<td>${search.getNumResults()}</td>
				<td><g:formatDate date="${search.executionDate}" format="HH:mm" /></td>
				<td><g:link action="show" id="${search.id}">Show</g:link>
				<td><g:link action="discard" id="${search.id}">Discard</g:link>
			</tr>
		</g:each>
	</table>
</g:if>
<p>
	<g:link action="index">Search again</g:link>
</p>
</body>
</html>
