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

    <h1>Select the columns that you want to be included in the resulting Excel file</h1>

    <g:form name="fieldSelectForm" action="compileExportData">

      <g:set var="catNum" value="${0}"/>
      <g:each in="${fieldMap}" var="entry">

          <assayExporter:categorySelector category="${entry.key}" ref="cat_${catNum}"/>

          <assayExporter:fieldSelectors ref="cat_${catNum}" fields="${entry.value}"/>

          <g:set var="catNum" value="${catNum + 1}"/>

      </g:each>

      <assayExporter:categorySelector category="Measurements" ref="cat_${catNum}"/>
      <g:select name="measurementToken" id="measurementToken" from="${measurementTokens}" class="field" multiple="true"/>
      <br /><br />
      <g:submitButton name="submit" value="Submit"/>

    </g:form>

  </div>
  </body>
</html>