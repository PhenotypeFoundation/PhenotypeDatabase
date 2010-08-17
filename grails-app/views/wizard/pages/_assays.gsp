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
		WE NEED SOME CONTENT OVER HERE, who's the brilliant copy writer? :)
	</span>

	<wizard:templateElement name="template" description="Template" value="${assay?.template}" entity="${dbnp.studycapturing.Assay}" addDummy="true" ajaxOnChange="switchTemplate" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" >
		Choose the type of assay you would like to add
	</wizard:templateElement>
	<g:if test="${assay}">
	<wizard:templateElements entity="${assay}" />
	</g:if>

</wizard:pageContent>