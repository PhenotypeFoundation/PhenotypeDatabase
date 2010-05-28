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
   <br> <b> Subgroups </b>:
   <br> <%= subgroups%>
   <input type="hidden" name="selectedStudyIds" value="${selectedStudyIds}"   </div>

   <% showSubgroups = (subgroups.size()>0) %>
    <g:if test="${selectedStudies.size()>0}">

       <table >
         <tr>
           <td></td>
           <td><b> Study </b></td>
           <td><b> Subject </b></td>
           <td><b> Sampling Events </b></td>
           <td><b> Sample Name </b></td>
           <td><b> Sample Material </b></td>
           <td><b> Start Time </b></td>
           <g:if test="${showSubgroups}"> <td><b> Subgroups </b></td> </g:if>
           <g:else> <td><b> Selection </b></td> </g:else>
         </tr>


         <g:each in="${selectedStudies}" status="j" var="studyIns">
         <tr>
         <td> <input type="checkbox" name="${studyIns.title}" id="${studyIns.title}" class="checkbox1_${studyIns.id}"> </td>
                 <td> ${studyIns.title} </td>
         </tr>



         <g:if test="${studyIns.samplingEvents.size ()>0}" >
         <g:each in ="${studyIns.samplingEvents}" var="event">
	   <% def firstRow = true %>
            <g:each in ="${event.samples}" var="sample">
               <tr>
                 <td></td><td></td>

		 <td>
		 <g:if test="${firstRow}">
		     <% firstRow=false %>
                     <input type="checkbox" name="${studyIns.id}.${event.subject.name}" id="${event.subject.name}" class="checkbox2_${studyIns.id}_${event.subject.id}" >
                          ${event.subject.name}
                 </g:if>
                 </td>

                 <td> ${event.eventDescription.name} </td>
                 <td> ${sample.name}</td>
                 <td> ${sample.material}</td>

                 <td> ${event.getPrettyDuration(event.startTime)} </td>

                 <td>
                 <g:if test="${showSubgroups}">
	         <select id="demo">
                     <g:each in ="${subgroups}" var="p">
	                 <option value = "${p}"> "${p}" </option>
                     </g:each>
	         </select>
	         </g:if>
		 <g:else>
                     <input type="checkbox" name="${studyIns.id}.${event.subject.id}.${sample.id}" id="${event.subject.name}" class="checkbox3_${studyIns.id}_${event.subject.id}_${sample.id}" >
		 </g:else>
                 </td>

               </tr>
            </g:each>
           </g:each>
         </g:if>


         <g:else>
	       <tr>
	           <td></td> <td>Study does not cotain any samples. </td>
	           <td></td> <td></td> <td></td> <td></td> <td></td>
                   <g:if test="${showSubgroups}"> <td></td> </g:if>
	       </tr>
           </g:else>
           </g:each>

       </table>



       <g:each in="${selectedStudies}" status="j" var="studyIns">
            <g:each in ="${studyIns.samplingEvents}" var="event">
               <g:each in ="${event.samples}" var="sample">
               <% def cb1 = '\'.checkbox1_' + studyIns.id + '\''  %>
               <% def cb2 = '\'.checkbox2_' + studyIns.id + '_' + event.subject.id + '\''  %>
               <% def cb3 = '\'.checkbox3_' + studyIns.id + '_' + event.subject.id + '_' + sample.id + '\''  %>
                    <script>
                         $(${cb1}).click(function () {
                             if($(this).attr("checked")==true) {
                                $(${cb2}).attr("checked", "checked");
                                $(${cb3}).attr("checked", "checked");
			     }
			     else {
                                $(${cb2}).attr("checked", false);
                                $(${cb3}).attr("checked", false);
			     } });
                         $(${cb2}).click(function () {
                             if($(this).attr("checked")==true)
                                    $(${cb3}).attr("checked", true);
                             else {
                                    $(${cb1}).attr("checked", false);
                                    $(${cb3}).attr("checked", false);
			     } });
                         $(${cb3}).click(function () {
                             if($(this).attr("checked")==false) {
                                    $(${cb1}).attr("checked", false);
                                    $(${cb2}).attr("checked", false);
                             } });
                    </script>
               </g:each>
            </g:each>
       </g:each>


    </g:if>


    <br>

    <script>
       function checkAll(value){
           var list = document.getElementsByTagName("input");
	   for(i=0; i<list.length; i++)
           {
               if(list[i].type=="checkbox")
               {
                   list[i].checked=value;
               }
           }
       }
    </script>


    <input type="button" name="CheckAll" value="Check All" onClick="checkAll(true)">
    <input type="button" name="UncheckAll" value="Uncheck All" onClick="checkAll(false)">

     Infer subgroups:
    <INPUT TYPE=submit name=submit Value="Subject Groups">
    <INPUT TYPE=submit name=submit Value="Event Groups">
    <INPUT TYPE=submit name=submit Value="Starting Time Groups">
    <br>




    <% if (selectedStudies.size()==0) { %>
    <br> Please select studies to query samples.
    <% } %>

    <br>
    <INPUT TYPE=submit name=submit Value="<< Back to study selection">
    <INPUT TYPE=submit name=submit Value=">> Execute and continue with biomarker selection">

    </g:form>



  </body>
</html>