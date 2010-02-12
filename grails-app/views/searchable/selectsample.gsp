<html>

  <head>
      <title>Generic Study Capture Framework - Query studies</title>
      <meta name="layout" content="main" />
      <g:setProvider library="jquery"/>
      <script src = ${createLinkTo(dir: 'js/jquery_combobox', file: 'ui.core.js')}></script>
      <script src = ${createLinkTo(dir: 'js/jquery_combobox', file: 'ui.combobox')}></script
    </head>

<body>

  <h1>Advanced query - select samples</h1>
  <br>


  <g:form action="selectsample" url >


   <br> <%= params %>
   <br> <%= selectedStudyIds.each{ println it } %>
   <br> <%= subgroups%>
   <input type="hidden" name="selectedStudyIds" value="${selectedStudyIds}"   </div>


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
           <g:if test="${subgroups.size>0}"> <td><b> Subgroups </b></td> </g:if>
         </tr>

  <g:each in="${selectedStudies}" status="j" var="studyIns">
  <tr>
  <td> <input type="checkbox" name="${studyIns.title}" id="${studyIns.title}" class="checkbox1${studyIns.id}"> </td>
          <td> ${studyIns.title} </td>
  </tr>



  <g:each in ="${studyIns.events}" var="events">
        <tr>
          <td></td><td></td>
          <td> <input type="checkbox" name="${studyIns.id}.${events.subject.name}" id="${events.subject.name}" class="checkbox2${studyIns.id}" >
               ${events.subject.name} </td>
          <td> ${studyIns.samplingEvents} </td>
          <td> ${studyIns.samplingEvents.samples.name}</td>
          <td> ${studyIns.samplingEvents.samples.material}</td>
          <td> ${events.getDurationString()}</td>

          <g:if test="${subgroups.size>0}">
	  <td> <select id="demo">
              <g:each in ="${subgroups}" var="p">
	          <option value = "${p}"> "${p}" </option>
              </g:each>
	  </select> </td>
	  </g:if>
        </tr>
     </g:each>


    </g:each>
       
     </table>



    <g:each in="${selectedStudies}" status="j" var="studyIns">
        <% def cb1 = '\'.checkbox1' + studyIns.id + '\''  %>
        <% def cb2 = '\'.checkbox2' + studyIns.id + '\''  %>
        <script>
              $(${cb1}).click(function () {
                  if($(this).attr("checked")==true)
                     $(${cb2}).attr("checked", "checked");
                  });
              $(${cb2}).click(function () {
                  if($(this).attr("checked")==false)
                         $(${cb1}).attr("checked", false);
                  });
        </script>
    </g:each>



  <%}%>


     Infer subgroups:
    <INPUT TYPE=submit name=submit Value="Subject Groups">
    <INPUT TYPE=submit name=submit Value="Event Groups">
    <INPUT TYPE=submit name=submit Value="Starting Time Groups">

    <% if (selectedStudies.size()==0) { %>
    <br> Please select studies to query samples.
    <% } %>

    <br>
    <INPUT TYPE=submit name=submit Value="<< Back to study selection">
    <INPUT TYPE=submit name=submit Value=">> Execute and continue with biomarker selection">

    </g:form>



  </body>
</html>