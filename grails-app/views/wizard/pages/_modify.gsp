<%
/**
 * Load study to modify page
 *
 * @author  Jeroen Wesbeek
 * @since   20100414
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
		<span class="title">Edit a study</span>
		Select the study you would like to modify.
		<b><i>Note: this functionality is currently in beta and has not been verified properly... use with care!</i></b>
	</span>

	<wizard:studyElement name="study" description="Study" error="study" value="">
		The study you would like to load and edit
	</wizard:studyElement>

</wizard:pageContent>