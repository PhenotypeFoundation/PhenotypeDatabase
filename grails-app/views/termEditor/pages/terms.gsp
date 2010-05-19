<%
	/**
	 * Term Editor overview template
	 *
	 * @author Jeroen Wesbeek
	 * @since 20100422
	 * @package wizard
	 * @see dbnp.studycapturing.TermEditorController
	 *
	 * Revision information:
	 * $Rev$
	 * $Author$
	 * $Date$
	 */
%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
 <head>
  <meta name="layout" content="dialog"/><g:if env="production">
  <script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.min.js')}"></script>
</g:if><g:else>
  <script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.js')}"></script>
</g:else>
 </head>
 <body>

 <g:form action="pages" name="wizardForm" id="wizardForm">
 <g:textField name="term" rel="ontology-${ontologies}" />
 <g:submitButton name="add" value="go go goooooo!" />
 </g:form>

 ${errors}

 <script type="text/javascript">
	$(document).ready(function() {
    	// initialize the ontology chooser
    	new OntologyChooser().init();
	});
 </script>

 </body>
</html>