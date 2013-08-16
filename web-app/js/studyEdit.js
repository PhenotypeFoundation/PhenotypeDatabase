$(function() {
	StudyEdit.initialize();
});

if( typeof( StudyEdit ) === "undefined" ) { 
	StudyEdit = {};
}

StudyEdit.initialize = function() {
	attachHelpTooltips();
	StudyEdit.form.attachDatePickers();
	StudyEdit.form.attachDateTimePickers();
}

/**
 * Marks a specific input field (select or input) as an error to the user
 * @param cellname		Name of the input field
 * @param cellvalue		Value that the user has entered and gives an error
 */
StudyEdit.markFailedField = function( cellname, cellvalue ) {
	// First handle the field with an error if it is a selectbox
	var element = $("select[name=" + cellname + "]");
	
	element.addClass('error')
	element.append( new Option("Invalid: " + cellvalue, "#invalidterm", false, false) );

	// Also try to handle the field if it is a textbox
	element = $("input[name=" + cellname + "]");
	element.addClass('error')
}

StudyEdit.showOpenStudyDialog = function() {
	// Only load the studies list once
	if( $( '#openstudy select' ).length == 0 ) {
		$.get( baseUrl + '/study/json', function(data) {
			var select = $( '<select>' ).attr( 'name', 'id' );
			
			$.each( data, function( idx, el) {
				console.log( el );
				select.append( $( "<option>" ).attr( "value", el.id ).text( el.title ) );
			});
			$( "#openstudy" ).append( select );
			
			$( '#openStudyDialog' ).dialog( 'open' ); 
		});
	} else {
		$( '#openStudyDialog' ).dialog( 'open' ); 
	}
}

StudyEdit.submit = function( link ) {
	var table = $(link).parents( ".dataTables_wrapper" );
	var newData = {};
	
	// If nothing has changed, just reset all fields
	if( table.find( "td.changed" ).length == 0 ) {
		return;
	}
	
	// Show a message that the system is saving data in the status bar
	table.find( ".saveChanges a" ).hide();
	table.find( ".saveChanges .saving" ).show();

	// Copy all data into the form
	table.find( ".changed input, .changed select, .changed textarea" ).each( function( idx, el ) {
		newData[ $(el).attr( "name" ) ] = $(el).val();
	});
	
	// Make sure all inputs are disabled during save. When the datatable is refreshed,
	// these fields will be editable again
	table.find( "tbody input, tbody select, tbody textarea" ).attr( "disabled", true );
	
	// Send the data to the server
	var form = $( "form#subjectForm" );
	newData[ "id" ] = form.find( "[name=id]" ).val();
	
	$.post( form.attr( "action" ), newData )
		.done( function() { console.log( "done" ); } )
		.fail( function() { console.log( "fail" ); } )
		.always( function() {
			// Reload data for the datatable
			table.find( ".dataTables_scrollBody .dataTable" ).dataTable().fnDraw();
			
			// Clear the uploaded fields in the form
			form.find( ":not(.original)" ).remove();
			
			// Reset the saveChanges row
			table.find( ".saveChanges a" ).show();
			table.find( ".saveChanges .saving" ).hide();
			table.find( ".saveChanges" ).slideUp(100);
		});
}

/*************************************************
 *
 * Functions for file upload fields
 *
 ************************************************/
// Create a file upload field
function fileUploadField(field_id) {
	/* example 2 */
	new AjaxUpload('#upload_button_' + field_id, {
		//action: 'upload.php',
		action: baseUrl + '/file/upload', // I disabled uploads in this example for security reaaons
		data : {},
		name : field_id,
		autoSubmit: true,
		onChange : function(file, ext) {
			oldFile = $('#' + field_id).val();
			if (oldFile != '' && oldFile != 'existing*' && oldFile != '*deleted*' ) {
				if (!confirm('The old file is deleted when uploading a new file. Do you want to continue?')) {
					return false;
				}
			}

			this.setData({
				'field':   field_id,
				'oldFile': oldFile
			});

			// Give feedback to the user
			$('#' + field_id + 'Example').html('Uploading ' + createFileHTML(file));
			$('#' + field_id + 'Delete').hide();

		},
		onComplete : function(file, response) {
			if (response == "") {
				$('#' + field_id).val('');
				$('#' + field_id + 'Example').html('<span class="error">Error uploading ' + createFileHTML(file) + '</span>');
				$('#' + field_id + 'Delete').hide();
			} else {
				// Sometimes, the response is returned with HTML tags. 
				// It is unknown why this happens, but the tags are not needed.
				response = response.replace(/<\/?[^>]+>/gi, '');

				$('#' + field_id).val(response);
				$('#' + field_id + 'Example').html('Uploaded ' + createFileHTML(file));
				$('#' + field_id + 'Delete').show();
			}
		}
	});
}

function deleteFile(field_id) {
	$('#' + field_id).val('*deleted*');
	$('#' + field_id + 'Example').html('File deleted');
	$('#' + field_id + 'Delete').hide();
}


function createFileHTML(filename) {
	return '<a target="_blank" href="' + baseUrl + '/file/get/' + filename + '">' + filename + '</a>';
}

/*******************************************************************
 * 
 * Functions for date and timepickers 
 * 
 *******************************************************************/
StudyEdit.form = {
	//add datepickers to date fields
	attachDatePickers: function() {
		$("input[type=text][rel$='date']").each(function() {
			$(this).datepicker({
				changeMonth : true,
				changeYear  : true,
				/*numberOfMonths: 3,*/
				showButtonPanel: true,
				dateFormat  : 'dd/mm/yy',
				yearRange   : 'c-80:c+20',
				altField	: '#' + $(this).attr('name') + 'Example',
				altFormat   : 'DD, d MM, yy'
			});
		});
	},
	
	//add datetimepickers to date fields
	attachDateTimePickers: function() {
		$("input[type=text][rel$='datetime']").each(function() {
			$(this).datepicker({
				changeMonth	 : true,
				changeYear	  : true,
				dateFormat	  : 'dd/mm/yy',
				altField		: '#' + $(this).attr('name') + 'Example',
				altTimeField	: '#' + $(this).attr('name') + 'Example2',
				altFormat	   : 'DD, d MM, yy',
				showTime		: true,
				time24h		 : true
			});
		});
	},
	
	submit: function( formId, action ) {
		var form = $( 'form#' + formId );
		
		if( action != undefined ) {
			$( 'input[name=_action]', form ).val( action );
		}
			
		form.submit();
	}
}
