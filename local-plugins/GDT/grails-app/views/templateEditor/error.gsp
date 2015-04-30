<%
	/**
	 * Template Editor error
	 *
	 * @author Jeroen Wesbeek
	 * @since 20100422
	 * @package wizard
	 * @see dbnp.studycapturing.TemplateEditorController
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
  <meta name="layout" content="${layout}"/>
  <title>error</title>
 </head>
 <body>

 <h1>Invalid request!</h1>

 <p>
	 You tried to access the template editor in an invalid way. If you feel you get this message
	 in error, please file a bugreport <a href="https://trac.nbic.nl/gscf/newticket?summary=templateEditor%20invalid%20request&version=${meta(name: 'app.version')}" target="_new">here</a> with as many details as possible.
 </p>

 <g:if test="${layout == 'main'}">
	 <g:if test="${request.getHeader('referer')}">
		 Click <a href="${request.getHeader('referer')}">here</a> to return to where you came from...
	 </g:if>
 </g:if><g:else>
		Please click 'close' to go back
 </g:else>

 </body>
</html>