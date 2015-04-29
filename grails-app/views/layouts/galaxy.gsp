<%@page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en-EN" xml:lang="en-EN">
<head>
	<title><g:layoutTitle default="${grailsApplication.config.application.title}"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <r:require modules="gscfmain"/>
    <r:layoutResources />
    <script type="text/javascript">var baseUrl = '${resource(dir: '')}';</script>

	<style type="text/css">
		body {
			background: none;
		}
		.container {
			margin: 20px;
		}
	</style>


	<g:layoutHead/>
</head>
<body>
<g:render template="/common/header"/>
<div class="container">
	<g:layoutBody/>
	<div id="footer">
		Copyright © 2008 - <g:formatDate format="yyyy" date="${new Date()}"/> NuGO, NMC and NBIC. All rights reserved. For more information go to <a href="http://dbnp.org">http://dbnp.org</a>.
	</div>
</div>
</body>
</html>