
<%@ page import="dbnp.studycapturing.Study" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
</head>
<body>

  <g:form action="list_extended">

  <div class="body">
    <h1><g:message code="default.list.label" args="[entityName]" /></h1>
    <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>

    <div class="list">
      <table>
          <thead>
              <tr>
                  <th></th>
                  <g:sortableColumn property="title" title="${message(code: 'study.title.label', default: 'Title')}" />
                  <th>Subjects</th>
                  <th>Events</th>
                  <th>Assays</th>
              </tr>
          </thead>
          <tbody>
          <g:each in="${studyInstanceList}" var="studyInstance" status="i" >
              <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                  <td><input type="checkbox" name="${studyInstance.title}" id="${studyInstance.title}"></td>

                  <td><g:link action="show" id="${studyInstance.id}">${fieldValue(bean: studyInstance, field: "title")}</g:link></td>

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
    <div class="buttons">
        <span class="button"><g:link controller="wizard" class="create" action="index"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${studyInstanceTotal}" prev="&laquo; Previous" next="&raquo; Next" />
        <br>
        <INPUT TYPE=submit name=submit Value="Compare selected studies">
    </div>
  </div>
</g:form>
</body>
</html>
