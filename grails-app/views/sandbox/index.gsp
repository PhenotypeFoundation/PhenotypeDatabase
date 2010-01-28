<%--
  Created by IntelliJ IDEA.
  User: kees
  Date: 28-jan-2010
  Time: 14:36:03

  The sandbox is meant for internal communication over code examples etc.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Sandbox</title></head>
<body>
<h1>Sandbox</h1>
<h2>Subject Template fields demo</h2>
<table>
	<tr>
		<td>Name</td>
	<g:each in="${fields}" var="field">
		<td>${field.name} (${field.type})</td>
	</g:each>
	</tr>
	<g:each in="${subjects}" var="subject">
		<tr>
			<td>${subject.name}</td>
		<g:each in="${fields}" var="field">
			<td>${subject.getFieldValue(field.name)}</td>
		</g:each>
		</tr>
	</g:each>
</tr>
</table>
</body>
</html>