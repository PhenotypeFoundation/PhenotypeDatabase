<%@ page import="dbnp.studycapturing.Study" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}"/>
	<title><g:message code="default.list.label" args="[entityName]"/></title>
</head>
<body>

<g:form action="list_extended" name="list_extended">
	<div class="body">
		<h1><g:message code="default.list.label" args="[entityName]"/></h1>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>

		<div class="list">
			<table>
				<thead>
				<tr>
					<th colspan="3"></th>
					<g:sortableColumn property="code" title="${message(code: 'study.code.label', default: 'Code')}"/>
					<th>Title</th>
					<th>Subjects</th>
					<th>Events</th>
					<th>Assays</th>
				</tr>
				</thead>
				<tbody>
				<g:each in="${studyInstanceList}" var="studyInstance" status="i">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

						<td><input type="checkbox" name="id" value="${studyInstance.id}" id="${studyInstance.title}"></td>
						<td><g:link action="show" id="${studyInstance?.id}"><img src='${fam.icon(name: 'application_form_magnify')}' border="0" alt="view study" /></g:link></td>
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
		</div>
		<div class="buttons">
			<sec:ifLoggedIn>
				<span class="button"><g:link class="create" controller="studyWizard" params="[jump:'create']"><g:message code="default.new.label" args="[entityName]"/></g:link></span>
				<span class="button"><a class="compare" href="#" onClick="$( 'form#list_extended' ).first().submit(); return false;">Compare selected studies</a></span>
			</sec:ifLoggedIn>
			
		</div>
		<div class="paginateButtons">
			<g:paginate max="10" total="${studyInstanceTotal}" prev="&laquo; Previous" next="&raquo; Next"/>
		</div>
	</div>
</g:form>
</body>
</html>
