<%
	/**
	 * Postview template which will display how the data was imported
	 *
	 * @author Tjeerd Abma
	 * @since 20100317
	 * @package importer
	 *
	 * Revision information:
	 * $Rev$
	 * $Author$
	 * $Date$
	 */
%>
<g:form name="postviewform" action="savepostview">
    <table>	
	<tr>	  
	    <g:each var="table" in="${datamatrix}">
		<g:each var="entity" in="${table}">
		    <tr>
			<g:each var="field" in="${entity.giveFields()}">
			    <td class="header">
				<b>${field.name}</b>
				${entity.getFieldValue(field.name)}
			    </td>
			</g:each>
		    </tr>
		</g:each>
	    </g:each>
	</tr>
    </table>
    <table>
	<tr>
	    <td colspan="">
		<input type="submit" value="This is OK, store the imported data">
	    </td>
	</tr>
    </table>
</g:form>
