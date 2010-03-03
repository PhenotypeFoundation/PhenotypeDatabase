
<%@ page import="dbnp.studycapturing.Study" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
  <title><g:message code="default.show.label" args="[entityName]" /></title>
      <script type="text/javascript">
	$(function() {
		$("#tabs").tabs();
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



      <div id="tabs">
      <ul>
        <li><a href="#study">Study Information</a></li>
        <li><a href="#subjects">Subjects</a></li>
        <li><a href="#groups">Groups</a></li>
        <li><a href="#protocols">Protocols</a></li>
        <li><a href="#events">Events</a></li>
        <li><a href="#event-description">Event Description</a></li>
        <li><a href="#event-group">Event Groups</a></li>
        <li><a href="#assays">Assays</a></li>
      </ul>

    <div id="study">
        
         <b> Id </b>: ${fieldValue(bean: studyInstance, field: "id")} <br>
         <b>Template </b>:<g:link controller="template" action="show" id="${studyInstance?.template?.id}">${studyInstance?.template?.encodeAsHTML()}</g:link><br>
         <b> Start </b>:<g:formatDate date="${studyInstance?.startDate}" /> <br>
         <b> Events </b>:
         <% def eventList = [] %>

          <g:each in="${studyInstance.events}" var="s">
            <%  eventList.add(s.eventDescription) %>
          </g:each>

         <g:if test="${eventList.size()==0}">
          -
         </g:if>

         <g:else>
           <% def sampEvent = eventList.get(0).name %>
           ${sampEvent}
         <g:each in="${eventList}" var="samplingEvent">
           <g:if test="${(samplingEvent.name!=sampEvent)}">
            ${samplingEvent.name}
         </g:if>
          </g:each>
         </g:else>
           <br>


         <b>Sampling Events </b>:
           <% def tmpList = [] %>

          <g:each in="${studyInstance.samplingEvents}" var="s">
            <%  tmpList.add(s.eventDescription) %>
          </g:each>

         <g:if test="${tmpList.size()==0}">
          -
         </g:if>

         <g:else>
           <% def sampEvent = tmpList.get(0).name %>
           ${sampEvent}
         <g:each in="${tmpList}" var="samplingEvent">
           <g:if test="${(samplingEvent.name!=sampEvent)}">
            ${samplingEvent.name}
         </g:if>
          </g:each>
         </g:else>
           <br>
        <b>Last Updated </b>:<g:formatDate date="${studyInstance?.lastUpdated}" /><br>
        <b>Readers </b>:

        <g:if test="${studyInstance.readers.size()==0}">
          -
        </g:if>
        <g:else>
        <ul>
          <g:each in="${studyInstance.readers}" var="r">
            <li><g:link controller="user" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul>
        </g:else>
          <br>
        <b>Code </b>: ${fieldValue(bean: studyInstance, field: "code")} <br>
        <b>Editors </b>:
          <g:if test="${studyInstance.editors.size()==0}">
          -
        </g:if>
        <g:else>
        <ul>
          <g:each in="${studyInstance.editors}" var="e">
            <li><g:link controller="user" action="show" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
          </g:each>
        </ul>
        </g:else>
          <br>
        <b>EC Code </b>: ${fieldValue(bean: studyInstance, field: "ecCode")} <br>
        <b>Research Question </b>: ${fieldValue(bean: studyInstance, field: "researchQuestion")} <br>
        <b>Title </b>: ${fieldValue(bean: studyInstance, field: "title")} <br>
        <b>Description </b>: ${fieldValue(bean: studyInstance, field: "description")} <br>
        <b>Owner </b>:<g:link controller="user" action="show" id="${studyInstance?.owner?.id}">${studyInstance?.owner?.encodeAsHTML()}</g:link> <br>
        <b>Date Created </b>:<g:formatDate date="${studyInstance?.dateCreated}" /> <br>
       </div>

       <div id="subjects">
	   <g:each in="${studyInstance.giveSubjectTemplates()}" var="template">
        <table>
          <tr>
            <td><b>Id </b></td>
            <td><b>Species</b></td>
            <td><b>Name</b></td>
          <g:each in="${template.fields}" var="g">
            <td><b>
              <g:link controller="templateField" action="show" id="${g.id}">
              ${g}</b></td>
            </g:link>
          </g:each>
          </tr>

          <g:each in="${studyInstance.subjects.findAll { it.template == template}}" var="s">
            <tr>
              <td><g:link controller="subject" action="show" id="${s.id}">${s.id}</g:link></td>
              <td>${s.species}</td>
              <td>${s.name}</td>
              <g:each in="${template.fields}" var="g">
               <td>
                  <% print s.getFieldValue(g.toString())  %>
               </td>
          </g:each>
            </tr>
          </g:each>

          </table>

	  </g:each>
      </div>

        <div id="groups">
         <g:if test="${studyInstance.groups.size()==0}">
           No groups in this study
         </g:if>
         <g:else>
          <g:each in="${studyInstance.groups}" var="g">
            ${g.name}
          </g:each>
         </g:else>
         </div>

        <div id="protocols">
             <table>
          <tr>
            <td><b>Id </b></td>
            <td><b>Name</b></td>
            <td><b>Parameters</b></td>
            <td><b>Reference</b></td>
          </tr>

             <% def protocol_list = [] %>
          <% def tmp_protocol = studyInstance.events.eventDescription.protocol.get(0) %>
          <% def tmpBis_protocol = studyInstance.samplingEvents.eventDescription.protocol.get(0) %>
          <% protocol_list.add(tmp_protocol) %>
          <% protocol_list.add(tmpBis_protocol) %>

            <g:each in="${studyInstance.events.eventDescription.protocol}" var="s">

          <% if (tmp_protocol!=s) { %>
            <% protocol_list.add(s) %>
            <%}%>
          </g:each>

          <g:each in="${studyInstance.samplingEvents.eventDescription.protocol}" var="s">

          <% if (tmpBis_protocol!=s) { %>
            <% protocol_list.add(s) %>
            <%}%>
          </g:each>


            <g:each in="${protocol_list}" var="protocol">
            <tr>
              <td><g:link controller="protocol" action="show" id="${protocol.id}">${protocol.id}</g:link></td>
          <td>${protocol.name}</td>
          <td>
          <g:each in="${protocol.parameters}" var="p"><ul><li>
            <g:link controller="protocolParameter" action="show" id="${p.id}">${p.name}</g:link>
            </li></ul>
          </g:each>
          </td>
          <td>${protocol.reference}</td>
          </tr>

          </g:each>
             </table>
       </div>

        <div id="events">
          <table>
          <tr>
            
            <td><b>Subject</b></td>
            <td><b>Start Time</b></td>
            <td><b>Duration</b></td>
            <td><b>Event Description</b></td>
            <td><b>Sampling Event</b></td>
            <td><b>Parameters</b></td>
          </tr>
          <g:each in="${studyInstance.events}" var="e">
            <tr>
          <td>-</td>
          <td>${e.getPrettyDuration(studyInstance.startDate,e.startTime)}</td>
          <td>${e.getPrettyDuration()}</td>
           <td><g:link controller="event" action="show" id="${e.id}">  ${e.eventDescription.name}</g:link></td>
            <td><g:checkBox name="event" disabled="${true}" value="${false}"/></td>

            <g:each in="${e.eventDescription.protocol.parameters}" var="param">
          <td>
            ${param.name} : ${param.listEntries}
          </td>
            </g:each>
            </tr>
          </g:each>

          <g:each in="${studyInstance.samplingEvents}" var="e">
            <tr>
          <td>-</td>
          <td>${e.getPrettyDuration(studyInstance.startDate,e.startTime)}</td>
          <td>${e.getPrettyDuration()}</td>
           <td><g:link controller="event" action="show" id="${e.id}">  ${e.eventDescription.name}</g:link></td>
            <td><g:checkBox name="samplingEvent" disabled="${true}" value="${true}"/></td>

            <g:each in="${e.eventDescription.protocol.parameters}" var="param">
          <td>
            ${param.name} : ${param.listEntries}
          </td>
            </g:each>
          </tr>
          </g:each>

          </table>
      </div>

        <div id="event-description">
          <table>
          <tr>

            <td><b>Event Name</b></td>
            <td><b>Parameters </b></td>
          </tr>
          <tr>
            <td><b></b></td>
            <td><b>Name</b></td>
            <td><b>Description</b></td>
            <td><b>Unit</b></td>
            <td><b>Reference</b></td>
            <td><b>Options</b></td>
            <td><b>Type</b></td>
          </tr>
          <g:each in="${dbnp.studycapturing.EventDescription.list()}" var="e">
          <g:if test="${(studyInstance.events.eventDescription.contains(e))}" >
            <tr>
              <td>${e.name} </td></tr><tr>
            <g:each in="${e.protocol.parameters}" var="p">
              <td></td>
          <td>${p.name}</td>
          <td>${p.description}</td>
          <td>${p.unit}</td>
          <td>${p.reference}</td>
          <g:if test="${(p.listEntries.size()==0)}" >
          <td>-</td>
            </g:if>
          <g:else>
          <td>${p.listEntries}</td>
          </g:else>
          <td>${p.type}</td>
            </tr>
            </g:each>
            </g:if>
            </g:each>

           <g:each in="${dbnp.studycapturing.EventDescription.list()}" var="e">
          <g:if test="${(studyInstance.samplingEvents.eventDescription.contains(e))}" >
            <tr>
              <td>${e.name} </td></tr><tr>
            <g:each in="${e.protocol.parameters}" var="p">
              <td></td>
          <td>${p.name}</td>
          <td>${p.description}</td>
          <td>${p.unit}</td>
          <td>${p.reference}</td>
          <g:if test="${(p.listEntries.size()==0)}" >
          <td>-</td>
            </g:if>
          <g:else>
          <td>${p.listEntries}</td>
          </g:else>
          <td>${p.type}</td>
            </tr>
            </g:each>
            </g:if>
            </g:each>

          </table>

        <g:form controller="eventDescription" action="create">
         <INPUT TYPE=submit name=submit Value="New Event Description">
    </g:form>

      </div>

        <div id="event-group">
        </div>

        <div id="assays">
        <g:if test="${studyInstance.assays.size()==0}">
          No assays in this study
        </g:if>
        <g:else>
          <table>
            <tr>
              <td width="100"><b>Assay Name</b></td>
              <td width="100"><b>Module</b></td>
              <td><b>Type</b></td>
              <td width="150"><b>Platform</b></td>
              <td><b>Url</b></td>
              <td><b>Samples</b></td>
            </tr>
          <g:each in="${studyInstance.assays}" var="assay">
            <tr>
            <td>${assay.name}</td>
            <td>${assay.module.name}</td>
            <td>${assay.module.type}</td>
            <td>${assay.module.platform}</td>
            <td>${assay.module.url}</td>
            <td>
              <g:each in="${assay.samples}" var="assaySample">
                ${assaySample.name}<br>
              </g:each>
            </td>
            </tr>
          </g:each>

          </table>
        </g:else>
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
