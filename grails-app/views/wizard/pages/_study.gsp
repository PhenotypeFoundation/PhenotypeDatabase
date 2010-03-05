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
		Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce laoreet leo nec leo vehicula quis scelerisque elit pulvinar. Vivamus arcu dui, adipiscing eu vestibulum id, consectetur et erat. Aenean risus mauris, placerat et lacinia vulputate, commodo eget ligula. Pellentesque ornare blandit metus ac dictum. Donec scelerisque feugiat quam, a congue ipsum malesuada nec. Donec vulputate, diam eget porta rhoncus, est mauris ullamcorper turpis, vitae dictum risus justo quis justo. Aenean blandit feugiat accumsan. Donec porttitor bibendum elementum.
	</span>
	
	<wizard:templateElement name="template" description="Template" value="${study?.template}" entity="${dbnp.studycapturing.Study}">
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

	<span class="info">
		<span class="title">TODO</span>
		This page should also contain the template fields of the study template selected above (if available). This is
		scheduled for implementation in a later version
	</span>
	
</wizard:pageContent>