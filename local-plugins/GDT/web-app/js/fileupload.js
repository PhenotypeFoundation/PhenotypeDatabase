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
			$('#' + field_id + 'Example').html('Uploading ' + createFileHTML(file));
			$('#' + field_id + 'Delete').hide();

		},
		onComplete : function(file, response) {
            //If there is HTML in the response, just retrieve the text value.
            if(response.indexOf("<") != -1) {
                response = $(response).text().split("//<![CDATA[")[0];
            }
			if (response == "") {
				$('#' + field_id).val('');
				$('#' + field_id + 'Example').html('<span class="error">Error uploading ' + createFileHTML(file) + '</span>');
				$('#' + field_id + 'Delete').hide();
			} else {
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