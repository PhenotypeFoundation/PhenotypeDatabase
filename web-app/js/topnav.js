$(document).ready(function() {
	$("ul.subnav").parent().append("<span></span>");

	// menu label
	$("ul.topnav li span").parent().click(
		function() {
			$(this).find("ul.subnav").slideDown('fast').show();
			$(this).hover(function() {
			}, function() {
				$(this).find("ul.subnav").slideUp('slow');
				$(this).parent().find("ul.childnav").slideUp('slow');
			});
		}).hover(function() {
			$("span", this).addClass("subhover");
		}, function() {
			$("span", this).removeClass("subhover");
		}
	);

	// menu icon
	$("ul.topnav li span").click(
		function() {
			$(this).parent().find("ul.subnav").slideDown('fast').show();
			$(this).parent().hover(function() {
			}, function() {
				$(this).parent().find("ul.subnav").slideUp('slow');
				$(this).parent().find("ul.childnav").slideUp('slow');
			});
		}).hover(function() {
			$("a", $(this).parent()).addClass("tophover");
			$(this).addClass("subhover");
		}, function() {
			$("a", $(this).parent()).removeClass("tophover");
			$(this).removeClass("subhover");
		}
	);

	// child nav
	$("ul.childnav").each(function() {
		var childnav = $(this);
		var childparent = childnav.parent();

		childnav.hide();
		childparent.bind("click",function() {
			// hide other childnav
			$("ul.childnav", childparent.parent()).each(function() {
				$(this).slideUp('slow').hide();
			});

			// show childnav
			childnav.slideDown('fast').show();
		});
	});

	// make sure navigation is on top of everything
	$("ul.subnav").css({ 'z-index': '1000' });
});