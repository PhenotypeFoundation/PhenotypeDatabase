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
    // Show the form is this item is not disabled
    if( !formOpened ) {
        formOpened = true;
        showTemplateFieldForm( e.target.id );
    }
}

/**
 * Shows the form to edit a template field
 */
function showTemplateFieldForm( list_item_id ) {
    // Show the form
    $( '#' + list_item_id + '_form' ).show();
    
    // Disable all other listitems
    $( '#templateFields li:not(#' + list_item_id + ')').addClass( 'ui-state-disabled' );

}

/**
 *Hides the form to edit a template field
 */
function hideTemplateFieldForm( id ) {
    $( '#templateField_' + id + '_form' ).hide();

    // Enable all other listitems
    $( '#templateFields li:not(#templateField_' + id + ')').removeClass( 'ui-state-disabled' );

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
    var url = baseUrl + '/templateEditor/move?template=' + templateId + '&templateField=' + templateFieldId + '&position=' + newposition;

    // Disable sorting until this move has been saved (in order to prevent collisions
    $( '#templateFields' ).sortable( 'disable' );

    // Move the item
    $.ajax({
        url: url,
        success: function(data, textStatus, request) {
            $( '#templateFields' ).sortable( 'enable' );
        },
        error: function() {
            alert( "Could not move template field" );
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
        data:       formEl.serialize(),
        type:       "POST",
        success:    function(data, textStatus, request) {
            hideTemplateFieldForm( id );
            updateListItemTitle( id );
        },
        error:      function() {
            alert( "Could not update template field" );
        }
    });
}

// Updates the visible text on the listitem when a field is updated
function updateListItemTitle( id ) {

}