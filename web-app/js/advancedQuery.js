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
			selectQueryableFieldItem( ui.item );
			//$( "#queryFieldEntity" ).html( ui.item.entity );
			return false;
		},
		change: function( event, ui ) {
			// If the user has left the field blank, remove the field that has been selected
			if( $( '#queryFieldText' ).val().trim() == "" ) {
				selectQueryableFieldItem( null );
			}
			// If no item is selected and the user has entered some text, select the first one
			// See https://github.com/scottgonzalez/jquery-ui-extensions/blob/master/autocomplete/jquery.ui.autocomplete.selectFirst.js
			else if( ui.item == null ) {
				var el = $( '#queryFieldText' ).autocomplete().data( "autocomplete" );
				
				// Check how many fields are in the list. However, if the user first enters
				// a term that shows items, and afterwards continues typing, the menu items 
				// will remain in the list, but are hidden.
				// For that reason we perform an extra check to see whether the value of the
				// first item matches the entered text
				var searchResults = $.ui.autocomplete.filter( queryableFields, $( '#queryFieldText' ).val() );
				if( searchResults && searchResults.length > 0 ) {
					selectQueryableFieldItem( searchResults[ 0 ] );
				} else {
					// Clear the input field if nothing is in the list
					selectQueryableFieldItem( null );
				}
			}
		}
	})
	.data( "autocomplete" )._renderItem = function( ul, item ) {
		return $( "<li></li>" )
			.data( "item.autocomplete", item )
			.append( "<a>" + item.show + " <span class='entity'>" + item.entity + "</span></a>" )
			.appendTo( ul );
	};
});

/**
 * Selects a field in the select combo box
 * @param item	THe selected item or null if nothing is selected
 */
function selectQueryableFieldItem( item ) {
	var show = "";
	var value = "";
	
	if( item != null ) {
		show = item.show;
		value = item.value;

		// Only hide the text if something is chosen. Otherwise, the entered
		// text remains
		$( "#queryFieldText" ).val( show );
	}
	
	$( "#queryField" ).val( value );
	
	if( value == "" ) {
		$( "#queryFieldText" ).css( "background-color", "#FDD" );
		$( ".newCriterion .addButton a" ).addClass( "disabled" );
	} else {
		$( "#queryFieldText" ).css( "background-color", "white" );
		$( ".newCriterion .addButton a" ).removeClass( "disabled" );
	}
	
	// Enable or disabled the search button
	toggleSearchButton()
}

/**
 * Enables or disabled the search button, based on the number of criteria
 * and the state of the input field
 */
function toggleSearchButton() {
	if( $( "#criteria li:not(.newCriterion):not(.titlerow)" ).length == 0 && $( "#queryField" ).val() == "" ) {
		$( '.submitcriteria' ).attr( 'disabled', true );
	} else {
		$( '.submitcriteria' ).removeAttr("disabled");
	}
	
}

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
	
	if( field_descriptor == "" ) {
		alert( "Please select a field to search in." );
		return;
	}
	
	// Show the title and a remove button
	showCriterium(field_descriptor, value, operator);
	toggleSearchMode();
	
	// Clear the input form
	$( '#searchForm #queryFieldText' ).val( '' );
	$( '#searchForm #queryField' ).val( '' );
	$( '#searchForm select#operator' ).val( 'equals' );
	$( '#searchForm input#value' ).val( '' );
	$( "#searchForm .newCriterion .addButton a" ).addClass( "disabled" );
	
}

/**
 * Removes a criterium from the list using javascript
 */
function removeCriterium(element) {
	element.remove();
	toggleSearchMode();
	toggleSearchButton();
}

function toggleSearchMode() {
	if( $( "#criteria li:not(.newCriterion):not(.titlerow)" ).length == 0 ) {
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
	var a  = $( '<a class="' + fieldname + '" href="' + fieldvalue.url  + '"></a>')
	a.text( fieldvalue.description );
	
	var inputField = $( '<input type="hidden" name="criteria.' + criteriumId + '.' + fieldname + '" />' );
	inputField.val( fieldvalue.id );
	
	a.append( inputField );

	a.bind( 'click', function() {
		// Make sure the criterium is not deleted when clicking on the link
		location.href = a.attr( 'href' );
		return false;
	});	
	
	return a;
}
