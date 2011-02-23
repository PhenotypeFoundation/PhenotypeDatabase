/**
 * simple wizard javascript functions
 *
 * @author  Robert Horlings (robert@isdat.nl)
 * @since   20110221
 * @package wizard
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
var warnOnRedirect = false;

/**
 * Submits a form with a given ID and sets the 'event' field to contain the value of action
 * @param id
 * @param action
 */
function submitForm(id, action) {
	var form = $( 'form#' + id );
	
	if( action != undefined )
		$( 'input[name=event]', form ).val( action );
		
	form.submit();
}

/**
 * Submits the first form with class=simpleWizard on the page. This method is automatically called by the 
 * ajax selectAddMore options, after a popup is closed.
 * 
 * @see studyWizard/_refresh_flow.gsp
 * @see SelectAddMore.js 
 */
function refreshFlow() {
	var form = $( 'form.simpleWizard' ).first();
	
	if( form )
		submitForm( form.attr( 'id' ) )
}

/**
 * Marks a specific input field (select or input) as an error to the user
 * @param cellname		Name of the input field
 * @param cellvalue		Value that the user has entered and gives an error
 */
function markFailedField( cellname, cellvalue ) {
	// First handle the field with an error if it is a selectbox
	var element = $("select[name=" + cellname + "]");
	
	element.addClass('error')
	element.append( new Option("Invalid: " + cellvalue, "#invalidterm", false, false) );

	// Also try to handle the field if it is a textbox
	element = $("input[name=" + cellname + "]");
	element.addClass('error')
	
	
}