function initializePagination( selector ) {
	if( selector == undefined ) {
		selector = ''
	}
	
	$( selector + ' .paginate').each(function(idx, el) {
		var $el = $(el);
		$el.dataTable({ 
			bJQueryUI: true, 
			bFilter: false, 
			bLengthChange: false, 
			iCookieDuration: 86400,				// Save cookie one day
			sPaginationType: 'full_numbers',
			iDisplayLength: 10,					// Number of items shown on one page.
			aoColumnDefs: [
				{ "bSortable": false, "aTargets": ["nonsortable"] }	// Disable sorting on all columns with th.nonsortable
			]						
		});
	});
	
	// Remove the top bar of the datatable and hide pagination with only one page
	$( selector + " .dataTables_wrapper").each(function(idx, el) {
		var $el = $(el);
		
		// Hide pagination if only one page is present (that is: if no buttons can be clicked)
		if($el.find('span span.ui-state-default:not(.ui-state-disabled)').size() == 0 ){
			$el.find('div.fg-toolbar').css( 'display', 'none' );
		} else {
			$el.find('div.fg-toolbar').css( 'display', 'block' );
			$el.find( 'div.ui-toolbar' ).first().hide();
			
			// Check whether a h1, h2 or h3 is present above the table, and move it into the table
			/*
			var $previousElement = $el.prev();
			if( $previousElement != undefined && $previousElement.get(0) != undefined ) {
				var tagName = $previousElement.get(0).tagName.toLowerCase();
				if( tagName == "h1" || tagName == "h2" || tagName == "h3" ) {
					// Put the margin that was on the title onto the table
					$el.css( "margin-top", $previousElement.css( "margin-top" ) );
					$previousElement.css( "margin-top", '4px' );
					$previousElement.css( "marginBottom", '4px' );

					// If so, move the element into the table
					$previousElement.remove();
					$el.find( 'div.ui-toolbar' ).first().append( $previousElement );
				}
			}
			*/						
		}
	});
	
}

$(function() { initializePagination(); });
