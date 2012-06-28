<%--
  Created by IntelliJ IDEA.
  User: kees
  Date: 27-06-12
  Time: 17:25
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Test Assay service</title>
</head>
<body>
<h2>Show assay</h2>
<table>
  <tr>
    <td>Name</td>
    <g:each in="${samples}" var="sample">
    <td>${sample.name}</td>
  </g:each>
  </tr>
    <g:each in="${measurements}" var="measurement">
      <tr>
      <td>${measurement.key}</td>
      <g:each in="${measurement.value}" var="value">
        <td>${value}</td>
      </g:each>
      </tr>
    </g:each>
  </tr>
</table>
</body>
</html>