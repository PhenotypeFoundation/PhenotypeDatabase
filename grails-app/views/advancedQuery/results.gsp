<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Query results</title>
	<link rel="stylesheet" href="<g:resource dir="css" file="advancedQuery.css" />" type="text/css"/>
	<link rel="stylesheet" href="<g:resource dir="css" file="buttons.css" />" type="text/css"/>
	<g:javascript src="advancedQueryResults.js" />
</head>
<body>

<h1>Query results</h1>

<div class="searchoptions">
	${search.getNumResults()} <g:if test="${search.getNumResults() == 1}">result</g:if><g:else>results</g:else> found 
	<g:render template="criteria" model="[criteria: search.getCriteria()]" />
</div>
<g:if test="${search.getNumResults() > 0}">
	<% 
		def resultFields = search.getShowableResultFields();
		def extraFields = search.getShowableResultFieldNames(resultFields);
	%>
	<table id="searchresults" class="paginate">
		<thead>
			<tr>
				<th class="nonsortable"><input type="checkbox" id="checkAll" onClick="checkAllPaginated(this);" /></th>
				<th>Type</th>
				<th>Id</th>
				<g:each in="${extraFields}" var="fieldName">
					<th>${fieldName}</th>
				</g:each>
			</tr>
		</thead>
		<g:each in="${search.getResults()}" var="result">
			<tr>
				<td width="3%">
					<% /* 
						The value of this checkbox will be moved to the form (under this table) with javascript. This
						way the user can select items from multiple pages of the paginated result list correctly. See
						also http://datatables.net/examples/api/form.html and advancedQueryResults.js
					*/ %>
					<g:checkBox name="uuid" value="${result.giveUUID()}" checked="${false}" onClick="updateCheckAll(this);" />
				</td>			
				<td>${search.entity}</td>
				<td>${result.id}</td>
				<g:each in="${extraFields}" var="fieldName">
					<td>
						<% 
							def fieldValue = resultFields[ result.id ]?.get( fieldName );
							if( fieldValue ) { 
								if( fieldValue instanceof Collection ) {
									fieldValue = fieldValue.collect { it.toString() }.findAll { it }.unique().join( ', ' );
								} else {
									fieldValue = fieldValue.toString();
								}
							} else {
								fieldValue = "";
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
