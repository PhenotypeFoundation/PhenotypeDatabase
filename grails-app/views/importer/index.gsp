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
    <p>Which import wizard do you want to use?</p>

    <ol>
	<li>
	    <g:link controller="importer" action="simpleWizard">Simple wizard</g:link>
	</li>
	<li>
	    <g:link controller="importer" action="advancedWizard">Advanced wizard</g:link>
	</li>
    </ol>

  </body>
</html>
