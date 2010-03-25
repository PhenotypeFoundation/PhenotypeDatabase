<%
	/**
	 * Wizard template with first page rendered
	 *
	 * @author Jeroen Wesbeek
	 * @since 20100113
	 * @package wizard
	 * @see dbnp.studycapturing.WizardTagLib
	 * @see dbnp.studycapturing.WizardController
	 *
	 * Revision information:
	 * $Rev: 299 $
	 * $Author: duh $
	 * $Date: 2010-03-22 14:40:35 +0100 (Mon, 22 Mar 2010) $
	 */
%>
<div id="wizard" class="wizard">
	<h1>Create a new study</h1>
	<g:form action="pages" name="wizardForm" id="wizardForm">
	<g:hiddenField name="do" value="" />
		<div id="wizardPage">
			<wizard:ajaxFlowRedirect form="form#wizardForm" name="next" url="[controller:'query',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />
		</div>
	</g:form>
</div>