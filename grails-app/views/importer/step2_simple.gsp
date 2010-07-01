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
    <title>Step 2: import wizard properties/columns</title>
  </head>
  <body>
    <h1>Step 2: import wizard properties/columns</h1>
    <p>The next step is to assign properties to the columns. Below you see the columns, please assign every column to
    a property.</p>
  <importer:properties entities="${entities}" header="${header}" datamatrix="${datamatrix}" templates="${templates}" allfieldtypes="true" layout="horizontal"/>
  </body>
</html>
