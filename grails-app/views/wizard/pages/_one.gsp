<%
/**
 * Wizard page one
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
	<wizard:textFieldElement name="studyTitle" help="The title of the study you are creating">Title</wizard:textFieldElement>
	<wizard:textFieldElement name="studyCode" help="A code to reference your study by">Code</wizard:textFieldElement>
	<wizard:textFieldElement name="studyResearchQuestion" help="The research question">Research Question</wizard:textFieldElement>
	<wizard:textFieldElement name="studyDescription">Description</wizard:textFieldElement>
	<wizard:textFieldElement name="studyEcCode">Ethical Committee Code</wizard:textFieldElement>
</wizard:pageContent>
