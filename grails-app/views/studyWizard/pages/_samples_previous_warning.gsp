<%
/**
 * Warning page for browsing back from samples page
 *
 * @author  Jeroen Wesbeek
 * @since   20100701
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
<af:page>

	<script type="text/javascript">
		var seconds = 8;
		$(document).ready(function() {
			redirect();
		});

		function redirect() {
			var s = $('#seconds');
			if (s[0]) {
				s.html( seconds );

				if ( seconds < 1) {
					goToPrevious();
				} else {
					seconds--;
					setTimeout("redirect();", 1000);
				}
			}
		}

		function goToPrevious() {
			<wizard:ajaxSubmitJs name="previous" afterSuccess="onPage()" />
		}
	</script>

	<span class="info">
		<span class="warning">Warning!</span>
		Renaming, adding and removing subjects, sample events and / or grouping <i>may</i> result in loss of samples!<br />
		Continuing to the groups page in <span id="seconds"></span> seconds...
	</span>

</af:page>