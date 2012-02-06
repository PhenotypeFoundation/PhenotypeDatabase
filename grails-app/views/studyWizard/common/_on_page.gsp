<%
/**
 * wizard refresh flow action
 *
 * When a page (/ partial) is rendered, any DOM event handlers need to be
 * (re-)attached. The af:ajaxButton, af:ajaxSubmitJs and af:redirect tags
 * supports calling a JavaScript after the page has been rendered by passing
 * the 'afterSuccess' argument.
 *
 * Example:	af:redirect afterSuccess="onPage();"
 * 		af:redirect afterSuccess="console.log('redirecting...');"
 *
 * Generally one would expect this code to add jQuery event handlers to
 * DOM objects in the rendered page (/ partial).
 *
 * @author Jeroen Wesbeek
 * @since  20101220
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<script type="text/javascript">
	function onPage() {
		onStudyWizardPage();

		// make sure quicksave gives user feedback
		// by showing replacing navigation with a
		// spinner
		$('input[name*="quickSave"]').bind('click', function(e) {
			$(e.target).parent().html('<img src="<g:resource dir="images/ajaxflow" file="ajax-loader.gif"/>">');
		});
	}
</script>
