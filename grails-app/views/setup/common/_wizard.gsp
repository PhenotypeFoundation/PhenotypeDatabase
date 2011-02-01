<%
/**
 * Setup / Migrate assistant
 *
 * @author Jeroen Wesbeek
 * @since 20101111
 * @package setup
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<div id="wizard" class="wizard">
	<h1>Setup assistant</h1>
	<g:form action="pages" name="wizardForm" id="wizardForm" enctype="multipart/form-data">
	<g:hiddenField name="do" value="" />
		<div id="wizardPage">
			<af:ajaxFlowRedirect form="form#wizardForm" name="next" url="[controller:'setup',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />
		</div>
	</g:form>
</div>