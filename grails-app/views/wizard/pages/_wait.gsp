<%
/**
 * Finish page
 *
 * @author  Jeroen Wesbeek
 * @since   20100212
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
		$(document).ready(function() {
			handleDots();
			goToSave();
		});

		function handleDots() {
			var dots = $('#dots');

			if (dots[0]) {
				var html = dots.html();
				dots.html( ((html.length) < 5) ? html+"." : "")
				setTimeout("handleDots();", 1000);
			}
		}

		function goToSave() {
			<wizard:ajaxSubmitJs name="next" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" class="prevnext" />
		}
	</script>

	<span class="info">
		<span class="spinner">Saving study</span>
		<img src="${resource(dir: 'images', file: 'spinner.gif')}">
		Please wait your study is being saved <span id="dots"></span>
	</span>

</wizard:pageContent>