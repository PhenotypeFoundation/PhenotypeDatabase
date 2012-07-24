<%@page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en-EN" xml:lang="en-EN">
<head>
	<title><g:layoutTitle default="${grailsApplication.config.application.title}"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta property="og:title" content="${grailsApplication.config.application.title}"/>
	<meta property="og:description" content="A generic tool for planning scientific studies, and capturing study meta-data, integrating with analysis platform(s) / LIMS systems and searching relevant studies."/>
	<meta property="og:type" content="non_profit"/>
	<meta property="og:image" content="${resource(dir: 'images', file: 'facebookLike.png', absolute: true)}"/>
	<meta property="fb:admins" content="721482421"/>
	<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon"/>
	<g:javascript library="jquery" plugin="jquery"/>
	<script type="text/javascript">var baseUrl = '${resource(dir: '')}';</script>
	<script src="${createLinkTo(dir: 'js', file: 'jquery-ui-1.8.13.custom.min.js')}" type="text/javascript"></script>
	<link rel="stylesheet" href="${createLinkTo(dir: 'css/cupertino', file: 'jquery-ui-1.8.13.custom.css')}"/>
	<g:if env="production"><script src="http://connect.facebook.net/en_US/all.js#xfbml=1"></script></g:if>
	<g:if env="development">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'default.css')}"/>
		<link rel="stylesheet" href="${resource(dir: 'css', file: session.style + '.css')}"/>
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'login_panel.css')}"/>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'disableKeys.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'login_panel.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'topnav.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'paginate.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'removeWebflowExecutionFromMenu.min.js')}"></script>
	</g:if>
	<g:else>
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'default.min.css')}"/>
		<link rel="stylesheet" href="${resource(dir: 'css', file: session.style + '.min.css')}"/>
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'login_panel.min.css')}"/>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'disableKeys.min.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'login_panel.min.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'topnav.min.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'paginate.min.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'removeWebflowExecutionFromMenu.js')}"></script>
	</g:else>
	<g:layoutHead/>
</head>
<body>
<g:render template="/common/login_panel"/>
<div class="container">
	<div id="header">
		<g:render template="/common/topnav"/>
	</div>
	<div id="content"><g:layoutBody/></div>
	<g:if env="production">
	<g:if test="${facebookLikeUrl}">
	<div id="facebookConnect">
		<fb:like href="${resource(absolute: true)}${facebookLikeUrl}" show_faces="true" width="450" action="recommend" font="arial"></fb:like>
	</div>
	</g:if>
	</g:if>
	<div id="footer">
		Copyright © 2008 - <g:formatDate format="yyyy" date="${new Date()}"/> NuGO, NMC and NBIC. All rights reserved. For more information go to <a href="http://dbnp.org">http://dbnp.org</a>.
	</div>
</div>
<trackr:track reference="${session?.gscfUser ? session.gscfUser : '-'}"/>
</body>
</html>