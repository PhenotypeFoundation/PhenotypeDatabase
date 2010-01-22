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
	<wizard:textFieldElement name="studyTitle" description="Title" error="title" value="${study?.title}">
		The title of the study you are creating
	</wizard:textFieldElement>
	<wizard:textFieldElement name="studyCode" description="Code" error="code">
		A code to reference your study by
	</wizard:textFieldElement>
	<wizard:textFieldElement name="studyResearchQuestion" description="Research Question" error="researchQuestion">
		The research question
	</wizard:textFieldElement>
	<wizard:textFieldElement name="studyDescription" description="Description" error="description" />
	<wizard:textFieldElement name="studyEcCode" description="Ethical Committee Code" error="ecCode" />
</wizard:pageContent>