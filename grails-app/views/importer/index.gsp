<!--
  To change this template, choose Tools | Templates
  and open the template in the editor.
-->

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta name="layout" content="main"/>
    <title>Importer wizard</title>
  </head>
  <body>
    <h1>Importer wizard</h1>
    <p>You can import your Excel data to the server by choosing a file from your local harddisk in the form below.</p>
	<g:form controller="importer" method="post" action="upload" enctype="multipart/form-data">
	<table border="0">
    	<tr>
	    <td width="100px">
		Choose your Excel file to import:
	    </td>
	    <td width="100px">
		<input type="file" name="importfile"/>
	    </td>
	</tr>
	<tr>
	    <td>
		Template
	    </td>
	    <td>
		<g:select name="template_id" from="${templates}" optionKey="id"/>
	    </td>
	</tr>
	<tr>
	    <td colspan="2">
		<input type="submit" value="Next"/>
	    </td>
	</tr>
        </table>
	</g:form>

  </body>
</html>
