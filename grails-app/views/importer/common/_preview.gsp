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
<g:form name="previewform" action="accept">
    <table>
	<tr>
	  <td>Columnname:</td>
	  <g:each var="column" in="${header}">
	      <td class="header">
		  <b>${column.value}</b>
	      </td>
	  </g:each>
	</tr>

	<tr>
	    <td>Datatype:</td>
	    <g:each var="column" in="${header}">
		<td class="header">
		    <importer:celltypeSelect selected="${column.celltype}" name="celltype[${column.columnindex}]"/>
		</td>
	    </g:each>
	</tr>
	
	<tr>
	    <td>Entity:</td>
	    <g:each var="column" in="${header}">
		<td class="header">
		    <importer:entitySelect name="entity[${column.columnindex}]"/>
		</td>
	    </g:each>
	</tr>

	<g:each var="row" in="${datamatrix}">
	    <tr>
		<td>Value</td>
		<g:each var="column" in="${row}">
		    <td class="datamatrix">
			<g:if test="${column.toString()==''}">.</g:if>
			<g:else>${column.toString()}</g:else>
		    </td>
		</g:each>
	    </tr>
	</g:each>
	<tr>
	    <td align="right" colspan="${datamatrix.length}"><input type="submit" value="Accept"></td>
	</tr>
  </table>
  </g:form>