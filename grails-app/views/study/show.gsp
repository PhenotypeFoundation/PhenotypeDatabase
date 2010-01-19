
<%@ page import="dbnp.studycapturing.Study" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
  <title><g:message code="default.show.label" args="[entityName]" /></title>

        <my:jqueryui/>
      <script type="text/javascript">
	$(function() {
		$("#accordion").accordion();
	});
      </script>

</head>
<body>
<div class="nav">
  <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
  <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
  <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
</div>

<div class="body">
  <h1><g:message code="default.show.label" args="[entityName]" /></h1>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <div class="dialog">

    <div id="accordion">
      <a href="#"> Id </a> <div> ${fieldValue(bean: studyInstance, field: "id")} </div>
      <a href="#"> Template </a> <div> <g:link controller="template" action="show" id="${studyInstance?.template?.id}">${studyInstance?.template?.encodeAsHTML()}</g:link> </div>
      <a href="#"> Start Date </a> <div> <g:formatDate date="${studyInstance?.startDate}" /> </div>
      <a href="#"> Sampling Events </a> <div>  <ul>
          <g:each in="${studyInstance.samplingEvents}" var="s">
            <li><g:link controller="samplingEvent" action="show" id="${s.id}">${s?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul> </div>
      <a href="#"> Subjects </a><div> <ul>
          <g:each in="${studyInstance.subjects}" var="s">
            <li><g:link controller="subject" action="show" id="${s.id}">${s?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul> </div>
      <a href="#"> Events </a><div> <ul>
          <g:each in="${studyInstance.events}" var="e">
            <li><g:link controller="event" action="show" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul>
      </div>
      <a href="#"> Last Updated </a><div> <g:formatDate date="${studyInstance?.lastUpdated}" /> </div>
      <a href="#"> Readers </a> <div>  <ul>
          <g:each in="${studyInstance.readers}" var="r">
            <li><g:link controller="user" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul> </div>

      <a href="#"> Code </a><div> ${fieldValue(bean: studyInstance, field: "code")} </div>
      <a href="#"> Editors </a> <div> <ul>
          <g:each in="${studyInstance.editors}" var="e">
            <li><g:link controller="user" action="show" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul> </div>
     <a href="#"> EC Code </a> <div>${fieldValue(bean: studyInstance, field: "ecCode")} </div>
     <a href="#"> Research Question </a> <div>${fieldValue(bean: studyInstance, field: "researchQuestion")} </div>
     <a href="#"> Title </a><div> ${fieldValue(bean: studyInstance, field: "title")} </div>
      <a href="#"> Description </a> <div> ${fieldValue(bean: studyInstance, field: "description")} </div>
      <a href="#"> Owner </a><div> <g:link controller="user" action="show" id="${studyInstance?.owner?.id}">${studyInstance?.owner?.encodeAsHTML()}</g:link> </div>
      <a href="#"> Date Created </a><div> <g:formatDate date="${studyInstance?.dateCreated}" /> </div>
      <a href="#"> Groups </a> <div><ul>
          <g:each in="${studyInstance.groups}" var="g">
            <li><g:link controller="subjectGroup" action="show" id="${g.id}">${g?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul> </div>
  </div>
    </div>
  <br>
  <div class="buttons">
    <g:form>
      <g:hiddenField name="id" value="${studyInstance?.id}" />
      <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
      <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
    </g:form>
  </div>
</div>
</div>
</body>
</html>
