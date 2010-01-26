
<%@ page import="dbnp.studycapturing.Study" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
  <title><g:message code="default.list.label" args="[entityName]" /></title>
</head>
<body>

 <!--g:form url="[action:'list_extended',controller:'study']"-->
  <g:form action="list_extended.gsp">

  <div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
  </div>
  <div class="body">
    <h1><g:message code="default.list.label" args="[entityName]" /></h1>
    <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>
    <div class="list">

      <g:each in="${studyInstanceList}" status="i" var="studyInstance">
        <br>
        <table>
          <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <td>
              <input type="checkbox" name="${studyInstance.title}" id="${studyInstance.title}"></td>

            <td width=200>
          <g:link action="show" id="${studyInstance.id}">
${message(code: 'study.id.label', default: 'Id')} :
${fieldValue(bean: studyInstance, field: "id")}</g:link></td>

          <td width=300>
${message(code: 'study.template.label', default: 'Template')} :
${fieldValue(bean: studyInstance, field: "template")}</td>

          <td width=500>
${message(code: 'study.startDate.label', default: 'Start Date')} :
          <g:formatDate date="${studyInstance.startDate}" /></td>

          <td width=500>
${message(code: 'study.lastUpdated.label', default: 'Last Updated')} :
          <g:formatDate date="${studyInstance.lastUpdated}" /></td>

          <td width=300>
${message(code: 'study.code.label', default: 'Code')} :
${fieldValue(bean: studyInstance, field: "code")}</td>

          <td width=300>
${message(code: 'study.ecCode.label', default: 'Ec Code')} :
${fieldValue(bean: studyInstance, field: "ecCode")}</td>

          </tr>
        </table>
      </g:each>
      </table>
    </div>
    <div class="paginateButtons">
      <g:paginate total="${studyInstanceTotal}" />
      <br>
      <INPUT TYPE=submit name=submit Value="Compare selected studies">
    </div>
  </div>
</g:form>
</body>
</html>
