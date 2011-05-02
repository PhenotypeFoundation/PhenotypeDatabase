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

<h1>Search results</h1>

<div class="searchoptions">
	${search.getNumResults()} <g:if test="${search.getNumResults() == 1}">assay</g:if><g:else>assays</g:else> found 
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
			<th>Name</th>
			<th>Study</th>
			<g:each in="${extraFields}" var="fieldName">
				<th>${fieldName}</th>
			</g:each>
		</tr>
		</thead>
		<tbody>
		<g:each in="${search.getResults()}" var="assayInstance" status="i">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td width="3%">
					<% /* 
						The value of this checkbox will be moved to the form (under this table) with javascript. This
						way the user can select items from multiple pages of the paginated result list correctly. See
						also http://datatables.net/examples/api/form.html and advancedQueryResults.js
					*/ %>
					<g:checkBox name="uuid" value="${assayInstance.giveUUID()}" checked="${false}" onClick="updateCheckAll(this);" />
				</td>
				<td>
				  	<g:if test="${assayInstance.module.openInFrame == null || assayInstance.module.openInFrame == Boolean.TRUE}">
			          <jumpbar:link frameSource="${assayInstance.module.url}/assay/showByToken/${assayInstance.giveUUID()}" pageTitle="${assayInstance.module.name}">
						${fieldValue(bean: assayInstance, field: "name")}
					  </jumpbar:link>
					 </g:if>
					 <g:else>
					 	<g:link url="${assayInstance.module.url}/assay/showByToken/${assayInstance.giveUUID()}">
					 	${fieldValue(bean: assayInstance, field: "name")}
					 	</g:link>
					 </g:else>
				</td>
				<td><g:link controller="study" action="show" id="${assayInstance?.parent?.id}">${assayInstance?.parent?.title}</g:link></td>
				<g:each in="${extraFields}" var="fieldName">
					<td>
						<% 
							def fieldValue = resultFields[ assayInstance.id ]?.get( fieldName );
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
		</tbody>
	</table>
	<g:render template="resultsform" />

</g:if>
<g:render template="resultbuttons" model="[queryId: queryId]" />
</body>
</html>
