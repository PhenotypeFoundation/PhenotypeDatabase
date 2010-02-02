
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
  <% def att_list = ['startDate','code','title'] %>
  <% def selectedStudies = [] %>
  <% def tmpList = [] %>

  <div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
  </div>

  <div class="body">
    <h1>Compare Studies</h1>
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
           <td></td>
           <g:each in="${dbnp.studycapturing.Study.list()}" var="stud">
             <td>
         ${stud}
             </td>
           </g:each>
         </tr>
       </table>
       
      </div>

 <a href="#"> Groups </a> <div>
   <g:each in="${selectedStudies}" var="stud">
   ${stud}
   </g:each>
        </div>

       <a href="#"> Protocols </a><div>
         <g:each in="${selectedStudies}" var="stud">
   ${stud}
   </g:each>
       </div>

      <a href="#"> Events </a><div>
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
