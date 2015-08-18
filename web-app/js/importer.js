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

Importer.datatable = {
	/**
	 * Loads data from the given URL using the specified parameters into a datatable
	 * @return jQuery promise object to retrieve the data.
	 */
	load: function(element, url, parameters, callback) {
		var dataTable;
		var spinner = element.parent().find( ".spinner" );
		
		return $.get( url, parameters )
			.done(function(data) {
				if( dataTable ) {
					dataTable.fnDestroy();
				}
	
				// Hide the spinner and show the preview pane
				element.html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="datamatrix"></table>');
				element.show();
				spinner.hide();
				
				if( typeof(callback) != "undefined" )
					data = callback(data);
				
				dataTable = element.find( "#datamatrix" ).dataTable({
					"oLanguage": {
						"sInfo": "Showing rows _START_ to _END_ out of a total of _TOTAL_ example rows"
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
				element.empty().text( "Data preview could not be loaded. This means that the file you provided could not be properly read. Please specify another file or choose another sheet number." );
				element.show();
				spinner.hide();
			});
	}
}

Importer.upload = {
	initialize: function() {
		// Update data preview when something changes in the parameters
		$( "#uploadParameters" ).on("change", "input[type=hidden], select", function() {
			Importer.upload.updateDataPreview();
		});
		
		// Also initialize the data preview now, in case a filename has been given already
		Importer.upload.updateDataPreview();
		
		// Make sure to add an add/modify button to the template dropdown (if present)
		Importer.upload.addMoreForTemplates();
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
		
		// Perform the ajax call to retrieve the data
		Importer.datatable.load(previewTable, previewTable.data("url"), form.serialize());
	},
	
	/**
	 * Enables the addmore entry on a template selectbox
	 */
	addMoreForTemplates: function() {
	    new SelectAddMore().init({
	        rel	 : 'template',
	        url	 : baseUrl + '/templateEditor',
	        vars	: 'entity,ontologies',
	        label   : 'add / modify..',
	        style   : 'modify',
	        onClose : function() {
	            Importer.upload.refreshTemplates();
	        }
	    });		
	},
	
	/**
	 * Refreshes the list of templates on screen
	 */
	refreshTemplates: function() {
		$( "[rel=template]").each(function(idx,el) {
			var select = $(el);
			var entity = select.data("entity");
			
			$.get( baseUrl + "/template/getAllForEntity", { entity: entity }, function(data) {
				// Empty select 
				select.empty();
				select.off("change");
				
				// Add new templates
				$.each(data, function(templateId, templateName) {
					select.append( $("<option>").attr( "value", templateId ).text( templateName ) );
				});
				
				Importer.upload.addMoreForTemplates();
			});
		});
	}
	
}

Importer.match = {
	initialize: function(sessionKey, initialMapping) {
		Importer.match.showDatatable(sessionKey, initialMapping);
	},

	showDatatable: function(sessionKey, initialMapping) {
		var previewElement = $( "#data-with-headers" );
		
		// Perform the ajax call to retrieve the data
		Importer.datatable.load(
			previewElement, 
			previewElement.data("url"), 
			{ key: sessionKey },
			function(data) {
				console.log( "Enhance data", data);
				return Importer.match.addSelectBoxesToHeader(data, initialMapping);
			}
		).done(function(data) {
			if( previewElement.data( "match-url" ) ) {
				Importer.match.addMatchButtonsToDatatable(previewElement, sessionKey);
			}
		});
	},
	
	/**
	 * Adds a select box for each column
	 */
	addSelectBoxesToHeader: function(data, initialMapping) {
		// Update the datatable with select boxes to match the headers
		$.each(data.aoColumns, function(idx) {
			// Create a clone of the example header select
			var select = $("#example-header-select").clone();
			
			// Change the clone to work in the header
			select
				.attr("id", "column-match-" + idx)
				.attr("name", "column.match." + idx);
			
			// Set the initial mapping
			if( initialMapping && initialMapping[idx] ) {
				select.val(initialMapping[idx]);
			}
			
			// Add the select to the table (and show it)
			data.aoColumns[idx].sTitle += " " + select[0].outerHTML;
		});		
		
		return data;
	},

	/**
	 * Updates the datatable header with buttons to match and clear mapping
	 */
	addMatchButtonsToDatatable: function(element, sessionKey) {
		var optionsHeader = element.find( ".fg-toolbar" ).first();
		var matchOptions = $( "<div>" )
			.addClass( 'match_options' )
			.append( "<button class='match'>Match</button>" )
			.append( "<button class='clear'>Clear mapping</button>" );
		
		optionsHeader.find( ".dataTables_length" ).after(matchOptions);
		
		// Add click handlers to match buttons
		element.on( "click", "button.match", function() {
			Importer.match.matchHeaders( element, sessionKey );
			return false;
		});
		
		element.on( "click", "button.clear", function() {
			// Clear all header select boxes
			element.find( ".header-select" ).val("");
			return false;
		});		
	},
	
	/**
	 * Perform a match for each header against the possibilities
	 */
	matchHeaders: function( element, sessionKey ) {
		var url = element.data( "match-url" );
		var parameters = { key: sessionKey };
		
		return $.get( url, parameters )
			.done(function(data) {
				// The data returns contains the proposed selections. Select the right
				// values in the selectboxes
				for( columnIndex in data ) {
					$( "#column-match-" + columnIndex ).val( data[columnIndex] );
				}
			});
	}
}
