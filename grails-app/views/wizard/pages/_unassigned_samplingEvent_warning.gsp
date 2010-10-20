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
		<span class="warning">Warning!</span>
		In the previous page one (or more) sampling events were not assigned to a group which means no samples
		will be generated for these sampling events. If this is in error, please go back to the previous
		page and make sure to properly group the sampling events. If, however, this is how you intended it, press
		next to continue, or wait <span id="seconds"></span> seconds...
	</span>

</wizard:pageContent>