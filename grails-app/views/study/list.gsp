<%@ page import="dbnp.studycapturing.Study" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}"/>
	<title><g:message code="default.list.label" args="[entityName]"/></title>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'tipTip.css')}"/>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.tipTip.minified.js')}"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			$("a.linktips").tipTip();
		});
	</script>
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
					<th>Treatment types</th>
					<th>Assays</th>
				</tr>
				</thead>
				<tbody>
				<g:each in="${studyInstanceList}" var="studyInstance" status="i">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						<td><input type="checkbox" name="id" value="${studyInstance.id}" id="${studyInstance.title}"></td>
						<td><g:link class="linktips" action="show" title="View this study" id="${studyInstance?.id}"><img class="icon searchIcon" src="${resource(dir: 'images/default_style', file: 'blank.gif')}" alt="" /></g:link></td>
						<td><g:if test="${studyInstance.canWrite(loggedInUser)}">
							<g:link class="edit linktips" title="Edit this study" controller="studyEdit" action="edit" id="${studyInstance?.id}">
                                <img class="icon editIcon" src="${resource(dir: 'images/default_style', file: 'blank.gif')}" alt="" /></g:link>
							</g:if><g:else><img src='${fam.icon(name: 'lock')}' border="0" alt="you have no write access to shis study" /></g:else> 
						</td>
                        <%--<td><g:if test="${studyInstance.canWrite(loggedInUser)}">
                            <g:link class="edit linktips" title="Edit this study with simple study wizard" controller="simpleWizard" action="index" id="${studyInstance?.id}">
                            <img src='${fam.icon(name: 'pencil')}' border="0" alt="Edit this study with simple study wizard" /></g:link>
							</g:if><g:else><img src='${fam.icon(name: 'lock')}' border="0" alt="you have no write access to shis study" /></g:else>
						</td>                                                                                                       --%>
						<td>${fieldValue(bean: studyInstance, field: "code")}</td>
						<td>
							${fieldValue(bean: studyInstance, field: "title")}
						</td>
						<td>
							<% def subjectCounts = studyInstance.getSubjectCountsPerSpecies() %>
							<g:if test="${!subjectCounts}">
								-
							</g:if>
							<g:else>
								${subjectCounts.collect { it.value + " " + it.key }.join( ", " )}							
							</g:else>
						</td>

						<td>
							<% def eventTemplates = studyInstance.giveEventTemplates() %>
							<g:if test="${eventTemplates.size()==0}">
								-
							</g:if>
							<g:else>
								${eventTemplates*.name.join(', ')}
							</g:else>
						</td>

						<td>
							<% def assayModules = studyInstance.giveUsedModules() %>
							<g:if test="${assayModules.size()==0}">
								-
							</g:if>
							<g:else>
								${assayModules*.name.join(', ')}
							</g:else>
						</td>

					</tr>
				</g:each>
				</tbody>
			</table>
		</div>
		<div class="buttons">
			<sec:ifLoggedIn>
				<span class="button"><g:link class="buttonBg add" title="Create a new study" controller="studyEdit" action="add"><g:message code="default.new.label" args="[entityName]"/></g:link></span>
				<span class="button"><a class="buttonBg compare" title="Compare the selected studies" href="#" onClick="$( 'form#list_extended' ).first().submit(); return false;">Compare selected studies</a></span>
			</sec:ifLoggedIn>
		</div>
		<div class="paginateButtons">
            <div class="pager">
			    <g:paginate max="10" total="${studyInstanceTotal}" prev="&laquo;Prev" next="Next&raquo;"/>
            </div>
		</div>
	</div>
</g:form>
</body>
</html>
