<%
	/**
	 * Thirds step in the importer, showing the imported data
	 *
	 * @author Tjeerd Abma
	 * @since 20100318
	 * @package importer
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
    <meta name="layout" content="main"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'importer.css')}"/>
    <title>Step 5: import wizard finished</title>
  </head>
  <body>
    <h1>Step 5: import wizard finished</h1>
    <p>${validatedSuccesfully} of ${totalrows} rows were imported succesfully.</p>
    <p>Click <a href="${referer}">here</a> to return to the page you came from.</p>
  </body>
</html>
