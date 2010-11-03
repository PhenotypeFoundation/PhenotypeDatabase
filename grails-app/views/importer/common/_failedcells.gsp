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
<g:if env="production">
<script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.min.js')}"></script>
</g:if><g:else>
<script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.js')}"></script>
</g:else>

<script type="text/javascript">
$(document).ready(function() {
		// initialize the ontology chooser
    	new OntologyChooser().init();
});
</script>

<table>
  <tr>
    <td>Column</td><td>Row</td><td>Ontology unknown</td><td>Corrected ontology</td>
  </tr>
<g:form>
	<g:each var="record" in="${failedcells}">
          <tr>
            <td>${record.key.name}</td>
            <td>${record.value.rownum}</td>
            <td>${record.value.value}</td>
            <td>
                <input type="text" name="cell${record.value.rownum}" rel="ontology-all-name"/><br/>
                <input type="hidden" name="cell${record.value.rownum}-concept_id" /><br/>
                <input type="hidden" name="cell${record.value.rownum}-ontology_id" /><br/>
                <input type="hidden" name="cell${record.value.rownum}-full_id" /><br/>
            </td>
          </tr>
        </g:each>
  <tr>
    <td colspan="4">
      	<input type="submit" value="Accept changes">
    </td>
  </tr>

</g:form>

</table>

