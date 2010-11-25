<%
	/**
	 * Third step in the importer, showing the imported data
	 *
	 * @author Tjeerd Abma
	 * @since 20100317
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
    <title>Step 3: fill in missing mappings</title>
  </head>
  <body>
    <h1>Step 3: fill in missing mappings</h1>
    You must map the missing properties
    <importer:missingProperties datamatrix="${datamatrix}" failedcells="${failedcells}"/>
  </body>
</html>
