<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
	<meta name="layout" content="main"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'templateEditor.css')}" />
    <title>Imported templates</title>
  </head>
  <body>
    <h1>Templates imported</h1>
	<g:if test="${messages.size() == 0}">
	  No templates imported.
	</g:if>
	<g:else>
	  <ul>
		<g:each in="${messages}" var="message" status="i">
		  <li>${message}</li>
		</g:each>
	  </ul>
	</g:else>

	<p><a href="<g:createLink action="importTemplate" />">Import other templates</a></p>
  </body>
</html>
