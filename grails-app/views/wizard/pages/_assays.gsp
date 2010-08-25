<%
/**
 * Assays page
 *
 * @author  Jeroen Wesbeek
 * @since   20100817
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
		<span class="title">Add assays to your study</span>
		In this step you can define the various assays that were performed within this study.
		The actual (omics) data for these assays should reside in one of the assay modules that is coupled to this database (see Assay Modules).
	</span>

	<wizard:textFieldElement name="addNumber" description="Number of assays to add" error="addNumber" value="${values?.addNumber}" size="4" maxlength="4">
		The number of subjects to add to your study
	</wizard:textFieldElement>
	<wizard:templateElement name="template" description="Template" value="${assay?.template}" entity="${dbnp.studycapturing.Assay}" addDummy="true" ajaxOnChange="switchTemplate" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" >
		Choose the type of assay you would like to add
	</wizard:templateElement>
	<%
	    // TODO: switch single flow.assay to multiple flow.assays and finish this page
	%>
	<g:if test="${assay}">
	<wizard:templateElements entity="${assay}" />
	</g:if>
	<wizard:ajaxButtonElement name="add" value="Add" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()">
	</wizard:ajaxButtonElement>

	
</wizard:pageContent>