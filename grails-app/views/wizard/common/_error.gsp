<%
	/**
	 * Wizard error template
	 *
	 * @author Jeroen Wesbeek
	 * @since 20100114
	 * @package wizard
	 * @see dbnp.studycapturing.WizardTagLib
	 * @see dbnp.studycapturing.WizardController
	 *
	 * Revision information:
	 * $Rev$
	 * $Author$
	 * $Date$
	 */
%>
<g:if test="${errors}">
	<div id="wizardError" class="error" title="errors">
		<g:each in="${errors}" var="error" status="e">
			<p>
				<g:if test="${!e}"><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 50px 0;"></span></g:if>
				${error.value['key']} &rarr; ${error.value['value']}
			</p>
		</g:each>
	</div>
	<script type="text/javascript">
		// mark error fields
		<g:each in="${errors}" var="error">
		<g:if test="${error.value['dynamic']}">
		$("input:[name='${error.key}']").addClass('error');
		</g:if><g:else>
		$("input:[name='${error.key}']").parent().parent().addClass('error');
		</g:else>
		</g:each>

		// show error dialog
		$(function() {
			$("div#wizardError").dialog({
				bgiframe: true,
				modal: true,
				width: 600,
				buttons: {
					Ok: function() {
						$(this).dialog('close');
					}
				}
			});
		});
	</script>
</g:if>
