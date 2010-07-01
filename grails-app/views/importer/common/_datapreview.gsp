<%
	/**
	 * Data preview template which will display cells and columns from a matrix datasource
	 *
	 * @author Tjeerd Abma
	 * @since 20100622
	 * @package importer
	 *
	 * Revision information:
	 * $Rev: $
	 * $Author: $
	 * $Date: $
	 */
%>
<table width="100%">
<g:each var="row" in="${datamatrix}">
	    <tr>		
		<g:each var="cell" in="${row}">
		    <td class="datamatrix">
			<g:if test="${cell.toString()==''}">.</g:if>
			<g:else><importer:displayCell cell="${cell}"/></g:else>
		    </td>
		</g:each>
	    </tr>
</g:each>
</table>