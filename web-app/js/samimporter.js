// This variable can be set to false, if no warnings have to be given
var warnOnRedirect = true;

// insert a redirect confirmation dialogue to all anchors leading the
// user away from the wizard
function insertOnRedirectWarning() {
	// find all anchors that lie outside the wizard
	$('a').each(function() {
		var element = $(this);
		var re = /^#/gi;

		// bind to the anchor?
		if (!element.attr('href').match(/^#/gi) && !element.attr('href').match(/importData/gi)) {
			// bind a warning to the onclick event
			element.bind('click', function() {
				if (warnOnRedirect) {
					return onDirectWarning();
				}
			});
		}
	});
}

function onDirectWarning() {
	return confirm('Warning: navigating away from the importer might cause loss of work and unsaved data. Are you sure you want to continue?');
}

// Initialize warnings
$(function() {
	insertOnRedirectWarning();
})
