<%
	/**
	 * First step in the simple importer, showing a preview of the uploaded data
	 *
	 * @author Tjeerd Abma
	 * @since 20100512
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
    <title>Step 1: import wizard preview</title>
  </head>
  <body>
    <h1>Step 1: import wizard preview</h1>
    <p>Below you see a preview of your imported file, please correct the automatically detected types.</p>
  <importer:preview header="${header}" datamatrix="${datamatrix}"/>  
  </body>  
</html>
