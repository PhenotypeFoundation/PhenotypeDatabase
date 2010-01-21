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
	<h1>Proof of concept AJAXified Grails Webflow Wizard</h1>
	<g:form action="pages" name="wizardForm" id="wizardForm">
		<div id="wizardPage">
			<wizard:ajaxFlowRedirect form="form#wizardForm" name="next" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="attachHelpTooltips()" />
		</div>
		<!--g:render template="common/error"//-->
	</g:form>
</div>