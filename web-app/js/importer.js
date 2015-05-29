if( typeof( Importer ) === "undefined" ) { 
	Importer = {};
}

Importer.initialize = function() {
	attachHelpTooltips();
}

/*******************************************************************
 * 
 * Functions for date and timepickers 
 * 
 *******************************************************************/
Importer.form = {
	submit: function( formId, action ) {
		var form = $( 'form#' + formId );
		
		if( action != undefined ) {
			$( 'input[name=_action]', form ).val( action );
		}
			
		form.submit();
	}
}

Importer.upload = {
	initialize: function() {
		$( "#uploadFile" ).on("change", "input, select", function() {
			Importer.upload.updateDataPreview();
		});
	},
	
	updateDataPreview: function() {
		// Don't update the preview, if no file is specified
		var filename = $("#file").val();
		if( !filename || filename == "existing*" ) {
			$( "#exampleData" ).hide();
			return;
		}
		
		// Show the example data with spinner
		$( "#exampleData" ).show();
		$( "#datapreview" ).hide();
		$( "#exampleData .spinner" ).show();
		
		// Retrieve parameters
		var form = $('#uploadFile');
		var previewTable = $( "#datapreview" );
		var dataTable;
		
		// Perform the ajax call to retrieve the data
		$.get( previewTable.data( "url"), form.serialize() )
		.done(function(data) {
			if( dataTable ) {
				dataTable.fnDestroy();
			}

			// Hide the spinner and show the preview pane
			$('#datapreview').html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="datamatrix"></table>');
			$( "#datapreview" ).show();
			$( "#exampleData .spinner" ).hide();
			
			dataTable = $('#datapreview #datamatrix').dataTable({
				"oLanguage": {
					"sInfo": "Showing rows _START_ to _END_ out of a total of _TOTAL_ (inluding header)"
				},

				"sScrollX": "100%",
				"sScrollY": "200px",
				"bScrollCollapse": true,
				"bAutoWidth": false,
				"bJQueryUI": true,
				"bRetrieve": false,
				"bDestroy": true,
				"iDisplayLength": 5,
				"bSort" : false,
				"aaData": data.aaData,
				"aoColumns": data.aoColumns
			});
		})
		.fail(function() {
			// Hide the spinner and show the preview pane
			$( "#datapreview" ).empty().text( "Data preview could not be loaded. This means that the file you provided could not be properly read. Please specify another file or choose another sheet number." );
			$( "#datapreview" ).show();
			$( "#exampleData .spinner" ).hide();
		});
	}
}
