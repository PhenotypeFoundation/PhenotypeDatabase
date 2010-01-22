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
	<wizard:textFieldElement name="title" description="Title" error="title" value="${study?.title}">
		The title of the study you are creating
	</wizard:textFieldElement>
	<wizard:textFieldElement name="code" description="Code" error="code" value="${study?.code}">
		A code to reference your study by
	</wizard:textFieldElement>
	<wizard:textFieldElement name="researchQuestion" description="Research Question" error="researchQuestion" value="${study?.researchQuestion}">
		The research question
	</wizard:textFieldElement>
	<wizard:textFieldElement name="description" description="Description" error="description" value="${study?.description}" />
	<wizard:textFieldElement name="ecCode" description="Ethical Committee Code" error="ecCode" value="${study?.ecCode}" />
	<wizard:dateElement name="startDate" description="Start date" error="startDate" value="${study?.startDate}">
		The start date of the study	
	</wizard:dateElement>
</wizard:pageContent>