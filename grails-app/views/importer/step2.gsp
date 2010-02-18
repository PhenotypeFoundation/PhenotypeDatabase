<%
	/**
	 * Second step in the importer, showing the entities and the possibility
	 * to assign properties per column
	 *
	 * @author Tjeerd Abma
	 * @since 20100210
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
    <title>Step 2: import wizard entities/properties</title>
  </head>
  <body>
    <h1>Step 2: import wizard entities/properties</h1>
    <p>Below you see the entities and columns, assign properties to the columns.</p>
  <importer:properties entities="${entities}" header="${header}"/>
  </body>
</html>
