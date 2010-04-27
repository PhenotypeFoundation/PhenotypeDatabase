<%
	/**
	 * Template Editor overview template
	 *
	 * @author Jeroen Wesbeek
	 * @since 20100422
	 * @package wizard
	 * @see dbnp.studycapturing.TemplateEditorController
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
  <title>template editor</title>
 </head>
 <body>

  <script type="text/javascript">
	 $(function() {
		 $("#sortable").sortable({
			 placeholder: 'ui-state-highlight'
		 });
		 $("#sortable").disableSelection();
	 });
  </script>

 <g:form action="pages" name="wizardForm" id="wizardForm">
  <wizard:templateSelect name="template" description="Template" value="${template}" entity="${entity}" addDummy="true" onChange="this.form.submit();" />
 </g:form>

 <g:if test="${template}">
  <ul id="sortable">
  <g:render template="elements/all" collection="${template.fields}" />
  </ul>
 </g:if>

 add a new field:
 <g:select from="${templateFields}" />
 
 </body>
</html>