<!--
  To change this template, choose Tools | Templates
  and open the template in the editor.
-->

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
	<meta name="layout" content="main"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Export templates</title>
  </head>
  <body>
    <h1>Select template(s) to export </h1>
	<form method="post" action="<g:createLink action="export" />">
	  <input type="hidden" name="type" value="XML" />
	  <select name="templates" multiple="multiple" style="height: 200px;">
		<%
		  def groupedTemplates = [:]
		  templates.each { template ->
			if( !groupedTemplates[ template.entity ] )
			  groupedTemplates[ template.entity ] = [];

			groupedTemplates[ template.entity ] << template
		  }
		%>
		<g:each in="${groupedTemplates.keySet()}" var="entity">
		  <%
			// Remove the package part of the class name
			String FQClassName = entity.name;
			int firstChar;
			firstChar = FQClassName.lastIndexOf ('.') + 1;
			if ( firstChar > 0 ) {
			  FQClassName = FQClassName.substring ( firstChar );
			}

		  %>
		  <optgroup label="${FQClassName}">
			<g:each in="${groupedTemplates[entity]}" var="template">
			  <option value="${template.id}">${template.name}</option>
			</g:each>
		  </optgroup>
		</g:each>
	  </select><br />
	  <input type="submit" value="Export" />
	</form>
  </body>
</html>
