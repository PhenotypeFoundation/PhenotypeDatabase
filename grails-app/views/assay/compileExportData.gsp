<%--
  Created by IntelliJ IDEA.
  User: siemensikkema
  Date: Feb 22, 2010
  Time: 4:03 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
    <meta name="layout" content="main" />
    <title>Export preview</title>
  </head>
  <body>

  <h1>Below you see a preview of the resulting Excel file, click OK to generate it</h1>

  <table>
    <g:each in="${assayDataPreview}" var="row">

      <tr><g:each in="${row}" var="cell">

        <td>${cell}</td>

      </g:each>

      <td>...</td>

      </tr>

    </g:each>

    <tr>
    <g:set var="columns" value="${assayDataPreview[0].size()+1}"/>

    <g:while test="${columns>0}">
      <g:set var="columns" value="${columns-1}"/>
      <td>...</td>
    </g:while>
    </tr>

  </table>

  <a href="doExport">OK</a>
  <a href="selectAssay">Cancel</a>
  
  </body>
</html>