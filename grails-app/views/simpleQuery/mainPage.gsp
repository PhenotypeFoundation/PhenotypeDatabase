<%--
  Created by IntelliJ IDEA.
  User: luddenv
  Date: 26-mei-2010
  Time: 13:17:50
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
	<meta name="layout" content="main"/><g:if env="production">
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'simpleQuery.min.css')}"/>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'simpleQuery.min.js')}"></script>
</g:if><g:else>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'simpleQuery.css')}"/>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'simpleQuery.js')}"></script>
</g:else>
</head>
<body>
<g:render template="common/query"/>
</body>
</html>