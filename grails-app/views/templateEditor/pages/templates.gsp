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
  <title>my bla</title>
 </head>
 <body>
 templates for entity: ${entity}<br/>

 <g:if test="${templates}">
  <ul>
  <g:each in="${templates}">
     <li>${it}</li>
  </g:each>
  </ul>
 </g:if>
 
 </body>
</html>