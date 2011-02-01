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
	 * $Rev$
	 * $Author$
	 * $Date$
	 */
%>
<div id="wizard" class="wizard">
	<h1>Query</h1>
	<g:form action="pages" name="wizardForm" id="wizardForm">
	<g:hiddenField name="do" value="" />
		<div id="wizardPage">
			<af:ajaxFlowRedirect form="form#wizardForm" name="next" url="[controller:'query',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />
		</div>
	</g:form>
</div>