
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
  <% def att_list = ['id','template','startDate','code','title'] %>
  <% def selectedStudies = [] %>
  <% def tmpList = [] %>

  <div class="nav">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
    <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
  </div>

  <div class="body">
    <h1><g:message code="default.list.label" args="[entityName]" /></h1>
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


    <div id="tabs">
      <ul>
      <g:each in="${selectedStudies}" status="i" var="studyInstance">
        <li><a href="#${studyInstance}"> ${studyInstance} </a></li>
      </g:each>
      </ul>

      <g:each in="${selectedStudies}" status="i" var="studyIns">

        
      <div id="${studyIns}">
        <g:each in="${att_list}" status="s" var="attribute">
              ${message(code: 'study.id.'+attribute , default: attribute)} :
              ${fieldValue(bean: studyIns, field: attribute)}
                            <br>
        </g:each>
      </div>

        </g:each>
    </div>

    </div>


</body>
</html>
