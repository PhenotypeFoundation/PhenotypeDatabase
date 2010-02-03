
<%@ page import="dbnp.studycapturing.Study" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
  <title><g:message code="default.show.label" args="[entityName]" /></title>
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

    <% protocolList = dbnp.studycapturing.Protocol.list() %>

    <div id="accordion">
      <a href="#"> Study Information </a>
        
       <div> Id : ${fieldValue(bean: studyInstance, field: "id")} <br>
       Template :<g:link controller="template" action="show" id="${studyInstance?.template?.id}">${studyInstance?.template?.encodeAsHTML()}</g:link><br>
       Start :<g:formatDate date="${studyInstance?.startDate}" /> <br>
      Sampling Events :
          <g:each in="${studyInstance.samplingEvents}" var="s"><ul>
            <li><g:link controller="samplingEvent" action="show" id="${s.id}">${s?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul><br>
       Last Updated :<g:formatDate date="${studyInstance?.lastUpdated}" /><br>
             Readers :<ul>
          <g:each in="${studyInstance.readers}" var="r">
            <li><g:link controller="user" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul> <br>
      Code : ${fieldValue(bean: studyInstance, field: "code")} <br>
      Editors : <ul>
          <g:each in="${studyInstance.editors}" var="e">
            <li><g:link controller="user" action="show" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul> <br>
     EC Code : ${fieldValue(bean: studyInstance, field: "ecCode")} <br>
     Research Question : ${fieldValue(bean: studyInstance, field: "researchQuestion")} <br>
     Title : ${fieldValue(bean: studyInstance, field: "title")} <br>
     Description : ${fieldValue(bean: studyInstance, field: "description")} <br>
     Owner :<g:link controller="user" action="show" id="${studyInstance?.owner?.id}">${studyInstance?.owner?.encodeAsHTML()}</g:link> <br>
      Date Created :<g:formatDate date="${studyInstance?.dateCreated}" /> <br>
       </div>

      <a href="#"> Subjects </a><div>
        <table>
          <tr>
            <td><b>Id </b></td>
            <td><b>Species</b></td>
            <td><b>Name</b></td>
          <g:each in="${studyInstance.template.subjectFields}" var="g">
            <td><b>
              <g:link controller="templateSubjectField" action="show" id="${g.id}">
              ${g}</b></td>
            </g:link>
          </g:each>
          </tr>

          <g:each in="${studyInstance.subjects}" var="s">
            <tr>
              <td><g:link controller="subject" action="show" id="${s.id}">${s.id}</g:link></td>
              <td>${s.species}</td>
              <td>${s.name}</td>

                <g:each in="${studyInstance.template.subjectFields}" var="g">
               <td>
              <% if (g.type==dbnp.studycapturing.TemplateFieldType.INTEGER){ %>
                  <% print s.templateIntegerFields.get(g.toString())  %>
              <% } %>
               <% if (g.type==dbnp.studycapturing.TemplateFieldType.STRINGLIST){ %>
                <% print s.templateStringFields.get(g.toString())  %>
              <% } %>

            </td>
          </g:each>
          </tr>
          </g:each>
          </table>
      </div>

       <a href="#"> Groups </a> <div><ul>
          <g:each in="${studyInstance.groups}" var="g">
            <li><g:link controller="subjectGroup" action="show" id="${g.id}">${g?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul> </div>

       <a href="#"> Protocols </a><div>
             <table>
          <tr>
            <td><b>Id </b></td>
            <td><b>Name</b></td>
            <td><b>Parameters</b></td>
            <td><b>Reference</b></td>
          </tr>
          <g:each in="${dbnp.studycapturing .Protocol.list()}" var="s">
            
            <% if  (studyInstance.events.eventDescription.protocol.contains(s)) { %>
           
            <tr>
              <td><g:link controller="protocol" action="show" id="${s.id}">${s.id}</g:link></td>
          <td>${s.name}</td>
          <td>
          <g:each in="${s.parameters}" var="p"><ul><li>
            <g:link controller="protocolParameter" action="show" id="${p.id}">${p.name}</g:link>
            </li></ul>
          </g:each>
          </td>
          <td>${s.reference}</td>
          </tr>
 <%  } %>

          </g:each>
             </table>
       </div>

      <a href="#"> Events </a><div>
          <table>
          <tr>
            <td><b>Event Description</b></td>
            <td><b>Subject</b></td>
            <td><b>Start Time</b></td>
            <td><b>End Time</b></td>
            <td><b>Duration</b></td>
          </tr>
          <g:each in="${studyInstance.events}" var="e">
            <tr>
              <td><g:link controller="event" action="show" id="${e.id}">  ${e.eventDescription.name}</g:link></td>
          <td>${e.subject.id}</td>
          <td>${e.startTime}</td>
          <td>${e.endTime}</td>
          <td>${e.getDurationString()}</td>
          </tr>
          </g:each>
          </table>
      </div>

      <a href="#"> Assays </a><div>
      </div>

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
