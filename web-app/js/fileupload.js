/*************************************************
 *
 * Functions for file upload fields
 *
 ************************************************/
var FileUpload = {
	// Create a file upload field
	// Options can include:
	//		truncate: 	the maximum number of characters in the filename to be shown
	//		onUpload:	method to be called after a file is uploaded
	convertFileField: function(field_id, options) {
		var that = this;
		
		if( typeof(options) == 'undefined' ) {
			options = {};
		}

		var upload = $('#upload_button_' + field_id).fileupload({
			dataType: 'text', // type of the response
			replaceFileInput: false,
			url: baseUrl + '/file/upload',
			paramName: field_id,
			add: function(e, data) {
				oldFile = $('#' + field_id).val();
				if (oldFile != '' && oldFile != 'existing*' && oldFile != '*deleted*' ) {
					if (!confirm('The old file is deleted when uploading a new file. Do you want to continue?')) {
						return false;
					}
				}
				data.formData = {
					'field': field_id,
					'oldFile': oldFile
				};

				// Give feedback to the user
				var filename = data.files[0].name;
				$('#' + field_id + 'Example').html('Uploading ' + that.createFileHTML(filename, options.truncate));
				$('#' + field_id + 'Delete').hide();

				data.submit();
			},
			done: function (e, data) {
				var response = data.result;
				var filename = data.files[0].name;

				//If there is HTML in the response, just retrieve the text value.
				if(response.indexOf("<") != -1) {
					response = $(response).text().split("//<![CDATA[")[0];
				}
				if (response == "") {
					$('#' + field_id).val('');
					$('#' + field_id + 'Example').html('<span class="error">Error uploading ' + that.createFileHTML(filename, options.truncate) + '</span>');
					$('#' + field_id + 'Delete').hide();
				} else {
					$('#' + field_id).val(response).change(); // explicitly trigger change event, as this is not called when changing programmatically
					$('#' + field_id + 'Example').html('Uploaded ' + that.createFileHTML(filename, options.truncate));
					$('#' + field_id + 'Delete').show();
				}

				// Call user specified onComplete method
				if( typeof(options.onUpload) != 'undefined' ) {
					options.onUpload(file, response);
				}
			}
		});

		// Reroute click from icon to (invisible) file input
		$('#upload_icon_' + field_id).on('click', function() {
			$('#upload_button_' + field_id).click();
		})

		// Enable delete button
		$( '#' + field_id + "Delete").on( "click", function() {
			if( confirm( 'Are you sure to delete this file?' ) ) { 
				that.deleteFile( field_id ); 
			} 
			return false;
		});
		
		return upload;
	},
	
	deleteFile: function(field_id) {
		$('#' + field_id).val('*deleted*');
		$('#' + field_id + 'Example').html('File deleted');
		$('#' + field_id + 'Delete').hide();
	},

	createFileHTML: function(filename, truncate) {
		// See whether the filename should be truncated to a certain number of characters
		var label = filename;
		if( typeof(truncate) != 'undefined' && filename.length > truncate ) {
			label = filename.substring(0, truncate) + "...";
		}
		
		return '<a target="_blank" href="' + baseUrl + '/file/get/' + filename + '">' + label + '</a>';
	}
};
