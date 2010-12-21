<%
/**
 * Wizard index page
 *
 * @author Jeroen Wesbeek
 * @since  20101220
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
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'studywizard.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'templates.css')}"/>

	<script type="text/javascript" src="${resource(dir: 'js', file: 'studywizard.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'tooltips.js')}"></script>

	<link rel="stylesheet" href="${resource(dir: 'css', file: 'table-editor.css')}"/>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'table-editor.js')}"></script>



	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.qtip-1.0.0-rc3.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'publication-chooser.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'publication-chooser.pubmed.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'timepicker-0.2.1.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'ajaxupload.3.6.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.ui.autocomplete.html.js')}"></script>

</head>
<body>
	<g:render template="common/ajaxflow"/>
</body>
</html>
