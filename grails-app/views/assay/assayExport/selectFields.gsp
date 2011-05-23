<%--
  Created by IntelliJ IDEA.
  User: siemensikkema
  Date: 2/3/11
  Time: 1:29 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main" />
  <title>Select assay fields</title>
  <script type="text/javascript" src="${resource(dir: 'js', file: 'tooltips.js', plugin: 'gdt')}"></script>
  <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.qtip-1.0.0-rc3.min.js', plugin: 'gdt')}"></script>
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'templates.css')}"/>

  <style type="text/css">
    .category{
      margin-left: 5px;
    }

    .field{
      margin-left: 20px;
    }

    .element .helpIcon{
      margin-top: 0;
    }
  </style>

  <script type="text/javascript">
    $(document).ready(function() {
      attachHelpTooltips();
    })
  </script>
</head>
  <body>
  <div>

    <h1>Select the columns that you want to be included in the resulting file</h1>

    <g:if test="${errorMessage}">
    <div class="errormessage">${errorMessage}</div>
    </g:if>

    In this step you can make a selection from the available fields stored in the database related to the samples, including measurement data from a module (if available).

    <g:form name="fieldSelectForm" action="assayExport">

      <g:set var="catNum" value="${0}"/>
      <g:each in="${fieldMap}" var="entry">

          <assayExporter:categorySelector category="${entry.key}" name="cat_${catNum}" value="${true}" />

          <assayExporter:fieldSelectors ref="cat_${catNum}" fields="${entry.value}"/>

          <g:set var="catNum" value="${catNum + 1}"/>

      </g:each>

      <assayExporter:categorySelector category="Measurements" name="cat_${catNum}" value="${measurementTokens as Boolean}" />
      <g:select name="measurementToken" id="measurementToken" from="${measurementTokens}" value="${measurementTokens}" class="field" multiple="true" />
      <br /><br />

      <h1>Select type of resulting file</h1>
      <g:radioGroup name="exportFileType" labels="['Tab delimited (.txt)', 'Comma Separated: USA/UK (.csv)', 'Semicolon Separated: European (.csv)']" values="[1,2,3]" value="1" >
        <p>${it.radio} ${it.label}</p>
      </g:radioGroup>
      <g:submitButton name="submit" value="Submit"/>

    </g:form>

  </div>
  </body>
</html>