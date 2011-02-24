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

  <style type="text/css">

    .selectCategoryDiv{
      float: left;
      width: auto;
    }

    .selectFieldDiv{
    }

    .clear {clear: both;}

  </style>
</head>
  <body>
  <div>
    <g:form name="fieldSelectForm" action="compileExportData">
      <div class="selectCategoryDiv">

        <g:set var="catNum" value="${0}"/>

        <g:each in="${fieldMap + ['Measurement tokens':'']}">

          <assay:categorySelector category="${it.key}" ref="cat_${catNum}"/><br/>
          <g:set var="catNum" value="${catNum + 1}"/>

        </g:each>

      </div>

      <div class="selectFieldDiv">

        <g:set var="catNum" value="${0}"/>

        <g:each in="${fieldMap}">

          <assay:fieldSelector ref="cat_${catNum}" fieldNames="${it.value}"/><br/>
          <g:set var="catNum" value="${catNum + 1}"/>

        </g:each>

        <g:select name="measurementToken" id="measurementToken" from="${measurementTokens}" noSelection="${[null:'All tokens']}"/>

      </div>

      <div class="clear"></div>

      <g:submitButton name="submit" value="Submit"/>

    </g:form>
  </div>
  </body>
</html>