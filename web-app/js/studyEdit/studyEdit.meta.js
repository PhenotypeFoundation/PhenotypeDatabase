if( typeof( StudyEdit ) === "undefined" ) { 
	StudyEdit = {};
}

StudyEdit.dialogs = {
	/**
	 * Finds a button in a jquery dialog by name
	 */
	getButton: function(dialog_selector, button_name) {
		var buttons = $(dialog_selector + ' .ui-dialog-buttonpane button');
		for (var i = 0; i < buttons.length; ++i) {
			var jButton = $(buttons[i]);
			if (jButton.text() == button_name) {
				return jButton;
			}
		}

		return null;
	},

	/**
	 * Enables or disables a button in a selected dialog
	 */
	enableButton: function(dialog_selector, button_name, enable) {
		var dlgButton = StudyEdit.dialogs.getButton(dialog_selector, button_name);

		if (dlgButton) {
			if (enable) {
				dlgButton.attr('disabled', false);
				dlgButton.removeClass('ui-state-disabled');
			} else {
				dlgButton.attr('disabled', true);
				dlgButton.addClass('ui-state-disabled');
			}
		}
	}
}

/*************************************************
 *
 * Functions for adding publications to the study
 *
 ************************************************/

StudyEdit.publications = {

	/**
	 * Adds a selected publication to the study using javascript
	 * N.B. The publication must be added in grails when the form is submitted
	 */
	add: function(element_id) {
		/* Find publication ID and add to form */
		jQuery.ajax({
			type:"GET",
			url: baseUrl + "/publication/getID?" + $("#" + element_id + "_form").serialize(),
			success: function(data, textStatus) {
				var id = parseInt(data);
	
				// Put the ID in the array, but only if it does not yet exist
				var ids = StudyEdit.publications.getPublicationIds(element_id);
	
				if ($.inArray(id, ids) == -1) {
					ids[ ids.length ] = id;
					$('#' + element_id + '_ids').val(ids.join(','));
	
					// Show the title and a remove button
					StudyEdit.publications.show(element_id, id, $("#" + element_id + "_form").find('[name=publication-title]').val(), $("#" + element_id + "_form").find('[name=publication-authorsList]').val(), ids.length - 1);
	
					// Hide the 'none box'
					$('#' + element_id + '_none').hide();
				}
			},
			error:function(XMLHttpRequest, textStatus, errorThrown) {
				alert("Publication could not be added.")
			}
		});
		return false;
	},

	/**
	 * Removes a publication from the study using javascript
	 * N.B. The deletion must be handled in grails when the form is submitted
	 */
	remove: function(element_id, id) {
		var ids = StudyEdit.publications.getPublicationIds(element_id);
		if ($.inArray(id, ids) != -1) {
			// Remove the ID
			ids.splice($.inArray(id, ids), 1);
			$('#' + element_id + '_ids').val(ids.join(','));
	
			// Remove the title from the list
			var li = $("#" + element_id + '_item_' + id);
			if (li) {
				li.remove();
			}
	
			// Show the 'none box' if needed
			if (ids.length == 0) {
				$('#' + element_id + '_none').show();
			}
	
		}
	},
	
	/**
	 * Returns an array of publications IDs currently attached to the study
	 * The array contains integers
	 */
	getPublicationIds: function(element_id) {
		var ids = $('#' + element_id + '_ids').val();
		if (ids == "") {
			return new Array();
		} else {
			ids_array = ids.split(',');
			for (var i = 0; i < ids_array.length; i++) {
				ids_array[ i ] = parseInt(ids_array[ i ]);
			}
	
			return ids_array;
		}
	},
	
	/**
	 * Shows a publication on the screen
	 */
	show: function(element_id, id, title, authors, nr) {
		var deletebutton = document.createElement('img');
		deletebutton.className = 'famfamfam delete_button';
		deletebutton.setAttribute('alt', 'remove this publication');
		deletebutton.setAttribute('src', baseUrl + '/plugins/famfamfam-1.0.1/images/icons/delete.png');
		deletebutton.onclick = function() {
			StudyEdit.publications.remove(element_id, id);
			return false;
		};
	
		var titleDiv = document.createElement('div');
		titleDiv.className = 'title';
		titleDiv.appendChild(document.createTextNode(title));
	
		var authorsDiv = document.createElement('div');
		authorsDiv.className = 'authors';
		authorsDiv.appendChild(document.createTextNode(authors));
	
		var li = document.createElement('li');
		li.setAttribute('id', element_id + '_item_' + id);
		li.className = nr % 2 == 0 ? 'even' : 'odd';
		li.appendChild(deletebutton);
		li.appendChild(titleDiv);
		li.appendChild(authorsDiv);
	
		$('#' + element_id + '_list').append(li);
	},
	
	/**
	 * Creates the dialog for searching a publication
	 */
	createPublicationDialog: function(element_id) {
		/* Because of the AJAX loading of this page, the dialog will be created
		 * again, when the page is reloaded. This raises problems when reading the
		 * values of the selected publication. For that reason we check whether the
		 * dialog already exists
		 */
		if ($("." + element_id + "_publication_dialog").length == 0) {
			$("#" + element_id + "_dialog").dialog({
				title   : "Add publication",
				autoOpen: false,
				width   : 800,
				height  : 400,
				modal   : true,
				dialogClass : element_id + "_publication_dialog",
				position: "center",
				buttons : {
					Add  : function() {
						StudyEdit.publications.add(element_id);
						$(this).dialog("close");
					},
					Close  : function() {
						$(this).dialog("close");
					}
				},
				close   : function() {
					/* closeFunc(this); */
				}
			}).width(790).height(400);
		} else {
			/* If a dialog already exists, remove the new div */
			$("#" + element_id + "_dialog").remove();
		}
	},
	
	/**
	 * Opens the dialog for searching a publication
	 */
	openPublicationDialog: function(element_id) {
		// Empty input field
		var field = $('#' + element_id);
		field.autocomplete('close');
		field.val('');
	
		// Show the dialog
		$('#' + element_id + '_dialog').dialog('open');
		field.focus();
	
		// Disable 'Add' button
		StudyEdit.dialogs.enableButton('.' + element_id + '_publication_dialog', 'Add', false);
	}
}


/*************************************************
 *
 * Functions for adding contacts to the study
 *
 ************************************************/

StudyEdit.contacts = {
	/**
	 * Adds a selected contact to the study using javascript
	 * N.B. The contact must be added in grails when the form is submitted
	 */
	add: function(element_id) {
		// FInd person and role IDs
		var person_id = $('#' + element_id + '_person').val();
		var role_id = $('#' + element_id + '_role').val();
	
		if (person_id == "" || person_id == 0 || role_id == "" || role_id == 0) {
			alert("Please select both a person and a role.");
			return false;
		}
	
		var combination = person_id + '-' + role_id;
	
		// Put the ID in the array, but only if it does not yet exist
		var ids = StudyEdit.contacts.getContactIds(element_id);
		if ($.inArray(combination, ids) == -1) {
			ids[ ids.length ] = combination;
			$('#' + element_id + '_ids').val(ids.join(','));
	
			// Show the title and a remove button
			StudyEdit.contacts.show(element_id, combination, $("#" + element_id + "_person  :selected").text(), $("#" + element_id + "_role :selected").text(), ids.length - 1);
	
			// Hide the 'none box'
			$('#' + element_id + '_none').hide();
		}
	
		return true;
	},
	
	/**
	 * Removes a contact from the study using javascript
	 * N.B. The deletion must be handled in grails when the form is submitted
	 */
	remove: function(element_id, combination) {
		var ids = StudyEdit.contacts.getContactIds(element_id);
		if ($.inArray(combination, ids) != -1) {
			// Remove the ID
			ids.splice($.inArray(combination, ids), 1);
			$('#' + element_id + '_ids').val(ids.join(','));
	
			// Remove the title from the list
			var li = $("#" + element_id + '_item_' + combination);
			if (li) {
				li.remove();
			}
	
			// Show the 'none box' if needed
			if (ids.length == 0) {
				$('#' + element_id + '_none').show();
			}
	
		}
	},
	
	/**
	 * Returns an array of studyperson IDs currently attached to the study.
	 * The array contains string formatted like '[person_id]-[role_id]'
	 */
	getContactIds: function(element_id) {
		var ids = $('#' + element_id + '_ids').val();
		if (ids == "") {
			return new Array();
		} else {
			ids_array = ids.split(',');
	
			return ids_array;
		}
	},
	
	/**
	 * Shows a contact on the screen
	 */
	show: function(element_id, id, fullName, role, nr) {
		var deletebutton = document.createElement('img');
		deletebutton.className = 'famfamfam delete_button';
		deletebutton.setAttribute('alt', 'remove this person');
		deletebutton.setAttribute('src', baseUrl + '/plugins/famfamfam-1.0.1/images/icons/delete.png');
		deletebutton.onclick = function() {
			StudyEdit.contacts.remove(element_id, id);
			return false;
		};
	
		var titleDiv = document.createElement('div');
		titleDiv.className = 'person';
		titleDiv.appendChild(document.createTextNode(fullName));
	
		var authorsDiv = document.createElement('div');
		authorsDiv.className = 'role';
		authorsDiv.appendChild(document.createTextNode(role));
	
		var li = document.createElement('li');
		li.setAttribute('id', element_id + '_item_' + id);
		li.className = nr % 2 == 0 ? 'even' : 'odd';
		li.appendChild(deletebutton);
		li.appendChild(titleDiv);
		li.appendChild(authorsDiv);
	
		$('#' + element_id + '_list').append(li);
	}
}

/*************************************************
 *
 * Functions for adding users (readers or writers) to the study
 *
 ************************************************/

StudyEdit.users = {
	/**
	 * Adds a user to the study using javascript
	 */
	add: function(element_id) {
		/* Find publication ID and add to form */
		id = parseInt($("#" + element_id + "_form select").val());
	
		// Put the ID in the array, but only if it does not yet exist
		var ids = StudyEdit.users.getUserIds(element_id);
	
		if ($.inArray(id, ids) == -1) {
			ids[ ids.length ] = id;
			$('#' + element_id + '_ids').val(ids.join(','));
	
			// Show the title and a remove button
			StudyEdit.users.show(element_id, id, $("#" + element_id + "_form select option:selected").text(), ids.length - 1);
	
			// Hide the 'none box'
			$('#' + element_id + '_none').css('display', 'none');
		}
	
		return false;
	},
	
	/**
	 * Removes a user from the study using javascript
	 * N.B. The deletion must be handled in grails when the form is submitted
	 */
	remove: function(element_id, id) {
		var ids = StudyEdit.users.getUserIds(element_id);
		if ($.inArray(id, ids) != -1) {
			// Remove the ID
			ids.splice($.inArray(id, ids), 1);
			$('#' + element_id + '_ids').val(ids.join(','));
	
			// Remove the title from the list
			var li = $("#" + element_id + '_item_' + id);
			if (li) {
				li.remove();
			}
	
			// Show the 'none box' if needed
			if (ids.length == 0) {
				$('#' + element_id + '_none').css('display', 'inline');
			}
	
		}
	},
	
	/**
	 * Returns an array of user IDs currently attached to the study
	 * The array contains integers
	 */
	getUserIds: function(element_id) {
		var ids = $('#' + element_id + '_ids').val();
		if (ids == "") {
			return new Array();
		} else {
			ids_array = ids.split(',');
			for (var i = 0; i < ids_array.length; i++) {
				ids_array[ i ] = parseInt(ids_array[ i ]);
			}
	
			return ids_array;
		}
	},
	
	/**
	 * Shows a publication on the screen
	 */
	show: function(element_id, id, username, nr) {
		var deletebutton = document.createElement('img');
		deletebutton.className = 'famfamfam delete_button';
		deletebutton.setAttribute('alt', 'remove this user');
		deletebutton.setAttribute('src', baseUrl + '/plugins/famfamfam-1.0.1/images/icons/delete.png');
		deletebutton.onclick = function() {
			StudyEdit.users.remove(element_id, id);
			return false;
		};
	
		var titleDiv = document.createElement('div');
		titleDiv.className = 'username';
		titleDiv.appendChild(document.createTextNode(username));
	
		var li = document.createElement('li');
		li.setAttribute('id', element_id + '_item_' + id);
		li.className = nr % 2 == 0 ? 'even' : 'odd';
		li.appendChild(deletebutton);
		li.appendChild(titleDiv);
	
		$('#' + element_id + '_list').append(li);
	},
	
	/**
	 * Creates the dialog for searching a publication
	 */
	createUserDialog: function(element_id) {
		/* Because of the AJAX loading of this page, the dialog will be created
		 * again, when the page is reloaded. This raises problems when reading the
		 * values of the selected publication. For that reason we check whether the
		 * dialog already exists
		 */
		if ($("." + element_id + "_user_dialog").length == 0) {
			$("#" + element_id + "_dialog").dialog({
				title   : "Add user",
				autoOpen: false,
				width   : 800,
				height  : 400,
				modal   : true,
				dialogClass : element_id + "_user_dialog",
				position: "center",
				buttons : {
					Add  : function() {
						StudyEdit.users.add(element_id);
						$(this).dialog("close");
					},
					Close  : function() {
						$(this).dialog("close");
					}
				},
				close   : function() {
					/* closeFunc(this); */
				}
			}).width(790).height(400);
		} else {
			/* If a dialog already exists, remove the new div */
			$("#" + element_id + "_dialog").remove();
		}
	},
	
	/**
	 * Opens the dialog for searching a publication
	 */
	openUserDialog: function(element_id) {
		// Empty input field
		var field = $('#' + element_id);
		field.val('');
	
		// Show the dialog
		$('#' + element_id + '_dialog').dialog('open');
		field.focus();
	
		// Disable 'Add' button
		//StudyEdit.dialogs.enableButton( '.' + element_id + '_user_dialog', 'Add', false );
	}
}

/*************************************************
 *
 * Functions for adding userGroups (readerGroups or writerGroups) to the study
 *
 ************************************************/

StudyEdit.userGroups = {
	/**
	 * Adds a userGroup to the study using javascript
	 */
	add: function(element_id) {
		/* Find publication ID and add to form */
		id = parseInt($("#" + element_id + "_form select").val());
	
		// Put the ID in the array, but only if it does not yet exist
		var ids = StudyEdit.userGroups.getUserGroupIds(element_id);
	
		if ($.inArray(id, ids) == -1) {
			ids[ ids.length ] = id;
			$('#' + element_id + '_ids').val(ids.join(','));
	
			// Show the title and a remove button
			StudyEdit.userGroups.show(element_id, id, $("#" + element_id + "_form select option:selected").text(), ids.length - 1);
	
			// Hide the 'none box'
			$('#' + element_id + '_none').css('display', 'none');
		}
	
		return false;
	},
	
	/**
	 * Removes a userGroup from the study using javascript
	 * N.B. The deletion must be handled in grails when the form is submitted
	 */
	remove: function(element_id, id) {
		var ids = StudyEdit.userGroups.getUserGroupIds(element_id);
		if ($.inArray(id, ids) != -1) {
			// Remove the ID
			ids.splice($.inArray(id, ids), 1);
			$('#' + element_id + '_ids').val(ids.join(','));
	
			// Remove the title from the list
			var li = $("#" + element_id + '_item_' + id);
			if (li) {
				li.remove();
			}
	
			// Show the 'none box' if needed
			if (ids.length == 0) {
				$('#' + element_id + '_none').css('display', 'inline');
			}
	
		}
	},
	
	/**
	 * Returns an array of userGroup IDs currently attached to the study
	 * The array contains integers
	 */
	getUserGroupIds: function(element_id) {
		var ids = $('#' + element_id + '_ids').val();
		if (ids == "") {
			return new Array();
		} else {
			ids_array = ids.split(',');
			for (var i = 0; i < ids_array.length; i++) {
				ids_array[ i ] = parseInt(ids_array[ i ]);
			}
	
			return ids_array;
		}
	},
	
	/**
	 * Shows a publication on the screen
	 */
	show: function(element_id, id, groupName, nr) {
		var deletebutton = document.createElement('img');
		deletebutton.className = 'famfamfam delete_button';
		deletebutton.setAttribute('alt', 'remove this userGroup');
		deletebutton.setAttribute('src', baseUrl + '/plugins/famfamfam-1.0.1/images/icons/delete.png');
		deletebutton.onclick = function() {
			StudyEdit.userGroups.remove(element_id, id);
			return false;
		};
	
		var titleDiv = document.createElement('div');
		titleDiv.className = 'groupName';
		titleDiv.appendChild(document.createTextNode(groupName));
	
		var li = document.createElement('li');
		li.setAttribute('id', element_id + '_item_' + id);
		li.className = nr % 2 == 0 ? 'even' : 'odd';
		li.appendChild(deletebutton);
		li.appendChild(titleDiv);
	
		$('#' + element_id + '_list').append(li);
	},
	
	/**
	 * Creates the dialog for searching a publication
	 */
	createUserGroupDialog: function(element_id) {
		/* Because of the AJAX loading of this page, the dialog will be created
		 * again, when the page is reloaded. This raises problems when reading the
		 * values of the selected publication. For that reason we check whether the
		 * dialog already exists
		 */
		if ($("." + element_id + "_userGroup_dialog").length == 0) {
			$("#" + element_id + "_dialog").dialog({
				title   : "Add UserGroup",
				autoOpen: false,
				width   : 800,
				height  : 400,
				modal   : true,
				dialogClass : element_id + "_userGroup_dialog",
				position: "center",
				buttons : {
					Add  : function() {
						StudyEdit.userGroups.add(element_id);
						$(this).dialog("close");
					},
					Close  : function() {
						$(this).dialog("close");
					}
				},
				close   : function() {
					/* closeFunc(this); */
				}
			}).width(790).height(400);
		} else {
			/* If a dialog already exists, remove the new div */
			$("#" + element_id + "_dialog").remove();
		}
	},
	
	/**
	 * Opens the dialog for searching a publication
	 */
	openUserGroupDialog: function(element_id) {
		// Empty input field
		var field = $('#' + element_id);
		field.val('');
	
		// Show the dialog
		$('#' + element_id + '_dialog').dialog('open');
		field.focus();
	
		// Disable 'Add' button
		//StudyEdit.dialogs.enableButton( '.' + element_id + '_user_dialog', 'Add', false );
	}
}