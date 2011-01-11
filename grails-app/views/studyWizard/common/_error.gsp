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
 * $Rev: 1260 $
 * $Author: work@osx.eu $
 * $Date: 2010-12-10 15:12:28 +0100 (Fri, 10 Dec 2010) $
 */
%>
<g:if test="${wizardErrors}">
	<div id="wizardError" class="error" title="errors">
		<g:each in="${wizardErrors}" var="error" status="e">
			<p>
				<g:if test="${!e}"><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 50px 0;"></span></g:if>
				${error.value['key']} &rarr; ${error.value['value']}
			</p>
		</g:each>
	</div>
	<script type="text/javascript">
		// mark error fields
		<g:each in="${wizardErrors}" var="error">
		var element = $("input:[name='${error.key}'], input:[name='${error.key.toLowerCase().replaceAll("([^a-z0-9])","_")}'], select:[name='${error.key}'], select:[name='${error.key.toLowerCase().replaceAll("([^a-z0-9])","_")}'], textarea:[name='${error.key}'], textarea:[name='${error.key.toLowerCase().replaceAll("([^a-z0-9])","_")}']");
		 <g:if test="${error.value['dynamic']}">
		element.addClass('error');
		 </g:if><g:else>
		element.parent().parent().removeClass('required');
		element.parent().parent().addClass('error');
		 </g:else>
		</g:each>

		// show error dialog
		var we = $("div#wizardError");
		we.dialog({
			modal: true,
			width: 600,
			maxHeight: 400,
			buttons: {
				Ok: function() {
					$(this).dialog('close');
					we.remove();
				}
			}
		});
	</script>
</g:if>
