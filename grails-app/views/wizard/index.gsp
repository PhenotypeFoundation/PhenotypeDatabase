<%
	/**
	 * Wizard main template
	 *
	 * @author Jeroen Wesbeek
	 * @since 20100113
	 * @package wizard
	 * @see dbnp.studycapturing.WizardTagLib::previousNext
	 * @see dbnp.studycapturing.WizardController
	 *
	 * Revision information:
	 * $Rev$
	 * $Author$
	 * $Date$
	 */
%>
<html>
<head>
	<meta name="layout" content="main"/><g:if env="development">
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'wizard.min.css')}"/>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.qtip-1.0.0-rc3.min.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'swfobject.min.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'table-editor.min.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.min.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'timepicker-0.2.1.min.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'wizard.min.js')}"></script>
</g:if><g:else>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'wizard.css')}"/>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'development.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.qtip-1.0.0-rc3.min.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'swfobject.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'table-editor.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'timepicker-0.2.1.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'wizard.js')}"></script>
</g:else>
</head>
<body>
<g:render template="common/wizard"/>
</body>
</html>