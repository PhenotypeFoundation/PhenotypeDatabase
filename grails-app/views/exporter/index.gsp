
<%@ page import="dbnp.studycapturing.Study" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
    <title>SimpleTox Exporter</title>
</head>
<body>

  <g:form action="export">

  <div class="body">
    <h1>Export as SimpleTox</h1>
    <br> Select the study you want to export in SimpleTox format.<br><br>
    <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>

    <div class="list">
      <table>
          <thead>
              <tr>
                  <th></th>
                  <g:sortableColumn property="code" title="${message(code: 'study.code.label', default: 'Code')}" />
	              <th>Title</th>
                  <th>Subjects</th>
                  <th>Events</th>
                  <th>Assays</th>
              </tr>
          </thead>
          <tbody>
          <g:each in="${studyInstanceList}" var="studyInstance" status="i" >
              <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                  <td><input type="checkbox" name="${studyInstance.title}" id="${studyInstance.title}"></td>

                  <td><g:link action="show" id="${studyInstance.id}">${fieldValue(bean: studyInstance, field: "code")}</g:link></td>
	              <td>
		              ${fieldValue(bean: studyInstance, field: "title")}
	              </td>
                  <td>
                    <g:if test="${studyInstance.subjects.species.size()==0}">
                      -
                    </g:if>
                    <g:else>
                      <g:each in="${studyInstance.subjects.species.unique()}" var="currentSpecies" status="j">
                        <g:if test="${j > 0}">, </g:if>
                        <%= studyInstance.subjects.findAll { return it.species == currentSpecies; }.size() %>
                        ${currentSpecies}
                      </g:each>
                    </g:else>
                  </td>

                  <td>
                    <g:if test="${studyInstance.giveEventTemplates().size()==0}">
                      -
                    </g:if>
                    <g:else>
                      ${studyInstance.giveEventTemplates().name.join( ', ' )}
                    </g:else>
                  </td>

                  <td>
                    <g:if test="${studyInstance.assays.size()==0}">
                      -
                    </g:if>
                    <g:else>
                      ${studyInstance.assays.module.platform.unique().join( ', ' )}
                    </g:else>
                  </td>

              </tr>
          </g:each>
          </tbody>
      </table>
    </div>


    <div class="paginateButtons" id="button">
    </div>

<!--    <g:submitToRemote action="test" update="result" />-->

<!--    <g:submitToRemote action="export" update="[success:'message',failure:'error']" />-->

    <INPUT TYPE=submit name=submit Value="Export">

  </div>

    <div id="result">  </div>

</g:form>

</body>
</html>
