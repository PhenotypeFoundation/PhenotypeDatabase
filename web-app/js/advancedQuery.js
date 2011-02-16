/********************************************************
 * 
 * Javascripts to create an autocomplete for query fields
 * 
 ********************************************************/
$(function() {
	// Replace the selectbox with a textfield
	// By replacing it with javascript, users without javascript will still be able to use the select
	$( '#queryFieldSelect' ).after( $( '<input type="text" class="text" id="queryFieldText">' ));
	$( '#queryFieldText' ).after( $( '<input type="hidden" name="criteria.0.entityfield" id="queryField"></span>' ));
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
// ID = 0 is used for the input fields, in order to have them sent as well
// when the user clicks 'run query'
var criteriumId = 1;

/**
 * Adds a criteria to the list of search criteria 
 */
function addCriterion() {
	var field_descriptor = $( '#searchForm #queryField' ).val();
	var value = $( '#searchForm input#value' ).val();
	var operator = $( '#searchForm select#operator' ).val();
	
	// Show the title and a remove button
	showCriterium(field_descriptor, value, operator);
	toggleSearchMode();
	
	// Clear the input form
	$( '#searchForm #queryFieldText' ).val( '' );
	$( '#searchForm #queryField' ).val( '' );
	$( '#searchForm select#operator' ).val( 'equals' );
	$( '#searchForm input#value' ).val( '' );
}

/**
 * Removes a criterium from the list using javascript
 */
function removeCriterium(element) {
	element.remove();
	toggleSearchMode();
}

function toggleSearchMode() {
	if( $('#criteria' ).children( 'li' ) - 2 == 0 ) {
		$( '#searchMode' ).hide();
	} else {
		$( '#searchMode' ).show();
	}
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
	
	var input = $( '<a href="#" onClick="return false;"><img src="../plugins/famfamfam-1.0.1/images/icons/delete.png" border="0"></a>' );
	input.bind( 'click', function() {
		if( confirm( "Are you sure you want to remove this criterium?" ) ) {
			removeCriterium( $(this).closest( 'li' ) );
			return false;
		}
	});
	var span = $( '<span></span>' );
	span.append( "\n" ).append( input );
	
	// Increase the criteriumID to ensure a unique number every time
	criteriumId++;
	
	// Append them to a list item
	var li = $( '<li></li>' );
	li.append( fieldSpan ).append( "\n" ).append( operatorSpan ).append( "\n" ).append( valueSpan ).append( "\n" ).append( span );
	

	$('#criteria .newCriterion').before(li);
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
