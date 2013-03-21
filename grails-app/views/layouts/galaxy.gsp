<%@page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en-EN" xml:lang="en-EN">
<head>
	<title><g:layoutTitle default="${grailsApplication.config.application.title}"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<g:javascript library="jquery" plugin="jquery"/>
    <script type="text/javascript">var baseUrl = '${resource(dir: '')}';</script>
	<script src="${createLinkTo(dir: 'js', file: 'jquery-ui-1.8.23.custom.min.js')}" type="text/javascript"></script>
	<link rel="stylesheet" href="${createLinkTo(dir: 'css/cupertino', file: 'jquery-ui-1.8.23.custom.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'default.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'login_panel.css')}"/>

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
<g:render template="/common/login_panel"/>
<div class="container">
	<g:layoutBody/>
	<div id="footer">
		Copyright © 2008 - <g:formatDate format="yyyy" date="${new Date()}"/> NuGO, NMC and NBIC. All rights reserved. For more information go to <a href="http://dbnp.org">http://dbnp.org</a>.
	</div>
</div>
</body>
</html>