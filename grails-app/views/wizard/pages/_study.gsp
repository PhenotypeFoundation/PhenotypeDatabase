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
	<span class="info">
		<span class="title">Define the basic properties of your study</span>
		Pick the study template of choice (currently a fixed set) and define your study values. In this prototype the
		templated fields (below the 'note' box) are not yet handled so you can leave them empty for now.
	</span>
	
	<wizard:templateElement name="template" description="Template" value="${study?.template}" entity="${dbnp.studycapturing.Study}" addDummy="true" ajaxOnChange="switchTemplate" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" >
		The template to use for this study
	</wizard:templateElement>
	<wizard:textFieldElement name="title" description="Title" error="title" value="${study?.title}">
		The title of the study you are creating
	</wizard:textFieldElement>
	<wizard:textFieldElement name="code" description="Code" error="code" value="${study?.code}">
		A code to reference your study by
	</wizard:textFieldElement>
	<wizard:textFieldElement name="researchQuestion" description="Research Question" error="researchQuestion" value="${study?.researchQuestion}">
		The research question
	</wizard:textFieldElement>
	<wizard:textFieldElement name="description" description="Description" error="description" value="${study?.description}">
		A short description summarizing your study
	</wizard:textFieldElement>
	<wizard:textFieldElement name="ecCode" description="Ethical Committee Code" error="ecCode" value="${study?.ecCode}">
		[youtube:irvC_1ujhKo]
	</wizard:textFieldElement>
	<wizard:dateElement name="startDate" description="Start date" error="startDate" value="${study?.startDate}">
		The start date of the study
	</wizard:dateElement>
	<wizard:templateElements entity="${study}" />

</wizard:pageContent>