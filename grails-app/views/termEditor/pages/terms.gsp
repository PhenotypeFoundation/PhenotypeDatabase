<%
	/**
	 * Term Editor overview template
	 *
	 * @author Jeroen Wesbeek
	 * @since 20100422
	 * @package wizard
	 * @see org.dbnp.bgdt.TermEditorController
	 *
	 * Revision information:
	 * $Rev: 1430 $
	 * $Author: work@osx.eu $
	 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
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
<g:if env="development">
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.ui.autocomplete.html.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.js', plugin: 'gdt')}"></script>
</g:if><g:else>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.min.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.ui.autocomplete.html.min.js', plugin: 'gdt')}"></script>
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
        <g:hiddenField name="apikey" value="${apikey}" />
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