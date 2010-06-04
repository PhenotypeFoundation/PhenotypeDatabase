<%
/**
 * Redirect page
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
			window.location = "/${meta(name: 'app.name')}${uri}";
		});
	</script>
	
</wizard:pageContent>