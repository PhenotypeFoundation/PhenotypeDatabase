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
 * $Rev: 959 $
 * $Author: j.a.m.wesbeek@umail.leidenuniv.nl $
 * $Date: 2010-10-20 21:13:14 +0200 (Wed, 20 Oct 2010) $
 */
%>
<af:page>
	<script type="text/javascript">
		$(document).ready(function() {
			window.location = "/${meta(name: 'app.name')}${uri}";
		});
	</script>
	
</af:page>