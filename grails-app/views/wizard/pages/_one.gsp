<%
/**
 * Study page
 *
 * @author  Jeroen Wesbeek
 * @since   20100113
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
	<wizard:textFieldElement name="studyTitle" description="Title">
		The title of the study you are creating
	</wizard:textFieldElement>
	<wizard:textFieldElement name="studyCode" description="Code">
		A code to reference your study by
	</wizard:textFieldElement>
	<wizard:textFieldElement name="studyResearchQuestion" description="Research Question">
		The research question
	</wizard:textFieldElement>
	<wizard:textFieldElement name="studyDescription" description="Description" />
	<wizard:textFieldElement name="studyEcCode" description="Ethical Committee Code" />
</wizard:pageContent>