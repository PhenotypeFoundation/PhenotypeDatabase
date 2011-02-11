<%@ page import="org.dbnp.gdt.GdtService" %>
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


<!-- saveproperties action was defined in the form -->  
  ${mappingname}

    <table>
	      <tr><td colspan="3"><h4>defaultentity</h4></td></tr>
		<tr>
            <td class="header" width="55px">
              <input class="buttonsmall" id="clearselect" type="button" value="Clear" name="clearselect">
              <input class="buttonsmall" id="fuzzymatchselect" type="button" value="Match" name="fuzzymatchselect">
              <input type="hidden" name="fuzzymatching" id="fuzzymatching" value="false">
              <input class="buttonsmall" id="savepropertiesbutton" type="button" value="Save" name="savepropertiesbutton">
              <input class="buttonsmall" id="loadpropertiesbutton" type="button" value="Load" name="loadpropertiesbutton">
              <div id="savemapping" style="display:none">
                Give current mapping a name and press Save:
                <input type="text" name="mappingname" size="20" id="mappingname">
              </div>
              <div id="loadmapping" style="display:none">
                Select an existing mapping and press Load:
                <g:select name="importmapping" from="${importer_importmappings}" optionValue="name"/>
              </div>
            </td>           
            <g:each var="mappingcolumn" in="${importer_header}">
              <g:if test="${importer_columnproperty}">
                <g:set var="selected" value="${importer_columnproperty.index['' + mappingcolumn.index + '']}"/>
              </g:if>

			  <td class="header" width="200px">
				<b>${mappingcolumn.name}</b>
                  <importer:propertyChooser name="columnproperty" mappingcolumn="${mappingcolumn}" matchvalue="${mappingcolumn.name}" selected="${selected}" fuzzymatching="${importer_fuzzymatching}" template_id="${importer_template_id}" "allfieldtypes="true"/>
			  </td>			    		
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
	  
    </table>