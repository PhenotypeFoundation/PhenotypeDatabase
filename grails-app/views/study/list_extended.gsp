
<%@ page import="dbnp.studycapturing.Study" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
  <title><g:message code="default.list.label" args="[entityName]" /></title>

  <my:jqueryui/>
  <script type="text/javascript">
    $(function() {
            $("#tabs").tabs();
    });
  </script>

</head>
<body>

  <% studyList = dbnp.studycapturing.Study.list() %>
  <% def att_list = ['template','startDate','events','samplingEvents','lastUpdated','readers','code','editors','ecCode','researchQuestion','title','description','owner','dateCreated'] %>
  <% def selectedStudies = [] %>
  <% def tmpList = [] %>

  <div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
    <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
  </div>

  <div class="body">
    <h1>Studies Comparison</h1>
    <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>

    <% params.each{key,values-> %>
    <% if (values=="on"){ %>
      <% tmpList.add(key) %>
    <%  } }%>

    <% for (i in studyList) {%>
    <% if (tmpList.contains(i.getTitle())) { %>
      <% selectedStudies.add(i) %>
     <% }} %>

    <% if (selectedStudies.size()>0) {%>


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
      <br>
      <table>
      <tr>
        <td></td>
        <g:each in="${selectedStudies}" status="j" var="studyIns">
          <td width="50%"><b>${studyIns.title}</b></td>
        </g:each>
      </tr>
      <tr>
        <td><b>Id</b></td>
        <g:each in="${selectedStudies}" status="k" var="studyIns">
          <td><g:link action="show" id="${studyIns.id}">
${fieldValue(bean: studyIns, field: "id")}</g:link></td>
        </g:each>
      </tr>

      <g:each in="${att_list}" var="att">
      <tr>
        <td><b>${att[0].toUpperCase()+att.substring(1)}</b></td>
        <g:each in="${selectedStudies}" status="k" var="studyIns">

   <g:if test="${att == 'events'}">
     <td>
  <% def eventList = [] %>
             <g:each in="${studyIns.events}" var="s">
              <%  eventList.add(s.eventDescription) %>
             </g:each>
  <g:if test="${eventList.size()==0}">
          -
         </g:if>
        <g:else>
           <% def sampEvent = eventList.get(0).name %>
           ${sampEvent}
         <g:each in="${eventList}" var="event">
           <g:if test="${(event.name!=sampEvent)}">
            ${event.name}
         </g:if>
          </g:each>
         </g:else>
          </td>
        </g:if>

<g:elseif test="${att == 'samplingEvents'}">
     <td>
  <% def SampeventList = [] %>
             <g:each in="${studyIns.samplingEvents}" var="s">
              <%  SampeventList.add(s.eventDescription) %>
             </g:each>
  <g:if test="${SampeventList.size()==0}">
          -
         </g:if>
        <g:else>
           <% def samplEvent = SampeventList.get(0).name %>
           ${samplEvent}
         <g:each in="${SampeventList}" var="samplingEvent">
           <g:if test="${(samplingEvent.name!=samplEvent)}">
            ${samplingEvent.name}
         </g:if>
          </g:each>
         </g:else>
          </td>
</g:elseif>

          <g:else>
<td>${fieldValue(bean: studyIns, field: att)}</td>
          </g:else>
        </g:each>
      </tr>
      </g:each>

      </table>
    </div>

          <div id="subjects">
       
       <table border="2">
         <tr>
         <g:each in="${selectedStudies}" var="study">
           <td><center><b>${study.title}</b></center></td>
         </g:each>
       </tr>

         <tr>
         <g:each in="${selectedStudies}" var="stud">
             <td>


  <g:each in="${stud.giveSubjectTemplates()}" var="template">
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

          <g:each in="${stud.subjects.findAll { it.template == template}}" var="s">
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



             </td>
           </g:each>
         </tr>
       </table>
       
      </div>

          <div id="groups">
<table border="2">
  <tr>
         <g:each in="${selectedStudies}" var="study">
           <td width="50%"><center><b>${study.title}</b></center></td>
         </g:each>
       </tr>
         <tr>
         <g:each in="${selectedStudies}" var="study">
           <td>
  <g:if test="${study.groups.size()==0}"> No groups in this study </g:if>
  <g:else><center><b>${study.groups}</b></center></g:else>
         </td>
         </g:each>
       </tr>
</table>
        </div>

          <div id="protocols">
         <table border="2">
         <tr>
         <g:each in="${selectedStudies}" var="study">
           <td width="50%"><center><b>${study.title}</b></center></td>
         </g:each>
       </tr>

         <tr>
         <g:each in="${selectedStudies}" var="stud">
             <td>
                <table>
          <tr>
            <td><b>Id </b></td>
            <td><b>Name</b></td>
            <td><b>Parameters</b></td>
            <td><b>Reference</b></td>
          </tr>

          <% def protocol_list = [] %>
          <% def tmp_protocol = stud.events.eventDescription.protocol.get(0) %>
          <% def tmpBis_protocol = stud.samplingEvents.eventDescription.protocol.get(0) %>
          <% protocol_list.add(tmp_protocol) %>
          <% protocol_list.add(tmpBis_protocol) %>

          <g:each in="${stud.events.eventDescription.protocol}" var="s">
          <% if (tmp_protocol!=s) { %>
            <% protocol_list.add(s) %>
            <%}%>
          </g:each>

 <g:each in="${stud.samplingEvents.eventDescription.protocol}" var="s">
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

        </td>
           </g:each>
         </tr>
       </table>
       </div>

          <div id="events">
          <table border="2">
         <tr>
         <g:each in="${selectedStudies}" var="study">
           <td><center><b>${study.title}</b></center></td>
         </g:each>
       </tr>

         <tr>
         <g:each in="${selectedStudies}" var="stud">
             <td>

        <table>
          <tr>
            <td><b>Event Description</b></td>
            <td><b>Subject</b></td>
            <td><b>Start Time</b></td>
            <td><b>Duration</b></td>
            <td><b>Sampling Event</b></td>
            <td><b>Parameters</b></td>
          </tr>

          <g:each in="${stud.events}" var="e">
            <tr>
              <td><g:link controller="event" action="show" id="${e.id}">  ${e.eventDescription.name}</g:link></td>
          <td>${e.subject.id}</td>
          <td>${e.getPrettyDuration(stud.startDate,e.startTime)}</td>
          <td>${e.getPrettyDuration()}</td>
           <td><g:checkBox name="event" disabled="${true}" value="${false}"/></td>
          <g:each in="${e.eventDescription.protocol.parameters}" var="param">
          <td>
            ${param.name} : ${param.listEntries}
          </td>
            </g:each>
            </tr>
          </g:each>

          <g:each in="${stud.samplingEvents}" var="e">
          <tr>
          <td><g:link controller="event" action="show" id="${e.id}">${e.eventDescription.name}</g:link></td>
          <td>${e.subject.id}</td>
          <td>${e.getPrettyDuration(stud.startDate,e.startTime)}</td>
          <td>${e.getPrettyDuration()}</td>
            <td><g:checkBox name="samplingEvent" disabled="${true}" value="${true}"/></td>

            <g:each in="${e.eventDescription.protocol.parameters}" var="param">
          <td>
            ${param.name} : ${param.listEntries}
          </td>
            </g:each>
          </tr>
          </g:each>


        </table>
        </td>
           </g:each>
         </tr>
       </table>

      </div>
          
          <div id="event-description">
  <table border="2">
  <tr>
         <g:each in="${selectedStudies}" var="study">
          <td><center><b>${study.title}</b></center></td>
         </g:each>
  </tr>
   <tr>
         <g:each in="${selectedStudies}" var="stud">
           <td>
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
          <g:if test="${(stud.events.eventDescription.contains(e))}" >
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
          <g:if test="${(stud.samplingEvents.eventDescription.contains(e))}" >
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
        </td>
           </g:each>
         </tr>
       </table>

          </div>

          <div id="event-group">
          </div>

          <div id="assays">
   <table border="2">
  <tr>
         <g:each in="${selectedStudies}" var="study">
          <td><center><b>${study.title}</b></center></td>
         </g:each>
  </tr>
   <tr>
         <g:each in="${selectedStudies}" var="stud">
           <td>
           <table>
             <tr><td>
  <g:if test="${stud.assays.size()==0}">
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
          <g:each in="${stud.assays}" var="assay">
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


  </table>
        </td>
           </g:each>
         </tr>
       </table>

      </div>

    </div>

    <% } %>
    
     <% if (selectedStudies.size()==0) {%>
    Please select studies to compare.
    <% } %>

    </div>


</body>
</html>
