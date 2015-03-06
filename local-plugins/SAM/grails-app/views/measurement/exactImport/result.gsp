<%--
  Created by IntelliJ IDEA.
  User: Ferry
  Date: 20/6/13
  Time: 10:17
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="sammain"/>
    <title>Exact Measurement Importer</title>
    <r:require module="importer" />

</head>
<body>
<div class="data">
	<script type="text/javascript">
	    // disable redirect warning
	    $(function() {
	    	warnOnRedirect = false;
	    });
	</script>
    <content tag="contextmenu">
        <li><g:link action="list" class="list"  params="${[module: module]}">Back to list</g:link></li>
    </content>
    <h1>${module} Import Results for  ${assayInstance.name}</h1>
    <g:if test="${errorList.isEmpty()}">
        <p>No errors found</p>
    </g:if>
    <g:else>
        <p>The following errors occurred:</p>
        <g:each var="error" in="${errorList}">
            <p>${error}</p>
        </g:each>
    </g:else>
    <p><h2>You can find the assay <g:link controller="SAMAssay" action="show" params="${[module: module]}" id="${assayInstance.id}"><b>here</b></g:link></h2></p>
</div>
</body>
</html>