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
	updateDataPreview: function() {
		var form = $('#uploadFile');
		var previewTable = $( "#datapreview" );
		var dataTable;
		
		// Perform the ajax call to retrieve the data
		$.get( previewTable.data( "url"), form.serialize() )
		.done(function(data) {
			if( dataTable ) {
				dataTable.fnDestroy();
			}

			$('#datapreview').html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="datamatrix"></table>');

			dataTable = $('#datapreview #datamatrix').dataTable({
				"oLanguage": {
					"sInfo": "Showing rows _START_ to _END_ out of a total of _TOTAL_ (inluding header)"
				},

				"sScrollX": "100%",
				"bScrollCollapse": true,
				"bRetrieve": false,
				"bDestroy": true,
				"iDisplayLength": 5,
				"bSort" : false,
				"aaData": jsonDatamatrix.aaData,
				"aoColumns": jsonDatamatrix.aoColumns
			});
			
			$( "#exampleData" ).show();
		})
		.fail(function() {
			$( "#datapreview" ).empty().text( "Data preview could not be loaded." );
			$( "#exampleData" ).show();
		});
	}
}
