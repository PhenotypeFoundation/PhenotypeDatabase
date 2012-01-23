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
 * @since  20120123
 *
 * Revision information:
 * $Rev:  67320 $
 * $Author:  duh $
 * $Date:  2010-12-22 17:49:27 +0100 (Wed, 22 Dec 2010) $
 */
%>
<script type="text/javascript">
	function onPage() {
		// add waitForLoad class to ajax elements
		$('.ajax').each(function() {
			var element = $(this);
			var elementId = this.getAttribute('id');
			element.addClass('waitForLoad');

			$.getJSON(baseUrl+"/ajax/"+elementId,{},function(j) {
				var options = '';
				for (var i=0;i<j.length;i++) {
					options += '<input type="checkbox" name="species[]" value="'+j[i].id+'"/>'+j[i].name+'<br/>';
				}
				element.removeClass('waitForLoad').html(options);
			});

		});
	}
</script>

