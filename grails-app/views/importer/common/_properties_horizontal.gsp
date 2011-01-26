<%
	/**
	 * Properties template which shows entities and allows to assign properties to columns
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
<script type="text/javascript">
// for each select field on the page
$(document).ready(function() {

$('#clearselect').click(function() {

  // for each select field on the page
  $("select").each( function(){
    // set its value to its first option
    $(this).val($('option:first', this).val());
  });

});

});
</script>
<!-- saveproperties action was defined in the form --> 
    <table>
	  <g:each var="stdentity" in ="${importer_importableentities}">
	      <% if (importer_selectedentities.any { it.name.toLowerCase() == stdentity.entity.toLowerCase() } && stdentity.entity!="") { %>
            
	      <tr><td colspan="3"><h4>${stdentity.name}</h4></td></tr>
		<tr>
            <td class="header" width="25px"><input id="clearselect" type="button" value="clear" name="clearselect"></td>
            <g:each var="selentity" in="${importer_selectedentities}">
              <g:if test="${selentity.name.toLowerCase()==stdentity.entity.toLowerCase()}">
			    <td class="header" width="200px">
				<b>${importer_header[selentity.columnindex.toInteger()].name}</b>
                  <importer:propertyChooser name="columnproperty" mappingcolumn="${importer_header[selentity.columnindex.toInteger()]}" matchvalue="${importer_header[selentity.columnindex.toInteger()].name}" template_id="${importer_template_id}" allfieldtypes="${importer_allfieldtypes}"/>
			    </td>			    		
              </g:if>
            </g:each>
		</tr>

        <g:each var="row" in="${session.importer_datamatrix}">
		<tr>
                  <td class="datamatrix">                    
                  </td>
		    <g:each var="cell" in="${row}">
			<td class="datamatrix">
			    <g:if test="${cell.toString()==''}">.</g:if>
			    <g:else><importer:displayCell cell="${cell}"/></g:else>
			</td>
		    </g:each>
		</tr>
		</g:each>

	  <% }  %> <!-- end of JSP if-->
	  </g:each>
    </table>