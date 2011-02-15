function checkAllPaginated( input ) {
	var paginatedTable = $(input).closest( '.paginate' );
	var dataTable = paginatedTable.closest( '.dataTables_wrapper' );
	var checkAll = $( '#checkAll', paginatedTable );
	
	var oTable = paginatedTable.dataTable();
	var inputs = $('input', oTable.fnGetNodes())
	
	// If any of the inputs is checked, uncheck all. Otherwise, check all
	var check = false;
	
	for(var i = 0; i < inputs.length; i++ ) {
		if( !$(inputs[i]).attr( 'checked' ) ) {
			check = true;
			break;
		}
	}
	
	inputs.each( function( idx, el ) {
		$(el).attr( 'checked', check );
	})
	
	updateCheckAll( checkAll );
}

function updateCheckAll( input ) {
	var paginatedTable = $(input).closest( '.paginate' );
	var dataTable = paginatedTable.closest( '.dataTables_wrapper' );
	
	var checkAll = $( '#checkAll', paginatedTable );
	
	var oTable = paginatedTable.dataTable();
	var inputs = $('input', oTable.fnGetNodes())
	
	// Is none checked, are all checked or are some checked
	var numChecked = 0
	for(var i = 0; i < inputs.length; i++ ) {
		if( $(inputs[i]).attr( 'checked' ) ) {
			numChecked++;
		}
	}
	
	checkAll.attr( 'checked', numChecked > 0 );
	
	if( numChecked > 0 && numChecked < inputs.length - 1 ) {
		checkAll.addClass( 'transparent' );
	} else {
		checkAll.removeClass( 'transparent' );
	}
}

function submitForm( form, url ) {
	if( form == undefined || !form )
		return;

	form.attr( 'action', baseUrl + url );
	form.submit();
}
function performAction( form, action, module ) {
	// Make sure the data from the paginated table is submitted
	// This is performed with javascript, because otherwise
	// checkboxes of hidden rows won't be taken into account
	// See also http://datatables.net/examples/api/form.html
	
	// First remove all previously created inputs, in order to avoid any collissions
	$( 'input.created' ).remove();

	// Also perform a check whether any of the results is checked
	var checked = false;
	
	// Loop through all paginated inputs
	var oTable = $('#searchresults').dataTable();
	$('input', oTable.fnGetNodes()).each(function(idx,el) {
		var $el = $(el);
		if( $el.attr( 'name' ) == "id" && $(el).attr( 'checked' ) ) {
			checked = true;
			form.append( $( '<input type="hidden" name="id" value="' + $el.attr( 'value' ) + '" class="created" />' ) );
		}
	})

	if( !checked ) {
		alert( "Please pick at least one result to perform this action on." );
		return;
	}
	
	// Fill action and module names
	$( '[name=actionName]', form ).val( action );
	$( '[name=moduleName]', form ).val( module );
	submitForm( form, '/advancedQuery/performAction' );
}
