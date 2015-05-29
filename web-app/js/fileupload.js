/*************************************************
 *
 * Functions for file upload fields
 *
 ************************************************/
var FileUpload = {
	// Create a file upload field
	convertFileField: function(field_id, truncate) {
		var that = this;
		
		// Convert the file field
		new AjaxUpload('#upload_button_' + field_id, {
			//action: 'upload.php',
			action: baseUrl + '/file/upload/hoihoi', // I disabled uploads in this example for security reaaons
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
				$('#' + field_id + 'Example').html('Uploading ' + that.createFileHTML(file, truncate));
				$('#' + field_id + 'Delete').hide();

			},
			onComplete : function(file, response) {
	            //If there is HTML in the response, just retrieve the text value.
	            if(response.indexOf("<") != -1) {
	                response = $(response).text().split("//<![CDATA[")[0];
	            }
				if (response == "") {
					$('#' + field_id).val('');
					$('#' + field_id + 'Example').html('<span class="error">Error uploading ' + that.createFileHTML(file, truncate) + '</span>');
					$('#' + field_id + 'Delete').hide();
				} else {
					$('#' + field_id).val(response);
					$('#' + field_id + 'Example').html('Uploaded ' + that.createFileHTML(file, truncate));
					$('#' + field_id + 'Delete').show();
				}
			}
		});
		
		// Enable delete button
		$( '#' + field_id + "Delete").on( "click", function() {
			if( confirm( 'Are you sure to delete this file?' ) ) { 
				that.deleteFile( field_id ); 
			} 
			return false;
		});

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


