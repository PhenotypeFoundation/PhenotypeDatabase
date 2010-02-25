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
</wizard:pageContent>