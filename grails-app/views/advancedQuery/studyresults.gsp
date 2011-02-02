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
	Your search for studies with:
</p>
<g:render template="criteria" model="[criteria: search.getCriteria()]" />
<p> 
	resulted in ${search.getNumResults()} <g:if test="${search.getNumResults() == 1}">study</g:if><g:else>studies</g:else>.
</p>
<g:if test="${search.getNumResults() > 0}">
	<% 
		def resultFields = search.getShowableResultFields();
		def extraFields = resultFields[ search.getResults()[ 0 ].id ]?.keySet();
	%>

	<table id="searchresults" class="paginate">
		<thead>
		<tr>
			<th colspan="2"></th>
			<th>Code</th>
			<th>Title</th>
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

				<td><g:link controller="study" action="show" id="${studyInstance?.id}"><img src='${fam.icon(name: 'application_form_magnify')}' border="0" alt="view study" /></g:link></td>
				<td><g:if test="${studyInstance.canWrite(loggedInUser)}"><g:link class="edit" controller="studyWizard" params="[jump:'edit']" id="${studyInstance?.id}"><img src='${fam.icon(name: 'application_form_edit')}' border="0" alt="edit study" /></g:link></g:if><g:else><img src='${fam.icon(name: 'lock')}' border="0" alt="you have no write access to shis study" /></g:else> </td>
				<td>${fieldValue(bean: studyInstance, field: "code")}</td>
				<td>
					${fieldValue(bean: studyInstance, field: "title")}
				</td>
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
