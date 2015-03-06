/**
 * Template editor JavaScript Functions
 *
 * @author      Robert Horlings
 * @since       20100607
 * @package     wizard
 * @requires    jquery, jquery-ui
 *
 * Revision information:
 * $Rev: 1189 $
 * $Author: robert@isdat.nl $
 * $Date: 2010-11-23 10:11:15 +0100 (Tue, 23 Nov 2010) $
 */

// Flag to keep track of whether a form is opened or not
var formOpened = false;

// Contains information about the original position of an item, when it is being dragged
var currentSort = null;

function userMessage( message ) {
	alert( message );
}

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

        // Disable all other listitems
		$( '#templates li:not(#template_' + id + ')').addClass( 'ui-state-disabled' );

		// Make sure the opened form is on the page, so scroll down if needed
		var formElement = $( '#template_' + id + '_form' ).parent();
		var formBottom = formElement.offset().top + formElement.height();
		var margin = 10;

		if( formBottom > $(document).scrollTop() + $(window).height() ) {
			$('html, body').animate( { scrollTop: formBottom + margin - $(window).height() }, 200 );
	}

		if( id != 'new' && id != 'requestTemplate') {
			// Disable add new
            $( '#addNew').addClass( 'ui-state-disabled' );
		}
	}
}

/**
 * Hides the form to edit a template
 */
function hideTemplateForm( id ) {
    $( '#template_' + id + '_form' ).hide();

    // Enable all other listitems
    $( '#templates li:not(#template_' + id + ')').removeClass( 'ui-state-disabled' );
	$( '#addNew').removeClass( 'ui-state-disabled' );

    formOpened = false;
}


/**
 * Clears the form after adding a template
 */
function clearTemplateForm( id ) {
    $( '#template_' + id + '_form input#name' ).val( "" );
    $( '#template_' + id + '_form textarea' ).val( "" );
}

/**
 * Clears the form after submitting a templaterequest
 */
function clearTemplateRequestForm( id ) {
    $( '#template_' + id + '_form input#rname' ).val( "" );
    $( '#template_' + id + '_form textarea' ).val( "" );
}

/**
 * Creates a new template using AJAX
 */
function createTemplate( id ) {
    var formEl = $( '#template_' + id + '_form' );

	showWaiting();

    // Update the field
    $.ajax({
        url:        baseUrl + '/templateEditor/' + formEl.attr( 'action' ),
        data:       formEl.serialize(),
		dataType:   'json',
        type:       "POST",
        success:    function(data, textStatus, request) {
            clearTemplateForm( id );
			hideTemplateForm( id );
            addTemplateListItem( data.id, data.html );
        },
        error:      function( request ) {
            alert( "Could not create template: " + request.responseText );
        },
		complete: function( request, textStatus ) {
			hideWaiting();
			userMessage( 'Your template was created and added to the list.' );
		}
    });
}

/**
 * Updates the properties of a template using AJAX
 */
function updateTemplate( id ) {
    var formEl = $( '#template_' + id + '_form' );

	showWaiting();

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
        	userMessage( "Could not update template" );
        },
		complete: function( request, textStatus ) {
			hideWaiting();
			userMessage( 'Your template was updated.' );
		}
    });
}

/**
 * Deletes a template field using AJAX
 */
function deleteTemplate( id ) {

	showWaiting();

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
        },
		complete: function( request, textStatus ) {
			hideWaiting();
			userMessage( 'Your template was deleted.' );
		}
    });

	return true;
}

/**
 * Clones a template using AJAX
 */
function cloneTemplate( id ) {
	showWaiting();

    // Update the field
    $.ajax({
        url:        baseUrl + '/templateEditor/cloneTemplate/' + id,
		dataType:   'json',
        type:       "POST",
        success:    function(data, textStatus, request) {
            addTemplateListItem( data.id, data.html );
        },
        error:      function( request ) {
            alert( "Could not clone template: " + request.responseText );
        },
		complete: function( request, textStatus ) {
			hideWaiting();
			userMessage( 'A copy of your template has been created.' );
		}
    });
}

/**
 * Opens templateRequest dialog
 */
function requestTemplate( id ) {
    var formEl = $( '#template_' + id + '_form' );

	showWaiting();

    // Update the field
    $.ajax({
        url:        baseUrl + '/templateEditor/' + formEl.attr( 'action' ),
        data:       formEl.serialize(),
		dataType:   'json',
        type:       "POST",
        success:    function(data, textStatus, request) {
            clearTemplateRequestForm( id );
			hideTemplateForm( id );
        },
        error:      function( request ) {
            alert( "Could not create template: " + request.responseText );
        },
		complete: function( request, textStatus ) {
			hideWaiting();
            alert ("Your request has been sent to the templateadmin(s)")
        }
    });
}

/**
 * Opens templateRequest dialog
 */
function requestTemplateField( id ) {
    var formEl = $( '#templateField_' + id + '_form' );

    showWaiting();

    // Update the field
    $.ajax({
        url:        baseUrl + '/templateEditor/' + formEl.attr( 'action' ),
        data:       formEl.serialize(),
		dataType:   'json',
        type:       "POST",
        success:    function(data, textStatus, request) {
            clearTemplateFieldRequestForm( id );
			hideTemplateFieldForm( id );
        },
        error:      function( request ) {
            alert( "Could not create template: " + request.responseText );
        },
		complete: function( request, textStatus ) {
			hideWaiting();
            alert ("Your request has been sent to the templateadmin(s)")
		}
    });
}

// Adds a new listitem when a field has been added
function  addTemplateListItem( id, newHTML ) {
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
		$( '#templateField_' + list_item_id + '_form' ).show();

		// Disable all other listitems
		$( '#availableTemplateFields li:not(#templateField_' + list_item_id + ')').addClass( 'ui-state-disabled' );
		$( '#selectedTemplateFields li:not(#templateField_' + list_item_id + ')').addClass( 'ui-state-disabled' );
        $( '#disabledavailableTemplateFields li:not(#templateField_' + list_item_id + ')').addClass( 'ui-state-disabled' );
   		$( '#disabledselectedTemplateFields li:not(#templateField_' + list_item_id + ')').addClass( 'ui-state-disabled' );

		// Make sure the opened form is on the page, so scroll down if needed
		var formElement = $( '#templateField_' + list_item_id + '_form' ).parent();
		var formBottom = formElement.offset().top + formElement.height();
		var margin = 10;
		
		if( formBottom > $(document).scrollTop() + $(window).height() ) {
			$('html, body').animate( { scrollTop: formBottom + margin - $(window).height() }, 200 );
		}

		if( list_item_id != 'new' && list_item_id != 'requestTemplateField') {
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
	$( '#selectedTemplateFields li:not(#templateField_' + id + ')').removeClass( 'ui-state-disabled' );
    $( '#disabledavailableTemplateFields li:not(#templateField_' + id + ')').removeClass( 'ui-state-disabled' );
	$( '#disabledselectedTemplateFields li:not(#templateField_' + id + ')').removeClass( 'ui-state-disabled' );
	$( '#addNew').removeClass( 'ui-state-disabled' );

    formOpened = false;
}

/**
 * Clears the form to add a template field after adding one
 */
function clearTemplateFieldForm( id ) {
    $( '#templateField_' + id + '_form input[name=name]' ).val( "" );
    $( '#templateField_' + id + '_form input[name=unit]' ).val( "" );
    $( '#templateField_' + id + '_form input[name=required]' ).attr( "checked", "" );
    $( '#templateField_' + id + '_form textarea' ).val( "" );
    $( '#templateField_' + id + '_form select' ).attr( 'selectedIndex', 0 );
    $( '#templateField_' + id + '_form .extra' ).hide();
}

/**
 * Clears the form after submitting a templaterequest
 */
function clearTemplateFieldRequestForm( id ) {
    $( '#templateField_' + id + '_form input[name=rname]' ).val( "" );
    $( '#templateField_' + id + '_form textarea' ).val( "" );
    $( '#templateField_' + id + '_form select' ).attr( 'selectedIndex', 0 );
}

/**
 * Adds a new template field using AJAX
 */
function createTemplateField( id ) {
    var formEl = $( '#templateField_' + id + '_form' );
	var templateId = $('#templateSelect').val();

	showWaiting();

    // Update the field
    $.ajax({
        url:        baseUrl + '/templateEditor/' + formEl.attr( 'action' ),
        data:       "template=" + templateId + "&" + formEl.serialize(),
		dataType: 'json',
        type:       "POST",
        success:    function(data, textStatus, request) {
            clearTemplateFieldForm( id );
            hideTemplateFieldForm( id );
            addFieldListItem( data.id, data.html );
			userMessage( 'A new template field was created and can be added to your template.' );
        },
        error:       function( request ) {
            alert( "Could not add template field: " + request.responseText );
        },
		complete: function( request, textStatus ) {
			hideWaiting();
		}
    });
}

/**
 * Updates the properties of a template field using AJAX
 */
function updateTemplateField( id ) {
    var formEl = $( '#templateField_' + id + '_form' );

	showWaiting();

    //ensure all ontologies present in the selectbox are selected
    $("#ontologies_" + id).children().attr("selected", "selected");

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
        },
		complete: function( request, textStatus ) {
			hideWaiting();
			userMessage( 'Your template field has been updated.' );
		}
    });
}

/**
 * Deletes a template field using AJAX
 */
function deleteTemplateField( id ) {
	showWaiting();

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
        },
		complete: function( request, textStatus ) {
			hideWaiting();
			userMessage( 'Your template field has been deleted.' );
		}
    });

	return true;
}

/**
 * Is triggered when an item from the templatefields has been moved and
 * should be updated
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
    // There is also a hidden 'empty template' list item in the list. This is
	// the last item in the list, so it doesn't matter in this computation
    var newposition = ui.item.index();

    // Find the ID of the templateField and template
    var item_id = ui.item.context.id;
    var templateFieldId = item_id.substring( item_id.lastIndexOf( '_' ) + 1 );
    var templateId = $('#templateSelect').val();

    // Create a URL to call and call it
    var url = baseUrl + '/templateEditor/moveField';

    // Disable sorting until this move has been saved (in order to prevent collisions
    $( '#templateFields' ).sortable( 'disable' );

	showWaiting();

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
        },
		complete: function( request, textStatus ) {
			hideWaiting();
		}
    });
}

/**
 * Adds a new template field to the template using AJAX
 */
function addTemplateFieldEvent( event, ui ) {
    // Find the new position of the element in the list
    // http://stackoverflow.com/questions/2979643/jquery-ui-sortable-position
    var newposition = ui.item.index();
    
    // There is also a hidden 'empty template' list item in the list. The user might
	// move the new field to the last position, after the 'empty template' item. In that
	// case, the index is not correct anymore. For that reason, the index is lowered when
	// the empty item is before this one
    if( $( '.empty', ui.item.parent() ).index() < newposition )
    	newposition--;

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

	showWaiting();

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
        },
		complete: function( request, textStatus ) {
			hideWaiting();
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

	showWaiting();

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
        },
		complete: function( request, textStatus ) {
			hideWaiting();
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
		
		// Check whether a previous item is saved. If not, the moved item was the 
		// only item in the list.
		if( currentSort.previous != null ) {
			item.insertAfter( currentSort.previous );
		} else {
			$(currentSort.parent).append( item );
		}
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

function showWaiting() { $( '.wait' ).show() }
function hideWaiting() { $( '.wait' ).hide() }

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
    url = baseUrl + '/templateEditor/addOntologyById';
	if( $( '#ontologyAcronym' ).val() ) {
		data = 'ontology_id=' + $( '#addOntology input[name=ontologyAcronym-ontology_id]' ).val();
		$( '#ncbo_spinner' ).show();
	} else {
		data = 'ontology_id=' + $( '#addOntology input[name=termID-ontology_id]' ).val();
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

function updateOntologyLists( newObject) {
    if( $('#selectedTemplateFields :not(.ui-state-disabled) option[value='+newObject.id+']').length <= 0 ) {
	    $( '.ontologySelect' ).append( '<option title="' + newObject.name + '" value="' + newObject.id + '">' +
            newObject.name + '</option>');
    } else {
        userMessage("This ontology is already added.");
    }
}

function deleteOntology( id ) {
    $("#ontologies_" + id).children().remove(":selected");
}