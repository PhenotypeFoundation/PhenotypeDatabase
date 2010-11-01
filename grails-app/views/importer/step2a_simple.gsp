<%
	/**
	 * Second step part two (A) in the importer, show misread ontologies and allow to correct.
	 *
	 * @author Tjeerd Abma
	 * @since 20101101
	 * @package importer
	 *
	 * Revision information:
	 * Rev: $rev$
	 * Author: $author$
	 * Date: $date$
	 */
%>

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta name="layout" content="main"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'importer.css')}"/>
    <title>Step 2a: values which could not be determined</title>
  </head>
  <body>
    <h1>Step 2a: values which could not be determined</h1>
    <p>The next step is to adjust values which could not be determined.</p>
  <g:each var="record" in="${datamatrix}">
     <p>Record: ${record}</p>
</g:each>
  </body>
</html>
