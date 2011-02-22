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
	${search.getNumResults()} <g:if test="${search.getNumResults() == 1}">study</g:if><g:else>studies</g:else> found 
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
			<th>Title</th>
			<th>Code</th>
			<th>Subjects</th>
			<th>Events</th>
			<th>Assays</th>
			<g:each in="${extraFields}" var="fieldName">
				<th>${fieldName}</th>
			</g:each>			
		</tr>
		</thead>
		<tbody>
		<g:each in="${search.getResults()}" var="studyInstance" status="i">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td width="3%">
					<% /* 
						The value of this checkbox will be moved to the form (under this table) with javascript. This
						way the user can select items from multiple pages of the paginated result list correctly. See
						also http://datatables.net/examples/api/form.html and advancedQueryResults.js
					*/ %>
					<g:checkBox name="id" value="${studyInstance.id}" checked="${false}" onClick="updateCheckAll(this);" />
				</td>
				<td>
					<g:link controller="study" action="show" id="${studyInstance?.id}">${fieldValue(bean: studyInstance, field: "title")}</g:link>
					
				</td>
				<td>${fieldValue(bean: studyInstance, field: "code")}</td>
				<td>
					<g:if test="${studyInstance.subjects.species.size()==0}">
						-
					</g:if>
					<g:else>
						<g:each in="${studyInstance.subjects.species.unique()}" var="currentSpecies" status="j">
							<g:if test="${j > 0}">,</g:if>
							<%=studyInstance.subjects.findAll { return it.species == currentSpecies; }.size()%>
							${currentSpecies}
						</g:each>
					</g:else>
				</td>

				<td>
					<g:if test="${studyInstance.giveEventTemplates().size()==0}">
						-
					</g:if>
					<g:else>
						${studyInstance.giveEventTemplates().name.join(', ')}
					</g:else>
				</td>

				<td>
					<g:if test="${studyInstance.assays.size()==0}">
						-
					</g:if>
					<g:else>
						${studyInstance.assays.module.platform.unique().join(', ')}
					</g:else>
				</td>
				<g:each in="${extraFields}" var="fieldName">
					<td>
						<% 
							def fieldValue = resultFields[ studyInstance.id ]?.get( fieldName );
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
