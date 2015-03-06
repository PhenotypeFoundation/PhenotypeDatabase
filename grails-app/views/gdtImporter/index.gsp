<%
/**
 * Wizard index page
 *
 * @author Jeroen Wesbeek
 * @since  20110310
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

	<link rel="stylesheet" href="${resource(dir: 'css', file: 'gdtimporter.css', plugin: 'gdtimporter')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'studywizard.css', plugin: 'gdtimporter')}"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'table-editor.css', plugin: 'gdt')}"/>

    <script type="text/javascript" src="${resource(dir: 'js', file: 'fileupload.js', plugin:'gdt')}"></script>

    <script type="text/javascript" src="${resource(dir: 'js', file: 'studywizard.js', plugin:'gdtimporter')}"></script>

    <link rel="stylesheet" href="${resource(dir: 'css', file: 'demo_table.css', plugin: 'gdtimporter')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'demo_table_jui.css', plugin: 'gdtimporter')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'demo_page.css', plugin: 'gdtimporter')}"/>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.dataTables.js', plugin: 'gdtimporter')}"></script>
    <g:if env="production">
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.qtip-1.0.0-rc3.min.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.min.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'ajaxupload.3.6.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.min.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'table-editor.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'tooltips.js', plugin: 'gdt')}"></script>
	</g:if><g:else>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.qtip-1.0.0-rc3.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'ajaxupload.3.6.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'table-editor.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'tooltips.js', plugin: 'gdt')}"></script>
	</g:else>

</head>
<body>
	<g:render template="common/ajaxflow"/>
</body>
</html>
