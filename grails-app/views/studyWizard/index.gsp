<%
/**
 * Wizard index page
 *
 * @author Jeroen Wesbeek
 * @since  20101220
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<html>
<head>
	<meta name="layout" content="main"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'ajaxflow.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'studywizard.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'templates.css')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'tipTip.css')}"/>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.tipTip.minified.js')}"></script>

	<g:if env="development">
		<script type="text/javascript" src="${resource(dir: 'js', file: 'studywizard.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'tooltips.js', plugin: 'gdt')}"></script>
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'table-editor.css', plugin: 'gdt')}"/>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'table-editor.js', plugin: 'gdt')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'fuzzyStringMatch.js', plugin: 'gdt')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.qtip-1.0.0-rc3.js', plugin: 'gdt')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.js', plugin: 'gdt')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'publication-chooser.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'publication-chooser.pubmed.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.js', plugin: 'gdt')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'timepicker-0.2.1.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'ajaxupload.3.6.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.ui.autocomplete.html.js', plugin: 'gdt')}"></script>
	</g:if>
	<g:else>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'studywizard.min.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'tooltips.min.js', plugin: 'gdt')}"></script>
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'table-editor.min.css', plugin: 'gdt')}"/>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'table-editor.min.js', plugin: 'gdt')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'fuzzyStringMatch.min.js', plugin: 'gdt')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.qtip-1.0.0-rc3.min.js', plugin: 'gdt')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.min.js', plugin: 'gdt')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'publication-chooser.min.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'publication-chooser.pubmed.min.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.min.js', plugin: 'gdt')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'timepicker-0.2.1.min.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'ajaxupload.3.6.min.js')}"></script>
		<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.ui.autocomplete.html.min.js', plugin: 'gdt')}"></script>
	</g:else>
</head>
<body>
	<g:render template="common/ajaxflow"/>
</body>
</html>
