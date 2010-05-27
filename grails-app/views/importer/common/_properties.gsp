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
    <p><g:select name="study.id" from="${dbnp.studycapturing.Study.list()}" optionKey="id" class="selectsmall"/></p>
    <table>
	  <g:each var="stdentity" in ="${standardentities}">
	      <% if (selectedentities.any { it.name == stdentity.value.name } && stdentity.value.name!="") { %>
	      <tr><td colspan="2"><h4>${stdentity.value.name}</h4></td></tr>
	      <tr>		  
		  <td>Columnname:</td>
		  <td>Property:</td>
	      </tr>	      
		<g:each var="selentity" in="${selectedentities}">
		    <g:if test="${selentity.name==stdentity.value.name}">
			<tr>			    
			    <td class="header" width="200px">
				<b>${header[selentity.columnindex.toInteger()].name}</b>
			    </td>
			    <td>				
				<importer:propertyChooser name="columnproperty" mappingcolumn="${header[selentity.columnindex.toInteger()]}"/>
			    </td>
			</tr>
		    </g:if>
		</g:each>
	      <tr>
		  <td colspan="2">
		      <hr />
		  </td>
	      </tr>
	  <% } %> <!-- end of JSP if-->
	  </g:each>
	<tr>
	    <td>
		<input type="submit" name="savebutton" value="Next"/>
	    </td>
	</tr>

    </table>
</g:form>
