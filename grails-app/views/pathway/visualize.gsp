<%@ page import="dbnp.query.Operator" %>
<%@ page import="dbnp.studycapturing.*" %>
<%@ page import="org.dbnp.gdt.*" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Pathway</title>
</head>
<body>

<h1>Visualize Study</h1>

<g:if test="${flash.error}">
	<div class="errormessage">
		${flash.error.toString().encodeAsHTML()}
	</div>
</g:if>
<g:if test="${flash.message}">
	<div class="message">
		${flash.message.toString().encodeAsHTML()}
	</div>
</g:if>

<div id="pathwayForm">
	<g:form action="index" method="get">
        <p>
            ${test}
        </p>
    </g:form>
	<br clear="all" />
</div>
</body>
</html>
