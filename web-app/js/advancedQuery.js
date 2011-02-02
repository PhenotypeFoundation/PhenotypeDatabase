/********************************************************
 * 
 * Javascripts to create an autocomplete for query fields
 * 
 ********************************************************/
$(function() {
	// Replace the selectbox with a textfield
	// By replacing it with javascript, users without javascript will still be able to use the select
	$( '#queryFieldSelect' ).after( $( '<input type="text" class="text" id="queryFieldText">' ));
	$( '#queryFieldText' ).after( $( '<input type="hidden" name="field" id="queryField"></span>' ));
	$( '#queryFieldSelect' ).remove();

	$( "#queryFieldText" ).autocomplete({
		minLength: 0,
		source: queryableFields,
		focus: function( event, ui ) {
			$( "#queryFieldText" ).val( ui.item.show );
			return false;
		},
		select: function( event, ui ) {
			$( "#queryFieldText" ).val( ui.item.show );
			$( "#queryField" ).val( ui.item.value );
			//$( "#queryFieldEntity" ).html( ui.item.entity );
			return false;
		}
	})
	.data( "autocomplete" )._renderItem = function( ul, item ) {
		return $( "<li></li>" )
			.data( "item.autocomplete", item )
			.append( "<a>" + item.show + " <span class='entity'>" + item.entity + "</a>" )
			.appendTo( ul );
	};
});

/********************************************************
 * 
 * Javascripts to add and show criteria
 * 
 ********************************************************/

// Is used to keep track of a unique ID for all criteria.
var criteriumId = 0;

/**
 * Adds a criteria to the list of search criteria 
 */
function addCriterium() {
	var field_descriptor = $( '#input_criteria [name=field]' ).val();
	var value = $( '#input_criteria input[name=value]' ).val();
	var operator = $( '#input_criteria select[name=operator]' ).val();
	
	// Show the title and a remove button
	showCriterium(field_descriptor, value, operator);
	showHideNoCriteriaItem();
	
	// Clear the input form
	$( '#input_criteria #queryFieldText' ).val( '' );
	$( '#input_criteria [name=field]' ).val( '' );
	$( '#input_criteria select[name=operator]' ).val( 'equals' );
	$( '#input_criteria input[name=value]' ).val( '' );
}

function showHideNoCriteriaItem() {
	remainingCriteria = $( '#criteria' ).children().length - 1; // -1 because one element is the 'empty' item

	if( remainingCriteria == 0 ) {
		// Show the 'none box'
		$('#criteria .emptyList').show();
		$('.submitcriteria' ).attr( 'disabled', 'disabled' );
	} else {
		// Hide the 'none box'
		$('#criteria .emptyList').hide();
		$('.submitcriteria' ).attr( 'disabled', '' );
	}	
}

/**
 * Removes a criterium from the list using javascript
 */
function removeCriterium(element) {
	element.remove();
	showHideNoCriteriaItem();
}

/**
 * Shows a criterium on the screen
 */
function showCriterium( field,  value, operator ) {
	// Create data elements
	var fieldSpan = createCriteriumElement( 'entityfield', field );
	var operatorSpan = createCriteriumElement( 'operator', operator );

	// If the operator is 'in', the value element should be somewhat nicer
	var valueSpan;
	if( operator == "in" )
		valueSpan = createInSearchElement( 'value', value );
	else 
		valueSpan = createCriteriumElement( 'value', value );
	
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

function createInSearchElement( fieldname, fieldvalue ) {
	var a  = $( '<a class="' + fieldname + '" href="' + baseUrl + '/advancedQuery/show/' + fieldvalue + '"></a>')
	a.text( "Search " + fieldvalue );
	
	var inputField = $( '<input type="hidden" name="criteria.' + criteriumId + '.' + fieldname + '" />' );
	inputField.val( fieldvalue );
	
	a.append( inputField );

	a.bind( 'click', function() {
		// Make sure the criterium is not deleted when clicking on the link
		location.href = a.attr( 'href' );
		return false;
	});	
	
	return a;
}
