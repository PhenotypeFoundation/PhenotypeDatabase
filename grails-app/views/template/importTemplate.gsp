<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
	<meta name="layout" content="main"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Import templates</title>
  </head>
  <body>
    <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_TEMPLATEADMIN">
    <h1>Select XML file to import </h1>
    <g:if test="${flash.message}">
	  <div>${flash.message}</div>
	</g:if>
        <g:form controller="template" method="post" action="handleImportedFile" enctype="multipart/form-data">
        <input type="file" name="file"/><br />
        <input type="submit" value="Import" />
        </g:form>
    </sec:ifAnyGranted>
    <sec:ifNotGranted roles="ROLE_ADMIN, ROLE_TEMPLATEADMIN">
        <h1>Template import </h1>
        Only (template)Admins are able to import templates, please request a template/templatefield at the specific type.
    </sec:ifNotGranted>

  </body>
</html>
