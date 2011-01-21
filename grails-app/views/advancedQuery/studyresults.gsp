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
	resulted in ${search.getNumResults()} <g:if test="${search.getNumResults() == 1}">study</g:if><g:else>studies</g:else>.
</p>

<g:if test="${search.getNumResults() > 0}">

	<table id="searchresults" class="paginate">
		<thead>
		<tr>
			<th colspan="2"></th>
			<th>Code</th>
			<th>Title</th>
			<th>Subjects</th>
			<th>Events</th>
			<th>Assays</th>
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
