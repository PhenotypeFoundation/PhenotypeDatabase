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
	 * $Rev: 959 $
	 * $Author: j.a.m.wesbeek@umail.leidenuniv.nl $
	 * $Date: 2010-10-20 21:13:14 +0200 (Wed, 20 Oct 2010) $
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
    <p>The next step is to assign properties to the columns. Below you see the entities and columns, please make your
    selections.</p>
    
  <importer:properties entities="${entities}" header="${header}" templates="${templates}" layout="vertical"/>
  </body>
</html>
