<%
	/**
	* Template Editor compare template
	*
	* @author Robert Horlings (robert@isdat.nl)
	* @since 20101026
	* @package wizard
	* @see dbnp.studycapturing.TemplateEditorController
	*
	* Revision information:
	* $Rev: 996 $
	* $Author: robert@isdat.nl $
	* $Date: 2010-10-26 15:19:22 +0200 (Tue, 26 Oct 2010) $
	*/
%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<meta name="layout" content="${layout}"/>
		<title>Compare templates</title>
		<script src="${createLinkTo(dir: 'js', file: 'templateEditor.js')}" type="text/javascript"></script>
		<link rel="stylesheet" href="${createLinkTo(dir: 'css', file: 'templateEditor.css')}" />

		<script src="${createLinkTo(dir: 'js', file: 'jquery.dataTables.min.js')}" type="text/javascript"></script>

		<style type="text/css">
		  #content .templateEditorStep { font-size: 0.8em; }
		</style>

		<script type="text/javascript" language="javascript">
		  var standalone = ${extraparams?.standalone ? 'true' : 'false'};

		  $(document).ready(function() {
			  $('#compare_templates').dataTable( {
				"sPaginationType": "full_numbers"
			  } );
		  } );
		</script>



	</head>
	<body>
	  <h1>Template fields for templates of entity

	  <select onChange="location.href = '<g:createLink action="compare" params="${extraparams + [ extra: 'true' ]}" />&entity=' + $(this).val();">
		  <g:each in="${templateEntities}" var="entity">
			<option <g:if test="${entity.name == humanReadableEntity}">selected</g:if> value="${entity.encoded}">${entity.name}</option>
		  </g:each>
	  </select>
	  </h1>

	<table id="list"></table>
	<div id="pager"></div>

	<table id="compare_templates">
	  <thead>
		<tr>
		  <th>Name</th>
		  <th>Type</th>
		  <th>Unit</th>
		  <th>Comment</th>
		  <th>Required</th>
		  <g:each in="${templates}" var="currentTemplate">
			<th title="${currentTemplate.name}">${currentTemplate.name.substring(0,3)}...</th>
		  </g:each>
		</tr>
	  </thead>

	  <g:each in="${allFields}" var="field">
		<tr>
		  <td>${field.name}</td>
		  <td>${field.type}</td>
		  <td>${field.unit}</td>
		  <td>${field.comment}</td>
		  <td>${field.required}</td>
		  <g:each in="${templates}" var="currentTemplate">
			<td><input type="checkbox" <g:if test="${currentTemplate.fields.contains(field)}">checked="checked"</g:if> readonly="readonly" disabled="disabled" /> </td>
		  </g:each>
		</tr>
	  </g:each>
	</table>
	<div style="clear: both;"></div>
	</body>
</html>