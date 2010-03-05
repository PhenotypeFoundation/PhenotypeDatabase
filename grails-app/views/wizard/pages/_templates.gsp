<%
/**
 * Templates page
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
	<span class="info">
		<span class="title">Select the template you would like to use</span>
		A template is a predefined set of values to store with all elements of your study.
	</span>

	<wizard:templateElement name="template" description="Template" value="${study?.template}" entity="${dbnp.studycapturing.Subject}">
		The subject template to use for this study
	</wizard:templateElement>
</wizard:pageContent>