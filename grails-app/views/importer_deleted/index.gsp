<%
/**
 * Wizard index page
 *
 * @author Jeroen Wesbeek
 * @since  20101206
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
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'importer.css')}"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'wizard.css')}"/>

    <g:if env="production">
      <script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.min.js')}"></script>
      <script type="text/javascript" src="${resource(dir: 'js', file: 'ajaxupload.3.6.js')}"></script>
      <script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.min.js')}"></script>
    </g:if><g:else>
      <script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.js')}"></script>
      <script type="text/javascript" src="${resource(dir: 'js', file: 'ajaxupload.3.6.js')}"></script>
      <script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.js')}"></script>
      <script type="text/javascript" src="${resource(dir: 'js', file: 'table-editor.js')}"></script>
      <script type="text/javascript" src="${resource(dir: 'js', file: 'wizard.js')}"></script>
    </g:else>

</head>
<body>
	<g:render template="common/ajaxflow"/>
</body>
</html>
