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
<g:form name="propertiesform" action="saveproperties">
    <table>
	<tr>
	  <td>Entity:</td>

	  <g:each var="entity" in="${entities}">
	      <td class="header">
		  <b>${entity}</b>
	      </td>
	  </g:each>
    </table>
</g:form>