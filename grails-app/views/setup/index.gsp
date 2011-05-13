<%
/**
 * Wizard index page
 *
 * @author Jeroen Wesbeek
 * @since  20110318
 *
 * Revision information:
 * $Rev:  66849 $
 * $Author:  duh $
 * $Date:  2010-12-08 15:12:54 +0100 (Wed, 08 Dec 2010) $
 */
%>
<html>
<head>
	<meta name="layout" content="main"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'ajaxflow.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'setupwizard.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'templates.css')}"/>
	<link type="text/css" rel="stylesheet" href="http://alexgorbatchev.com/pub/sh/current/styles/shCore.css" />
	<link type="text/css" rel="stylesheet" href="http://alexgorbatchev.com/pub/sh/current/styles/shThemeDefault.css" />
	<script src="http://alexgorbatchev.com/pub/sh/current/scripts/shCore.js"></script>
	<script src="http://alexgorbatchev.com/pub/sh/current/scripts/shAutoloader.js"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'tooltips.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.qtip-1.0.0-rc3.js', plugin: 'gdt')}"></script>
</head>
<body>
	<g:render template="common/ajaxflow"/>
</body>
</html>
