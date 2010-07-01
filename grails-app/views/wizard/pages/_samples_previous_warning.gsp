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
<wizard:pageContent>

	<script type="text/javascript">
		var seconds = 8;
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
					goToPrevious();
				} else {
					seconds--;
					setTimeout("redirect();", 1000);
				}
			}
		}

		function goToPrevious() {
			<wizard:ajaxSubmitJs name="previous" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />
		}
	</script>

	<span class="info">
		<span class="warning">Warning!</span>
		Renaming, adding and removing subjects, sample events and / or grouping <i>may</i> result in (some of the) samples to be reset!<br />
		Continuing to the groups page in <span id="seconds"></span> seconds...
	</span>

</wizard:pageContent>