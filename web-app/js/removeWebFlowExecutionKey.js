// The grails webflow adds a 'execution=...' parameter to all links on a page
// However, this argument is only needed on webflow links (within the page)
// and gives problems when restarting the flow or going to another flow.
//
// For that reason, this argument is removed in all topnav and contextmenu links
$(document).ready(function() {
	// find all anchors in the menu
	$('ul.topnav a, #contextmenu a').each(function() {
		var anchor = $(this);
		anchor.attr('href', anchor.attr('href').replace(/execution=([a-z0-9]{1,})/i,""));
	});
});