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
	* $Rev: 1430 $
	* $Author: work@osx.eu $
	* $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
	*/
%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<meta name="layout" content="${layout}"/>
		<title>Compare templates</title>
		<r:require modules="templateEditor, gscf-datatables" />
		
		<r:script>
		  var standalone = ${extraparams?.standalone ? 'true' : 'false'};

		  $(document).ready(function() {
			  $('#compare_templates').dataTable( {
				"sPaginationType": "full_numbers",
				"aLengthMenu": [10, 25, 50, 100, 250, 500, 1000]
			  } );
		  } );


		  function toggleColumns()
		  {
			var columns = new Array( 1, 2, 3, 4);
			
			/* Get the DataTables object again - this is not a recreation, just a get of the object */
			var oTable = $('#compare_templates').dataTable();

			/* Toggle column visibility */
			var bVis = oTable.fnSettings().aoColumns[ columns[ 0 ] ].bVisible;
			$.each( columns, function(index, col) {
			  oTable.fnSetColumnVis( col, bVis ? false : true );
			});

			/* Show large or small titles */
			if( bVis ) {
			  // If the columns were visible, they are hidden now and large titles should be shown
			  $( '.shortTitle' ).hide();
			  $( '.longTitle' ).show();
			} else {
			  $( '.longTitle' ).hide();
			  $( '.shortTitle' ).show();
			}
		  }
		</r:script>

	</head>
	<body>
	  <h1>Template fields for templates of entity
	  <select onChange="location.href = '<g:createLink action="compare" params="${extraparams + [ extra: 'true' ]}" />&entity=' + $(this).val();">
		  <g:each in="${templateEntities}" var="ent">
			<option <g:if test="${ent.entity == entity.toString().replaceAll(/^class /, '')}">selected</g:if> value="${ent.encoded}">${ent.name}</option>
		  </g:each>
	  </select>
	  </h1>

	  <p>
		If you want more space or more information, try to <a class="toggle" href="#" onClick="toggleColumns(); return false;">toggle columns</a>.
	  </p>

	<table id="list"></table>
	<div id="pager"></div>

	<% /* Length of long titles depends on the number of templates.
		  We've got about 90 characters to show in total. However, we should show at least 3 characters */
		  def numCharsLongTitle = Math.max( 6, Math.floor( 90 / templates.size() ).intValue() );
	%>
	<table id="compare_templates">
	  <thead>
		<tr>
		  <th>Name</th>
		  <th>Type</th>
		  <th>Unit</th>
		  <th>Comment</th>
		  <th>Required</th>
		  <g:each in="${templates}" var="currentTemplate">
			<th title="${currentTemplate.name}">
			  <span class="shortTitle">
				<g:if test="${currentTemplate.name.size() > 5}">
				  ${currentTemplate.name.substring(0,3)}...
				</g:if>
				<g:else>
  				  ${currentTemplate.name}
				</g:else>
			  </span>
			  <span class="longTitle">
				<g:if test="${currentTemplate.name.size() > numCharsLongTitle}">
				  ${currentTemplate.name.substring(0, numCharsLongTitle - 3)}...
				</g:if>
				<g:else>
  				  ${currentTemplate.name}
				</g:else>
			  </span>
			</th>
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
			<td style="text-align: center;" align="center">
			  <g:if test="${currentTemplate.fields.contains(field)}">
			  	<img align="center" src="${createLinkTo( dir: 'images/icons', file: 'tick.png', plugin: 'famfamfam' )}" alt="X" />
			  </g:if>
			</td>
		  </g:each>
		</tr>
	  </g:each>
	</table>
	<div style="clear: both;"></div>
	</body>
</html>