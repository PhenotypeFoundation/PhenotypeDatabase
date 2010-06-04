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
		In this step of the step-by-step study capturing tool all the basic information of a study can be filled out.
		Keep in mind that the more and the more specific the information that is filled out, the more valuable the system will be.
		Only the fields with an asterisks are obligatory. Pick the study template of choice (currently a fixed set) and define your study values.
	</span>
	
	<wizard:templateElement name="template" description="Template" value="${study?.template}" entity="${dbnp.studycapturing.Study}" addDummy="true" ajaxOnChange="switchTemplate" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" >
		Choose the type of study you would like to create.
		Depending on the chosen template specific fields can be filled out. If none of the templates contain all the necessary fields, a new template can be defined (based on other templates).
	</wizard:templateElement>
	<g:if test="${study}"><wizard:templateElements entity="${study}" /></g:if>

	<wizard:publicationSelectElement name="publication" value="${study?.publications}" />

</wizard:pageContent>