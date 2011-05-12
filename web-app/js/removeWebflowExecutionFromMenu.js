// strip webflow arguments from menu anchors
$(document).ready(function() {
	// find all anchors in the menu
	$('a', $('ul.topnav')).each(function() {
		var anchor = $(this);
		anchor.attr('href', anchor.attr('href').replace(/execution=([a-z0-9]{1,})/i,""));
	});
});