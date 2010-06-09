/**
 * Template editor JavaScript Functions
 *
 * @author      Robert Horlings
 * @since       20100607
 * @package     wizard
 * @requires    jquery, jquery-ui
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

var formOpened = false;

/**
 * Is called on double click on a listitem
 */
function showTemplateFormEvent(e) {
    showTemplateFieldForm( e.target.id );
}

/**
 * Shows the form to edit a template field
 */
function showTemplateFieldForm( list_item_id ) {
    // Show the form is this item is not disabled
    if( !formOpened ) {
        formOpened = true;

		// Show the form
		$( '#' + list_item_id + '_form' ).show();

		// Disable all other listitems
		$( '#templateFields li:not(#' + list_item_id + ')').addClass( 'ui-state-disabled' );

		if( list_item_id != 'templateField_new' ) {
			// Disable add new
			$( '#addNew').addClass( 'ui-state-disabled' );
		}
	}
}

/**
 *Hides the form to edit a template field
 */
function hideTemplateFieldForm( id ) {
    $( '#templateField_' + id + '_form' ).hide();

    // Enable all other listitems
    $( '#templateFields li:not(#templateField_' + id + ')').removeClass( 'ui-state-disabled' );
	$( '#addNew').removeClass( 'ui-state-disabled' );

    formOpened = false;
}

/**
 * Is triggered when an item from the templatefields has been moved and
 * shoule be updated
 */
function updateTemplateFieldPosition( event, ui ) {
    // Find the new position of the element in the list
    // http://stackoverflow.com/questions/2979643/jquery-ui-sortable-position
    var newposition = ui.item.index();

    // Find the ID of the templateField and template
    var item_id = ui.item.context.id;
    var templateFieldId = item_id.substring( item_id.lastIndexOf( '_' ) + 1 );
    var templateId = $('#templateSelect').val();

    // Create a URL to call and call it
    var url = baseUrl + '/templateEditor/move';

    // Disable sorting until this move has been saved (in order to prevent collisions
    $( '#templateFields' ).sortable( 'disable' );

    // Move the item
    $.ajax({
        url: url,
		data: 'template=' + templateId + '&templateField=' + templateFieldId + '&position=' + newposition,
		dataType: 'json',
		type: 'POST',
        success: function(data, textStatus, request) {
            updateListItem( templateFieldId, data.html );
            $( '#templateFields' ).sortable( 'enable' );
        },
        error: function() {
            alert( "Could not move template field" );
        }
    });
}

/**
 * Adds a new template field using AJAX
 */
function addTemplateField( id ) {
    var formEl = $( '#templateField_' + id + '_form' );
	var templateId = $('#templateSelect').val();

    // Update the field
    $.ajax({
        url:        baseUrl + '/templateEditor/' + formEl.attr( 'action' ),
        data:       "template=" + templateId + "&" + formEl.serialize(),
		dataType: 'json',
        type:       "POST",
        success:    function(data, textStatus, request) {
            hideTemplateFieldForm( id );
            addListItem( data.id, data.html );
        },
        error:      function() {
            alert( "Could not add template field" );
        }
    });
}

/**
 * Updates the properties of a template field using AJAX
 */
function updateTemplateField( id ) {
    var formEl = $( '#templateField_' + id + '_form' );

    // Update the field
    $.ajax({
        url:        baseUrl + '/templateEditor/' + formEl.attr( 'action' ),
		dataType: 'json',
        data:       formEl.serialize(),
        type:       "POST",
        success:    function(data, textStatus, request) {
            hideTemplateFieldForm( id );
            updateListItem( id, data.html );
        },
        error:      function() {
            alert( "Could not update template field" );
        }
    });
}

/**
 * Deletes a template field using AJAX
 */
function deleteTemplateField( id ) {
	var templateId = $('#templateSelect').val();

    // Update the field
    $.ajax({
        url:        baseUrl + '/templateEditor/delete',
        data:       'template=' + templateId + '&templateField=' + id,
        type:       "POST",
        success:    function(data, textStatus, request) {
            hideTemplateFieldForm( id );
            deleteListItem( id );
        },
        error:      function() {
            alert( "Could not delete template field" );
        }
    });
}


// Adds a new listitem when a field has been added
function addListItem( id, newHTML ) {
	// Create a new listitem
	var li = $( '<li></li>' );
	li.attr( 'id', 'templateField_' + id );
	li.addClass( "ui-state-default" );
	
	// Insert the right HTML
	li.html( newHTML );

	// Append the listitem to the list
	$( '#templateFields li:last').after( li );

	// Hide the 'empty' listitem
	$( '#templateFields .empty' ).hide();
}

// Updates the contents of the listitem when something has changed
function updateListItem( id, newHTML ) {
	var li = $( '#templateField_' + id );
	li.html( newHTML );
}

// Removes a listitem when the template field has been deleted
function deleteListItem( id ) {
	var li = $( '#templateField_' + id );
	li.remove();

	// Show the 'empty' listitem if the last item is deleted
	if( $( '#templateFields li:not(.empty)' ).length == 0 ) {
		$( '#templateFields .empty' ).show();
	}

}