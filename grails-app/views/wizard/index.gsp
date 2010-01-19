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
	<meta name="layout" content="main"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'wizard.css')}"/>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.qtip-1.0.0-rc3.min.js')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'wizard.js')}"></script>
</head>
<body>
<g:render template="common/wizard"/>
</body>
</html>