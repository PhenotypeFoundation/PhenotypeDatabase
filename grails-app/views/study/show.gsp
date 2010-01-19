
<%@ page import="dbnp.studycapturing.Study" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
  <title><g:message code="default.show.label" args="[entityName]" /></title>
</head>
<body>


        <script type="text/javascript">
	 render "hello" 
      </script>


<resource:accordion skin="default" />
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
    <richui:accordion>
      <richui:accordionItem id="1" caption="Id"> ${fieldValue(bean: studyInstance, field: "id")} </richui:accordionItem>
      <richui:accordionItem id="1" caption="Template"> <g:link controller="template" action="show" id="${studyInstance?.template?.id}">${studyInstance?.template?.encodeAsHTML()}</g:link> </richui:accordionItem>
      <richui:accordionItem id="1" caption="Start Date"> <g:formatDate date="${studyInstance?.startDate}" /> </richui:accordionItem>
      <richui:accordionItem id="1" caption="Sampling Events">  <ul>
          <g:each in="${studyInstance.samplingEvents}" var="s">
            <li><g:link controller="samplingEvent" action="show" id="${s.id}">${s?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul> </richui:accordionItem>
      <richui:accordionItem id="1" caption="Subjects"> <ul>
          <g:each in="${studyInstance.subjects}" var="s">
            <li><g:link controller="subject" action="show" id="${s.id}">${s?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul> </richui:accordionItem>
      <richui:accordionItem id="1" caption="Events"> <ul>
          <g:each in="${studyInstance.events}" var="e">
            <li><g:link controller="event" action="show" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul>
      </richui:accordionItem>
      <richui:accordionItem id="1" caption="Last Updated"> <g:formatDate date="${studyInstance?.lastUpdated}" /> </richui:accordionItem>
      <richui:accordionItem id="1" caption="Readers">  <ul>
          <g:each in="${studyInstance.readers}" var="r">
            <li><g:link controller="user" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul> </richui:accordionItem>

      <richui:accordionItem id="1" caption="Code">${fieldValue(bean: studyInstance, field: "code")} </richui:accordionItem>
      <richui:accordionItem id="1" caption="Editors">     <ul>
          <g:each in="${studyInstance.editors}" var="e">
            <li><g:link controller="user" action="show" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul> </richui:accordionItem>
      <richui:accordionItem id="1" caption="EC Code">${fieldValue(bean: studyInstance, field: "ecCode")} </richui:accordionItem>
      <richui:accordionItem id="1" caption="Research Question">${fieldValue(bean: studyInstance, field: "researchQuestion")} </richui:accordionItem>
      <richui:accordionItem id="1" caption="Title">${fieldValue(bean: studyInstance, field: "title")} </richui:accordionItem>
      <richui:accordionItem id="1" caption="Description">${fieldValue(bean: studyInstance, field: "description")} </richui:accordionItem>
      <richui:accordionItem id="1" caption="Owner"> <g:link controller="user" action="show" id="${studyInstance?.owner?.id}">${studyInstance?.owner?.encodeAsHTML()}</g:link> </richui:accordionItem>
      <richui:accordionItem id="1" caption="Date Created"> <g:formatDate date="${studyInstance?.dateCreated}" /> </richui:accordionItem>
      <richui:accordionItem id="1" caption="Groups"><ul>
          <g:each in="${studyInstance.groups}" var="g">
            <li><g:link controller="subjectGroup" action="show" id="${g.id}">${g?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul> </richui:accordionItem>

    </richui:accordion>
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
</body>
</html>
