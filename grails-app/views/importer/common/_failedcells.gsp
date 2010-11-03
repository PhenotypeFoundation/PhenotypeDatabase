<%
	/**
	 * Failed cells template
	 *
	 * @author Tjeerd Abma
	 * @since 20101103
	 * @package importer
	 *
	 * Revision information:
	 * $Rev$
	 * $Author$
	 * $Date$
	 */
%>
<g:form>
	<g:each var="record" in="${failedcells}">
                Column: ${record.value.rownum}
                ${record.key.name} -
                ${record.value.value}<br/>
        </g:each>

	<input type="submit" value="Accept changes">
    
</g:form>    
</div>

