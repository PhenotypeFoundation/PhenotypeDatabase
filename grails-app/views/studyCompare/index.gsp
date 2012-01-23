<%
/**
 * Wizard index page
 *
 * @author Jeroen Wesbeek
 * @since  20120123
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
	<g:if env="development">
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'ajaxflow.css')}"/>
	</g:if><g:else>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'ajaxflow.min.css')}"/>
	</g:else>
	<style type="text/css">
	.waitForLoad {
		background: url(../images/ajaxflow/ajax-loader.gif) no-repeat center top;
		width: 220px;
		height: 30px;
	}
	</style>
</head>
<body>
	<g:render template="common/ajaxflow"/>
</body>
</html>
