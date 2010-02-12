<html>

  <head>
      <title>Generic Study Capture Framework - Query studies</title>
      <meta name="layout" content="main" />
    </head>

<body>

  <h1>Advanced query - select samples</h1>
  <br>

   <% def tmpList = [] %>
    <% studyList = dbnp.studycapturing.Study.list() %>
<% def selectedStudies = [] %>

<% params.each{key,values-> %>
    <% if (values=="on"){ %>
      <% tmpList.add(key) %>
    <%  } }%>

  <% for (i in studyList) {%>
    <% if (tmpList.contains(i.getTitle())) { %>
      <% selectedStudies.add(i) %>
     <% }} %>
   
  <% if (selectedStudies.size()>0) {%>
       <table >

         <tr>
           <td></td>
           <td><b> Study </b></td>
           <td><b> Subject </b></td>
           <td><b> Sampling Events </b></td>
           <td><b> Sample Name </b></td>
           <td><b> Sample Material </b></td>
           <td><b> Duration </b></td>
         </tr>
     <g:each in="${selectedStudies}" status="j" var="studyIns">
  <tr>
  <td> <input type="checkbox" name="${studyIns.title}" id="${studyIns.title}"> </td>
          <td> ${studyIns.title} </td>
          
        </tr>
  <g:each in ="${studyIns.events}" var="events">
        <tr>
          <td></td><td></td>
          <td> <input type="checkbox" name="${events.subject.name}" id="${events.subject.name}">
  ${events.subject.name} </td>
          <td> ${studyIns.samplingEvents} </td>
          <td> ${studyIns.samplingEvents.samples.name}</td>
          <td> ${studyIns.samplingEvents.samples.material}</td>
          <td> ${events.getDurationString()}</td>
        </tr>
     </g:each>
     </g:each>
       
       </table>
<%}%>

       <% if (selectedStudies.size()==0) {%>
    Please select studies to query samples.
    <% } %>

    <br>
    <INPUT TYPE=submit name=submit Value="<< Back to study selection">
    <INPUT TYPE=submit name=submit Value=">> Execute and continue with biomarker selection">

  </body>
</html>