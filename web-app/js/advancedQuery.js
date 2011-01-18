// Is used to keep track of a unique ID for all criteria.
var criteriumId = 0;

/**
 * Adds a criteria to the list of search criteria 
 */
function addCriterium() {
	var field_descriptor = $( '#input_criteria select[name=field]' ).val();
	var value = $( '#input_criteria input[name=value]' ).val();
	var operator = 'equals';
	
	// Show the title and a remove button
	showCriterium(field_descriptor, value, operator);

	// Hide the 'none box'
	$('#criteria .emptyList').hide();
	$('.submitcriteria' ).attr( 'disabled', '' );
	
	// Clear the input form
	$( '#input_criteria select[name=field]' ).val( '' );
	$( '#input_criteria input[name=value]' ).val( '' );
}

/**
 * Removes a criterium from the list using javascript
 */
function removeCriterium(element) {
	var remainingCriteria = element.parent().children().length - 2; // -2 because one element will be removed and the other one is the 'empty' item
	element.remove();

	// Show the 'none box' if needed
	if (remainingCriteria == 0) {
		$('.emptyList').show();
		$('.submitcriteria' ).attr( 'disabled', 'disabled' );
	}
}

/**
 * Shows a criterium on the screen
 */
function showCriterium( field,  value, operator ) {
	// Create data elements
	var fieldSpan = createCriteriumElement( 'entityfield', field );
	var valueSpan = createCriteriumElement( 'value', value );
	var operatorSpan = createCriteriumElement( 'operator', operator );
	
	// Increase the criteriumID to ensure a unique number every time
	criteriumId++;
	
	// Append them to a list item
	var li = $( '<li></li>' );
	li.append( fieldSpan ).append( operatorSpan ).append( valueSpan );
	
	
	li.bind( 'click', function() {
		if( confirm( "Are you sure you want to remove this criterium?" ) ) {
			removeCriterium( $(this) );
			return false;
		}
	});

	$('#criteria').append(li);
}

function createCriteriumElement( fieldname, fieldvalue ) {
	var span = $( '<span class="' + fieldname + '"></span>' );
	span.text( fieldvalue );
	
	var inputField = $( '<input type="hidden" name="criteria.' + criteriumId + '.' + fieldname + '" />' );
	inputField.val( fieldvalue );
	
	span.append( inputField );
	
	return span;
	
}
