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
<g:form name="propertiesform" action="saveProperties">
    <table>
	  <g:each var="stdentity" in ="${standardentities}">	      
	      
	      <% if (selectedentities.any { it.name.toLowerCase() == stdentity.value.name.toLowerCase() } && stdentity.value.name!="") { %>
	      <tr><td colspan="2"><h4>${stdentity.value.name}</h4></td></tr>	      
		<tr>
		<g:each var="selentity" in="${selectedentities}">		    
		    <g:if test="${selentity.name.toLowerCase()==stdentity.value.name.toLowerCase()}">
			    <td class="header" width="200px">
				<b>${header[selentity.columnindex.toInteger()].name}</b>
				<importer:propertyChooser name="columnproperty" mappingcolumn="${header[selentity.columnindex.toInteger()]}" allfieldtypes="${allfieldtypes}"/>
			    </td>			    		
		    </g:if>
		</g:each>
		</tr>
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
		<tr>
		  <td colspan="${header.size()}">
		      <hr />
		  </td>
		</tr>
	  <% } %> <!-- end of JSP if-->
	  </g:each>	
	<tr>
	    <td>
		<input type="hidden" name="layout" value="${layout}">
		<input type="submit" name="savebutton" value="Next"/>
	    </td>
	</tr>
    </table>    
</g:form>
