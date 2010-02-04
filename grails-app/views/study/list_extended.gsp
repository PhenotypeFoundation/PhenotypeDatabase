
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
            $("#accordions").accordion();
    });
  </script>

</head>
<body>

  <% studyList = dbnp.studycapturing.Study.list() %>
  <% def att_list = ['template','startDate','samplingEvents','lastUpdated','readers','code','editors','ecCode','researchQuestion','title','description','owner','dateCreated'] %>
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

    <div id="accordions">

    <a href="#"> Study Information </a>
    <div>
      <br>
      <table>
      <tr>
        <td></td>
        <g:each in="${selectedStudies}" status="j" var="studyIns">
        <td><b>${studyIns.title}</b></td>
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
        <td><b>${att}</b></td>
        <g:each in="${selectedStudies}" status="k" var="studyIns">
<td>${fieldValue(bean: studyIns, field: att)}</td>
        </g:each>
      </tr>
      </g:each>

      </table>
    </div>

     <a href="#"> Subjects </a><div>
       
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
            <td><b>Id </b></td>
            <td><b>Species</b></td>
            <td><b>Name</b></td>
          <g:each in="${stud.template.subjectFields}" var="g">
            <td><b>
              <g:link controller="templateSubjectField" action="show" id="${g.id}">
              ${g}</b></td>
            </g:link>
          </g:each>
          </tr>

          <g:each in="${stud.subjects}" var="s">
            <tr>
              <td><g:link controller="subject" action="show" id="${s.id}">${s.id}</g:link></td>
              <td>${s.species}</td>
              <td>${s.name}</td>

                <g:each in="${stud.template.subjectFields}" var="g">
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

             </td>
           </g:each>
         </tr>
       </table>
       
      </div>

 <a href="#"> Groups </a> <div>

        </div>

       <a href="#"> Protocols </a><div>
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
            <td><b>Id </b></td>
            <td><b>Name</b></td>
            <td><b>Parameters</b></td>
            <td><b>Reference</b></td>
          </tr>

          <% def protocol_list = [] %>
          <% def tmp_protocol = stud.events.eventDescription.protocol.get(0) %>
          <% protocol_list.add(tmp_protocol) %>
          <g:each in="${stud.events.eventDescription.protocol}" var="s">

          <% if (tmp_protocol!=s) { %>
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

      <a href="#"> Events </a><div>
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
            <td><b>End Time</b></td>
            <td><b>Duration</b></td>
          </tr>
          <g:each in="${stud.events}" var="e">
            <tr>
              <td><g:link controller="event" action="show" id="${e.id}">  ${e.eventDescription.name}</g:link></td>
          <td>${e.subject.id}</td>
          <td>${e.startTime}</td>
          <td>${e.endTime}</td>
          <td>${e.getDurationString()}</td>
          </tr>
          </g:each>
          </table>
        </td>
           </g:each>
         </tr>
       </table>

      </div>

      <a href="#"> Assays </a><div>
      </div>

    </div>

    

    <% } %>
    
     <% if (selectedStudies.size()==0) {%>
    Please select studies to compare.
    <% } %>

    </div>


</body>
</html>
