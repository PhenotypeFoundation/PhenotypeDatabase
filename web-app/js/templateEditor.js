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

// Flag to keep track of whether a form is opened or not
var formOpened = false;

// Contains information about the original position of an item, when it is being dragged
var currentSort = null;

/*************************************
 *
 * Methods for the index page with templates on it
 *
 *************************************/

/**
 * Sends the user to the page where the fields of this template can be edited
 */
function editFields( id ) {
	$( '#templateSelect' ).val( id );
	$( '#templateChoice' ).submit();
}

/**
 * Shows the form to edit template properties
 */
function editTemplate( id ) {
    // Show the form is this item is not disabled
    if( !formOpened ) {
        formOpened = true;

		// Show the form
		$( '#template_' + id + '_form' ).show();
	}
}

/**
 * Hides the form to edit a template
 */
function hideTemplateForm( id ) {
    $( '#template_' + id + '_form' ).hide();

    formOpened = false;
}

/**
 * Creates a new template using AJAX
 */
function createTemplate( id ) {
    var formEl = $( '#template_' + id + '_form' );

    // Update the field
    $.ajax({
        url:        baseUrl + '/templateEditor/' + formEl.attr( 'action' ),
        data:       formEl.serialize(),
		dataType:   'json',
        type:       "POST",
        success:    function(data, textStatus, request) {
            hideTemplateForm( id );
            addTemplateListItem( data.id, data.html );
        },
        error:      function( request ) {
            alert( "Could not create template: " + request.responseText );
        }
    });
}

/**
 * Updates the properties of a template using AJAX
 */
function updateTemplate( id ) {
    var formEl = $( '#template_' + id + '_form' );

    // Update the field
    $.ajax({
        url:        baseUrl + '/templateEditor/' + formEl.attr( 'action' ),
		dataType: 'json',
        data:       formEl.serialize(),
        type:       "POST",
        success:    function(data, textStatus, request) {
            hideTemplateForm( id );
            updateTemplateListItem( id, data.html );
        },
        error:      function( request ) {
            alert( "Could not update template: " + request.responseText );
        }
    });
}

/**
 * Deletes a template field using AJAX
 */
function deleteTemplate( id ) {
    // Update the field
    $.ajax({
        url:        baseUrl + '/templateEditor/deleteTemplate',
        data:       'template=' + id,
        type:       "POST",
        success:    function(data, textStatus, request) {
			// Remove the list item
			deleteTemplateListItem( id );

			showHideEmpty( '#templates' );
        },
        error:      function( request ) {
            alert( "Could not delete template: " + request.responseText );
        }
    });

	return true;
}

// Adds a new listitem when a field has been added
function addTemplateListItem( id, newHTML ) {
	// Create a new listitem
	var li = $( newHTML );

	// Append the listitem to the list
	$( '#templates li:last').after( li );

	// Hide the 'empty' listitem, if needed
	showHideEmpty( '#templates' );
}

// Updates the contents of the listitem when something has changed
function updateTemplateListItem( id, newHTML ) {
	var li = $( '#template_' + id );
	li.replaceWith( newHTML );
}

// Removes a listitem when the template field has been deleted
function deleteTemplateListItem( id ) {
	var li = $( '#template_' + id );
	li.remove();

	// Show the 'empty' listitem if the last item is deleted
	showHideEmpty( '#templates' );
}

/*************************************
 *
 * Methods for the template page with templatefields on it
 *
 *************************************/

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
		$( '#availableTemplateFields li:not(#' + list_item_id + ')').addClass( 'ui-state-disabled' );

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
    $( '#availableTemplateFields li:not(#templateField_' + id + ')').removeClass( 'ui-state-disabled' );
	$( '#addNew').removeClass( 'ui-state-disabled' );

    formOpened = false;
}

/**
 * Adds a new template field using AJAX
 */
function createTemplateField( id ) {
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
            addFieldListItem( data.id, data.html );
        },
        error:       function( request ) {
            alert( "Could not add template field: " + request.responseText );
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
            updateFieldListItem( id, data.html );
        },
        error:      function( request ) {
            alert( "Could not update template field: " + request.responseText );
        }
    });
}

/**
 * Deletes a template field using AJAX
 */
function deleteTemplateField( id ) {
    // Delete the field
    $.ajax({
        url:        baseUrl + '/templateEditor/deleteField',
        data:       'templateField=' + id,
        type:       "POST",
        success:    function(data, textStatus, request) {
			// Put the new HTML into the list item
			deleteFieldListItem( id );

			showHideEmpty( '#availableTemplateFields' );
        },
        error:      function( request ) {
            alert( "Could not delete template field: " + request.responseText );
        }
    });

	return true;
}

/**
 * Is triggered when an item from the templatefields has been moved and
 * shoule be updated
 */
function updateTemplateFieldPosition( event, ui ) {
	// If the item is dragged to the 'availableTemplateFIelds list, we should not 'move' it
	// Otherwise, when the item is dragged onto the selectedTemplateFields, but the 'sender' is availableTemplateFields,
	// the item is added, and does not need to be moved
	if(
		ui.item.parent().attr( 'id' ) == 'availableTemplateFields' ||
		ui.item.parent().attr( 'id' ) == 'selectedTemplateFields' && ui.sender != null && ui.sender.attr( 'id' ) == 'availableTemplateFields'
	) {
		// Return true, otherwise the move operation is canceled by jquery
		return true;
	}
    // Find the new position of the element in the list
    // http://stackoverflow.com/questions/2979643/jquery-ui-sortable-position
    //
    // Because there is also a hidden 'empty template' list item in the list,
    // the number is decreased by 1
    var newposition = ui.item.index() - 1;

    // Find the ID of the templateField and template
    var item_id = ui.item.context.id;
    var templateFieldId = item_id.substring( item_id.lastIndexOf( '_' ) + 1 );
    var templateId = $('#templateSelect').val();

    // Create a URL to call and call it
    var url = baseUrl + '/templateEditor/moveField';

    // Disable sorting until this move has been saved (in order to prevent collisions
    $( '#templateFields' ).sortable( 'disable' );

    // Move the item
    $.ajax({
        url: url,
		data: 'template=' + templateId + '&templateField=' + templateFieldId + '&position=' + newposition,
		dataType: 'json',
		type: 'POST',
        success: function(data, textStatus, request) {
            updateFieldListItem( templateFieldId, data.html );
            $( '#templateFields' ).sortable( 'enable' );
        },
        error: function( request ) {
			undoMove();
			alert( "Could not move template field: " + request.responseText );
        }
    });
}

/**
 * Adds a new template field to the template using AJAX
 */
function addTemplateFieldEvent( event, ui ) {
    // Find the new position of the element in the list
    // http://stackoverflow.com/questions/2979643/jquery-ui-sortable-position
    //
    // Because there is also a hidden 'empty template' list item in the list,
    // the number is decreased by 1
    var newposition = ui.item.index() - 1;

	var item_id = ui.item.context.id;
    var id = item_id.substring( item_id.lastIndexOf( '_' ) + 1 );

	return addTemplateField( id, newposition );
}

/**
 * Adds a new template field to the template using AJAX
 */
function addTemplateField( id, newposition, moveAfterwards ) {
	if( newposition == null ) {
		newposition = -1;
	}

	if( moveAfterwards == null ) {
		moveAfterwards = false;
	}

	var templateId = $('#templateSelect').val();

    // Update the field
    $.ajax({
        url:        baseUrl + '/templateEditor/addField',
        data:       "template=" + templateId + "&templateField=" + id + "&position=" + newposition,
		dataType:	'json',
        type:       "POST",
        success:    function(data, textStatus, request) {
			// Put the new HTML into the list item
			updateFieldListItem( id, data.html );

			if( moveAfterwards ) {
				moveFieldListItem( id, '#selectedTemplateFields' );
			}

			showHideEmpty( '#selectedTemplateFields' );
			showHideEmpty( '#availableTemplateFields' );
        },
        error:      function( request ) {
			// Send the item back (if it has been moved )
			if( !moveAfterwards ) {
				undoMove();
			}

            alert( "Could not add template field: " + request.responseText );
        }
    });

	return true;
}


/**
 * Deletes a template field from a template  using AJAX
 */
function removeTemplateFieldEvent( event, ui ) {
    var item_id = ui.item.context.id;
    var id = item_id.substring( item_id.lastIndexOf( '_' ) + 1 );

	return removeTemplateField( id );
}

/**
 * Removes a template field from a template using AJAX
 */
function removeTemplateField( id, moveAfterwards ) {

	if( moveAfterwards == null ) {
		moveAfterwards = false;
	}

	var templateId = $('#templateSelect').val();

    // Update the field
    $.ajax({
        url:        baseUrl + '/templateEditor/removeField',
        data:       'template=' + templateId + '&templateField=' + id,
        type:       "POST",
        success:    function(data, textStatus, request) {
			// Put the new HTML into the list item
			updateFieldListItem( id, data.html );

			if( moveAfterwards ) {
				moveFieldListItem( id, '#availableTemplateFields' );
			}

			showHideEmpty( '#selectedTemplateFields' );
			showHideEmpty( '#availableTemplateFields' );

        },
        error:      function( request ) {
			if( !moveAfterwards ) {
				undoMove();
			}

			alert( "Could not delete template field: " + request.responseText );
        }
    });

	return true;
}


// Adds a new listitem when a field has been added
function addFieldListItem( id, newHTML ) {
	// Create a new listitem
	var li = $( newHTML );

	// Append the listitem to the list
	$( '#availableTemplateFields li:last').after( li );

	// Hide the 'empty' listitem
	showHideEmpty( '#availableTemplateFields' );
}

// Updates the contents of the listitem when something has changed
function updateFieldListItem( id, newHTML ) {
	var li = $( '#templateField_' + id );
	li.replaceWith( newHTML );
}

// Removes a listitem when the template field has been deleted
function deleteFieldListItem( id ) {
	var li = $( '#templateField_' + id );
	li.remove();

	// Show the 'empty' listitem if the last item is deleted
	showHideEmpty( '#availableTemplateFields' );
}

// Moves a listitem from one list to another
function moveFieldListItem( id, toSelector ) {
	var li = $( '#templateField_' + id );
	li.remove();

	$( toSelector ).append( li );
}

/**
 * Saves the original position of a sortable LI, in order to be able to undo the move event later on
 * This function is called on start event of the sortable lists
 */
function savePosition( event, ui ) {
	currentSort = {
		id:   ui.item.attr( 'id' ),
		parent: ui.item.context.parentNode,
		previous: ui.item.context.previousElementSibling,
		index: ui.item.index()
	}
}

/**
 * Undoes the move of an item, when an ajax call has failed
 */
function undoMove() {
	if( currentSort ) {
		var item = $( '#' + currentSort.id );
		item.remove();
		item.insertAfter( currentSort.previous );
	}
}

/**
 * Shows and hides the right 'extra' divs in the field form.
 *
 * These fields show extra input fields for stringlist and ontology fields
 *
 * @param	id	ID of the templateField
 */
function showExtraFields( id ) {
	// Find the current selected fieldtype
	var fieldType = $( '#templateField_' + id + '_form select[name=type]' ).val();

	// Hide all extra forms, and show the right one
	$( '#templateField_' + id + '_form .extra' ).hide();
	$( '#templateField_' + id + '_form .' + fieldType.toLowerCase() + '_options' ).show();
}


/** 
 * Shows or hides the list item, indicating that a list is empty
 */
function showHideEmpty( selector ) {
	// Show the 'empty' listitem if the last item is deleted
	if( $( selector + ' li:not(.empty)' ).length == 0 ) {
		$( selector + ' .empty' ).show();
	} else {
		$( selector + ' .empty' ).hide();
	}
}

/************************************
 *
 * Functions for selecting ontologies
 *
 */

function openOntologyDialog() {
	$('#ontologyDialog').dialog('open');
}

function addOntology() {
	// Add ontology using AJAX
	var url; var data;

    // Create a URL to call and call it
	if( $( '#ncboID' ).val() ) {
	    url = baseUrl + '/templateEditor/addOntologyById';
		data = 'ncboID=' + $( '#ncboID' ).val();
		$( '#ncbo_spinner' ).show();
	} else {
	    url = baseUrl + '/templateEditor/addOntologyByTerm';
		data = 'termID=' + $( '#termID' ).val();
		$( '#term_spinner' ).show();
	}

    // Move the item
    $.ajax({
        url: url,
		data: data,
		dataType: 'json',
		type: 'POST',
        success: function(data, textStatus, request) {
			updateOntologyLists( data )
			$( '#term_spinner' ).hide();
			$( '#ncbo_spinner' ).hide();

			$('#ontologyDialog').dialog('close');
        },
        error: function( request ) {
			alert( "Could not add ontology: " + request.responseText );
			$( '#term_spinner' ).hide();
			$( '#ncbo_spinner' ).hide();
			$('#ontologyDialog').dialog('close');
        }
    });
}

function updateOntologyLists( newObject ) {
	$( '.ontologySelect' ).append( '<option value="' + newObject.id + '">' + newObject.name + '</option>');
}