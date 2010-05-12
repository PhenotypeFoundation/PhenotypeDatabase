<%
	/**
	 * Thirds step in the importer, showing the imported data
	 *
	 * @author Tjeerd Abma
	 * @since 20100317
	 * @package importer
	 *
	 * Revision information:
	 * $Rev: 359 $
	 * $Author: duh $
	 * $Date: 2010-04-20 14:42:20 +0200 (Tue, 20 Apr 2010) $
	 */
%>

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta name="layout" content="main"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'importer.css')}"/>
    <title>Step 3: import wizard imported data postview</title>
  </head>
  <body>
    <h1>Step 3: import wizard imported data postview</h1>
    <p>A total of ${datamatrix.size()} rows were imported, below an overview of the rows is shown.</span>
    <importer:postview datamatrix="${datamatrix}"/>
  </body>
</html>
