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
<h2>Events</h2>
<% println studyInstance.events.eventDescription.protocol.name %>
<h2>Subject Fields</h2>
<table>
	<tr>
		<td>Source</td>
		<td>Name</td>
		<td>Type</td>
	</tr>
	<g:each in="${subject.giveDomainFields()}" var="field">
	    <tr>
		    <td>Domain field</td>
			<td>${field.key}</td>
			 <td>${field.value}</td>
		</tr>
	</g:each>
	<g:each in="${subject.giveFields()}" var="field">
	    <tr>
			<td>Template field</td>
		    <td>${field.name}</td>
			 <td>${field.type}</td>
		</tr>
	</g:each>

</table>
</body>
</html>