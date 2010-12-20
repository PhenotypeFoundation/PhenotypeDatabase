<%
/**
 * Warning page for browsing back from samples page
 *
 * @author  Jeroen Wesbeek
 * @since   20101020
 * @package wizard
 * @see     dbnp.studycapturing.WizardTagLib::previousNext
 * @see     dbnp.studycapturing.WizardController
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<wizard:pageContent>
	<script type="text/javascript">
		var seconds = 15;
		$(document).ready(function() {
			redirect();
			//handleDots();
			//goToSave();
		});

		function redirect() {
			var s = $('#seconds');
			if (s[0]) {
				s.html( seconds );

				if ( seconds < 1) {
					goToNext();
				} else {
					seconds--;
					setTimeout("redirect();", 1000);
				}
			}
		}

		function goToNext() {
			<wizard:ajaxSubmitJs name="next" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />
		}
	</script>

	<span class="info">
		<span class="warning"><g:message code="generic.warning" />!</span>
	<g:if test="${study.samplingEvents}">
		<g:message code="studywizard.no.samplingevents.grouped" />
	</g:if>
	<g:else>
		<g:message code="studywizard.no.samplingevents" />
	</g:else>
	</span>

</wizard:pageContent>