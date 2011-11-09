<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
	<title>Simple GSP page</title>
	<meta name="layout" content="galaxy"/>
	<script type="text/javascript">
		$(document).ready(function(){
			$("form#galaxyForm").submit()
		})
	</script>
</head>

<body>

<form id='galaxyForm' action="${GALAXY_URL}" method="post">
	
	<input type="hidden" name="tool_id" value="${tool_id}">
	
	<input type="hidden" name="URL" value="${fetchUrl}">

</form>

</body>
</html>