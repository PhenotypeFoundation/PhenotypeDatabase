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
  <meta name="layout" content="dialog"/>
  <style type="text/css">
#termForm {
	display: block;
	vertical-align: top;
}
#termForm #label {
    color:#006DBA;
    font-size:14px;
    font-weight:normal;
    display: inline-block;
    zoom: 1; /* IE 6 & 7 hack */
    *display: inline; /* IE 6 & 7 hack */
}
#termForm #term {
    display: inline-block;
    zoom: 1; /* IE 6 & 7 hack */
    *display: inline; /* IE 6 & 7 hack */
}
#termForm #button {
    display: inline-block;
    zoom: 1; /* IE 6 & 7 hack */
    *display: inline; /* IE 6 & 7 hack */
}
  </style>
<g:if env="production">
  <script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.min.js')}"></script>
  <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.ui.autocomplete.html.min.js')}"></script>
</g:if><g:else>
  <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.ui.autocomplete.html.js')}"></script>
  <script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.js')}"></script>
</g:else>
 </head>
 <body>

	<g:if test="${errors}">
 		<span class="info">
			<span class="error">Oops!</span>
			<g:each in="${errors}" var="error">
				${error}<br/>
			</g:each>
		</span>
	</g:if>
 	<g:elseif test="${message}">
  		<span class="info">
			<span class="known">Success</span>
			${message}
 		</span>
 	</g:elseif>
 	<g:else>
		<span class="info">
			<span class="title">Add a term</span>
			Use the search box below to find the term you would like to add.
		</span>
 	</g:else>

 	<div id="termForm">
 	<g:form action="pages" name="wizardForm" id="wizardForm">
		<div id="label">search term: </div>
		<div id="term"><g:textField name="term" rel="ontology-${ontologies}" size="40" /></div>
 		<div id="button"><g:submitButton name="add" value="Add term" /></div>
 	</g:form>
	</div>

 	<script type="text/javascript">
		$(document).ready(function() {
    		// initialize the ontology chooser
    		new OntologyChooser().init({
			    showHide: $('div#button'),
			    spinner: "${resource(dir: 'images', file: 'spinner.gif')}"
		    });
		});
 	</script>

 </body>
</html>