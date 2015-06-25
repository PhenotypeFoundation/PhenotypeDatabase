<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
    <title>Study exporter</title>
</head>
<body>

  <g:form action="exportStudies" name="studyExportForm">
  <div class="body">
    <h1>Export studies<g:if test="${format}"> as ${format}</g:if></h1>
    <p>
    	Select the studies you want to export<g:if test="${format}"> in ${format} format</g:if>.
    </p>
    
  	<g:render template="/common/flashmessages" />

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
						<td><input type="checkbox" name="ids" value="${studyInstance.id}" id="${studyInstance.title}"></td>
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

   	<h3>Export as</h3> 
    <div class="buttons" style="padding-top:0px;">
    	<g:each in="${formats}" var="format">
    		<g:submitButton class="button-1" style="margin-right: 5px;" title="${format}" value="${format}" name="format" />
    	</g:each>
    </div>
  </div>

</g:form>

</body>
</html>
