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
        <g:each var="column" in="${header}">	    
          <td>${column.value}<br />
	      <importer:celltypeselector selected="${column.celltype}" name="celltype[${column.columnindex}]"/>
            </td>
        </g:each>
      </tr>
	<g:each var="row" in="${datamatrix}">
	    <tr>
		<g:each var="column" in="${row}">
		    <td>
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