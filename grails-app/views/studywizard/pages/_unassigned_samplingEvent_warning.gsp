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
 * $Rev: 1285 $
 * $Author: work@osx.eu $
 * $Date: 2010-12-20 16:28:23 +0100 (Mon, 20 Dec 2010) $
 */
%>
<af:page>
	<script type="text/javascript">
		var seconds = 15;
		$(document).ready(function() {
			redirect();
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
			<af:ajaxSubmitJs name="next" afterSuccess="onPage()" />
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

</af:page>