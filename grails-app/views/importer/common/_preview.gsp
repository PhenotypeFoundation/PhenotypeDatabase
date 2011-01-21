<%
	/**
	 * Preview template which will display cells and columns from a matrix datasource
	 *
	 * @author Tjeerd Abma
	 * @since 20100129
	 * @package importer
	 *
	 * Revision information: 
	 * $Rev$
	 * $Author$
	 * $Date$
	 */
%>
<g:form name="previewform" action="savepreview">
    <table>
	<tr>
	    <td>Identifier:</td>
		<g:each var="column" in="${header}">
		    <td>
			<g:radio name="identifier" value="${column.value.index}"/>
		    </td>
		</g:each>
	    </td>
	</tr>
	<tr>
	  <td>Columnname:</td>
	  <g:each var="column" in="${header}">
	      <td class="header">
		  <b>${column.value.name}</b>
	      </td>
	  </g:each>
	</tr>

	<tr>
	    <td>Fieldtype:</td>
	    <g:each var="column" in="${header}">
		<td class="header">
		    <importer:templatefieldtypeSelect selected="${column.value.templatefieldtype}" name="templatefieldtype" customvalue="${column.key.toString()}"/>
		</td>
	    </g:each>
	</tr>
	
	<tr>
	    <td>Entity:</td>
	    <g:each var="column" in="${header}">
		<td class="header">
		    <importer:entitySelect name="entity" customvalue="${column.key.toString()}"/>
		</td>
	    </g:each>
	</tr>

	<g:each var="row" in="${datamatrix}">
	    <tr>
		<td>Value</td>
		<g:each var="cell" in="${row}">
		    <td class="datamatrix">
			<g:if test="${cell.toString()==''}">.</g:if>
			<g:else><importer:displayCell cell="${cell}"/></g:else>
		    </td>
		</g:each>
	    </tr>
	</g:each>
	
	<tr>
	    <td align="right" colspan="${datamatrix.length}"><input type="submit" value="Next"></td>
	</tr>
  </table>
  </g:form>
