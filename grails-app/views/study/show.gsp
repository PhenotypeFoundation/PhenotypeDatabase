
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
<% def study_eventsDescription = [] %>
<% study_eventsDescription = studyInstance.events.eventDescription.unique() %>
<% study_eventsDescription.addAll(studyInstance.samplingEvents.eventDescription.unique()) %>
<% def study_events = [] %>
<% study_events = studyInstance.events.unique() %>
<% study_events.addAll(studyInstance.samplingEvents.unique()) %>

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
            <g:if test="${studyInstance.events.eventDescription.size()==0}">
              -
            </g:if>
            <g:else>
            ${studyInstance.events.eventDescription.unique().toString().replaceAll("]"," ").
          substring(1,studyInstance.events.eventDescription.unique().toString().size())}
            </g:else>
          <br>
          <b>Sampling Events </b>:
            <g:if test="${studyInstance.samplingEvents.eventDescription.size()==0}">
             -
            </g:if>
            <g:else>
             ${studyInstance.samplingEvents.eventDescription.unique().toString().replaceAll("]"," ").
                 substring(1,studyInstance.samplingEvents.eventDescription.unique().toString().size())}
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

              <g:each in="${studyInstance.subjects.findAll {it.template == template}}" var="s">
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
<% def protocol_list = [] %>
<% protocol_list= studyInstance.events.eventDescription.protocol.unique() %>
          <g:if test="${protocol_list.size()==0}">
            No protocols in this study
          </g:if>
          <g:else>
            <table>
            <tr>
              <td><b>Id </b></td>
              <td><b>Name</b></td>
              <td><b>Parameters</b></td>
              <td><b>Reference</b></td>
            </tr>
            <g:each in="${protocol_list}" var="protocol">
              <tr>
              <td><g:link controller="protocol" action="show" id="${protocol.id}">${protocol.id}</g:link></td>
              <td>${protocol.name}</td>
              <td>
              <g:each in="${protocol.parameters}" var="p">
                  <g:link controller="protocolParameter" action="show" id="${p.id}">${p.name}</g:link>
              </g:each>
              </td>
              <td>${protocol.reference}</td>
              </tr>

            </g:each>
          </table>
          </g:else>
        </div>

        <div id="events">
          <g:if test="${study_events.size()==0}">
           No events in this study
          </g:if>
          <g:else>
          <table>
            <tr>
              <td><b>Subject</b></td>
              <td><b>Start Time</b></td>
              <td><b>Duration</b></td>
              <td><b>Event Description</b></td>
              <td><b>Sampling Event</b></td>
              <td><b>Parameters</b></td>
            </tr>
            <g:each in="${study_events}" var="e">
              <tr>
                <td>-</td>
                <td>${e.getPrettyDuration(studyInstance.startDate,e.startTime)}</td>
                <td>${e.getPrettyDuration()}</td>
                <td><g:link controller="event" action="show" id="${e.id}">  ${e.eventDescription.name}</g:link></td>
              <td>
              <g:if test="${e instanceof dbnp.studycapturing.SamplingEvent}">
                <g:checkBox name="samplingEvent" disabled="${true}" value="${true}"/>
              </g:if>
              <g:else>
                <g:checkBox name="event" disabled="${true}" value="${false}"/>
              </g:else>
              </td>
              <td>
             <g:if test="${e.parameterStringValues.size()>0}">
<% def stringValues = e.parameterStringValues.toString().replaceAll("}"," ") %>
<% print stringValues.substring(1,stringValues.size()) %>
              </g:if>
              <g:elseif test="${e.parameterIntegerValues.size()>0}">
<% def integerValues = e.parameterInteger.toString().replaceAll("}"," ") %>
<% print integerValues.substring(1,integerValues.size()) %>
              </g:elseif>
              <g:elseif test="${e.parameterFloatValues.size()>0}">
<% def floatValues = e.parameterFloatValues.toString().replaceAll("}"," ") %>
<% print floatValues.substring(1,floatValues.size()) %>
              </g:elseif>
              </td>
              </tr>
            </g:each>
          </table>
          </g:else>
        </div>

        <div id="event-description">
          <g:if test="${study_eventsDescription.size()==0}">
            No events description in this study
          </g:if>
          <g:else>
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
            <g:each in="${study_eventsDescription}" var="e">
                <tr>
                  <td>${e.name} </td>
                <g:each in="${e.protocol.parameters}" var="p">
                  <td>${p.name}</td>
                  <td>${p.description}</td>
                  <td>${p.unit}</td>
                  <td>${p.reference}</td>
                  <g:if test="${(p.listEntries.size()==0)}" >
                    <td>-</td>
                  </g:if>
                  <g:else>
                    <td>${p.listEntries.toString().replaceAll("]"," ").substring(1,p.listEntries.toString().size())}</td>
                  </g:else>
                  <td>${p.type}</td>
                  </tr>
                </g:each>
            </g:each>
          </table>

          <g:form controller="eventDescription" action="create">
            <INPUT TYPE=submit name=submit Value="New Event Description">
          </g:form>
</g:else>
        </div>

        <div id="event-group">
          <g:if test="${studyInstance.eventGroups.size()==0}">
            No event groups in this study
          </g:if>
          <g:else>
          <table>
            <tr>
              <td><b>Name</b></td>
              <td colspan="${study_eventsDescription.size()}"><b>Events</b></td>
              <td><b>Subjects</b></td>
            </tr>
            <tr>
              <td></td>
                <g:each in="${study_eventsDescription}" var="list">
              <td>
                  <b>${list}</b>
          </td>
                </g:each>
          </tr>
            <g:each in="${studyInstance.eventGroups}" var="eventGroup">
            <tr>
              <td>${eventGroup.name}</td>

                <g:each in="${study_eventsDescription}" var="des">
                  <td>
              <g:each in="${eventGroup.events}" var="e">
                 <g:if test="${e.eventDescription==des}">
                   <g:if test="${e.parameterStringValues.size()>0}">
<% def stringValues = e.parameterStringValues.toString().replaceAll("}"," ") %>
<% print stringValues.substring(1,stringValues.size()) %>
              </g:if>
              <g:elseif test="${e.parameterIntegerValues.size()>0}">
<% def integerValues = e.parameterInteger.toString().replaceAll("}"," ") %>
<% print integerValues.substring(1,integerValues.size()) %>
              </g:elseif>
              <g:elseif test="${e.parameterFloatValues.size()>0}">
<% def floatValues = e.parameterFloatValues.toString().replaceAll("}"," ") %>
<% print floatValues.substring(1,floatValues.size()) %>
              </g:elseif>
                 </g:if>
                </g:each>
                 </td>
              </g:each>

              <td>${eventGroup.subjects.name.toString().replaceAll("]"," ").
    substring(1,eventGroup.subjects.name.toString().size())}</td>
          </tr>
            </g:each>
          </table>
          </g:else>
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
</body>
</html>
