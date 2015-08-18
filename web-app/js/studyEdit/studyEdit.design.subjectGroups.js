if( typeof( StudyEdit ) === "undefined" ) { 
	StudyEdit = {};
};

StudyEdit.design.subjectGroups = {
	groups: {
		data: [],
		
		findIndexById: function( id ) {
			for( i in StudyEdit.design.subjectGroups.groups.data ) {
				if( StudyEdit.design.subjectGroups.groups.data[ i ].id == id )
					return i;
			}
			
			return -1;
		},
		findIndexByName: function( name ) {
			for( i in StudyEdit.design.subjectGroups.groups.data ) {
				if( StudyEdit.design.subjectGroups.groups.data[ i ].name == name )
					return i;
			}
			
			return -1;
		},
		findByName: function( name ) {
			var i = StudyEdit.design.subjectGroups.groups.findIndexByName( name );
			
			if( i > -1 ) {
				return StudyEdit.design.subjectGroups.groups.data[ i ];
			} else {
				return null;
			}
		},
		findById: function( id ) {
			var i = StudyEdit.design.subjectGroups.groups.findIndexById( id );
			
			if( i > -1 ) {
				return StudyEdit.design.subjectGroups.groups.data[ i ];
			} else {
				return null;
			}
		},
		
		size: function() {
			return StudyEdit.design.subjectGroups.groups.data.length;
		},
		
		add: function( group ) {
			StudyEdit.design.subjectGroups.groups.data.push( { 
				id: group.id, 
				name: group.name 
			});
		},

		update: function( group ) {
			// Update the internal list of subject groups
			var i = StudyEdit.design.subjectGroups.groups.findIndexById( group.id );
			
			if( i < -1 ) {
				console.log( "Error finding group with id " + group.id );
				return;
			}
			
			var currentName = group.name;
			StudyEdit.design.subjectGroups.groups.data[ i ].name = group.name;
			
			// Update the timeline and eventgroups on the timeline 
			var timelineData = StudyEdit.design.timelineObject.getData();
			for( j in timelineData ) {
				if( timelineData[ j ].data.subjectGroupId == group.id ) {
					timelineData[ j ].group = group.name;
					timelineData[ j ].data.group = group.name
				}
			}
			
			StudyEdit.design.timelineObject.setData( timelineData );
		},
		
		deleteItem: function( id ) {
			// Update the internal list of subject groups
			var i = StudyEdit.design.subjectGroups.groups.findIndexById( id );
			
			if( i < -1 ) {
				console.log( "Error finding group with id " + id );
				return;
			}
			
			// Remove the item from our local storage
			StudyEdit.design.subjectGroups.groups.data.splice( i, 1 );
			
			// Update the timeline and eventgroups on the timeline 
			var timelineData = StudyEdit.design.timelineObject.getData();
			var dataKept = [];
			for( j in timelineData ) {
				if( timelineData[ j ].data.subjectGroupId != id ) {
					dataKept.push( timelineData[ j ] );
				}
			}
			
			StudyEdit.design.timelineObject.setData( dataKept );
		},		
	},
	dialog: {
		get: function() {
			return $( '#subjectGroupDialog' );
		},
		open: function() {
			// open the dialog
			var dialog = StudyEdit.design.subjectGroups.dialog;
			
			dialog.get().dialog( 'open' );
			
			// Initialize the datatable only after opening the dialog
			// to make sure column width are correct
			StudyEdit.design.subjectGroups.dialog.dataTable.initialize();
		},
		close: function() {
			// Close the dialog
			StudyEdit.design.subjectGroups.dialog.get().dialog( 'close' );
		},
		
		create: function() {
			StudyEdit.design.subjectGroups.dialog.get().dialog( { 
				modal: true, 
				autoOpen: false,
				width: 900,
				close: function(event,ui) {
					// Clear the datatable
					StudyEdit.design.subjectGroups.dialog.dataTable.destroy();
					
				}
			});
		},
		
		addButtons: function( ok, del, cancel ) {
			var buttons = {};
			
			if( ok ) {
				buttons.Ok =  function() {
					StudyEdit.design.subjectGroups.save();
					StudyEdit.design.subjectGroups.dialog.close( this );
				};
			}
			
			if( del ) {
				buttons.Delete = function() {
					var dialog = StudyEdit.design.subjectGroups.dialog.get();
					var id = dialog.data( "subjectgroup-id" );
					StudyEdit.design.subjectGroups.deleteItem( id );
					StudyEdit.design.subjectGroups.dialog.close( this );
				}
			}
			
			if( cancel ) {
				buttons.Cancel = function() {
					StudyEdit.design.subjectGroups.dialog.close();
				}
			}

			StudyEdit.design.subjectGroups.dialog.get().dialog( "option", "buttons", buttons );
		},
		
		dataTable: {
			getId: function() { 
				return 'selectSubjectsTable'; 
			},
			get: function() {
				return StudyEdit.design.subjectGroups.dialog.get().find( "#" + this.getId() ).dataTable();
			},
			initialize: function() {
				return StudyEdit.datatables.initialize( "#" + this.getId() );
			},
			destroy: function() {
				// Clear any references on selection
				StudyEdit.datatables.destroy( this.getId() );
				
				// Clear the datatable itself
				this.get().fnDestroy();
			}
		}
	},
	
	initialize: function() {
		// Create a dialog to add or edit subject groups
		StudyEdit.design.subjectGroups.dialog.create();
		
		// Enable doubleclick edit
		$( "#timeline-eventgroups" ).on( "dblclick", ".timeline-groups-text", function( e ) {
			StudyEdit.design.subjectGroups.edit( $(e.target).text() );
		});
		
		// Update overlay
		StudyEdit.design.subjectGroups.updateOverlay();
		
		// Enable selectable behaviour on subjects
		$( "#design-subjects" ).selectable({
			filter: ".subject",
		    stop: function() {        
		        $(".ui-selected input", this).each(function() {
		            this.checked= !this.checked
		        });
		    }
		});
	},
	
	/**
	 * Updates the overlay visibility, based on the number of subject groups
	 */
	updateOverlay: function() {
		// Add overlay if no subjectgroups are defined
		if( StudyEdit.design.subjectGroups.groups.size() == 0 ) {
			$( "#studydesign .overlay" ).show();
		} else {
			$( "#studydesign .overlay" ).hide();
		}
	},
	
	/**
	 * Shows a for to add a new subjectgroup
	 */
	add: function() {
		var dialog = StudyEdit.design.subjectGroups.dialog.get();
		dialog.dialog( "option", "title", "Add subjectgroup" );
		dialog.data( "subjectgroup-id", null );
		StudyEdit.design.subjectGroups.dialog.addButtons( true, false, true );
		StudyEdit.design.subjectGroups.dialog.open()
		
		// Clear the name box and subject checkboxes
		$( '[name=subjectgroup-name]' ).val( "" );
		
		// Selection is initially empty, as the datatable is refreshed
	},
	
	edit: function( groupName ) {
		var subjectGroup = StudyEdit.design.subjectGroups.groups.findByName( groupName );
		if( !subjectGroup )
			return;
		
		var dialog = StudyEdit.design.subjectGroups.dialog.get();
		dialog.dialog( "option", "title", "Edit subjectgroup" );
		dialog.data( "subjectgroup-id", subjectGroup.id );

		StudyEdit.design.subjectGroups.dialog.addButtons( true, true, true );
		StudyEdit.design.subjectGroups.dialog.open();

		// Load the data for the timeline
		var tableId = StudyEdit.design.subjectGroups.dialog.dataTable.getId();
		var loadingSelection = $( "#" + tableId ).parents( ".dataTables_wrapper" ).find( ".loadingSelection");
		loadingSelection.slideDown(100);
		
		$.get( StudyEdit.design.subjectGroups.getDataUrl( subjectGroup ), function( data ) {
			// Put data into the dialog
			$( '[name=subjectgroup-name]' ).val( data.name );
				
			// Check the right checkboxes
			$.each( data.subjects, function( idx, el ) {
				StudyEdit.datatables.selection.select( tableId, el.id, true );
				$( "#selectSubjectsTable_ids_" + el.id ).attr( "checked", true );
			} );
			
			// Notify the user of having a selection
			StudyEdit.datatables.selection.updateCheckAll($('#' + tableId));
			StudyEdit.datatables.selection.updateLabel(tableId);
			
			// Remove loading message
			loadingSelection.slideUp(100);
		});
	},
	
	/**
	 * Saves the data entered in the eventgroup dialog
	 */
	save: function() {
		var dialog = StudyEdit.design.subjectGroups.dialog.get();
		var id = dialog.data( "subjectgroup-id" );
		var action = ( id ? "Update" : "Add" );
		var url = $( 'form#subjectGroup' ).attr( 'action' ) + action;
				
		var subjectIds = StudyEdit.datatables.selection.getSelectedIds( StudyEdit.design.subjectGroups.dialog.dataTable.getId() );
		
		var data = { 
			name: $( '[name=subjectgroup-name]' ).val(),
			
			// Create a string of subjectIds, as tomcat has a maximum number of
			// parameters being sent to the server (defaults to 10000). 
			subjects: subjectIds ? subjectIds.join(",") : "" 
		};
		
		if( id ) {
			// On update, the subjectGroupId should be sent
			data.id = id;
		} else {
			// On add, the studyId must be put in place
			data.id = $( "#design" ).find( "#id" ).val();
		}
		
		// Start spinner for the user to wait
		StudyEdit.spinner.show( "Please wait while saving your subjectgroup. For many subjects, this may take up to a minute.");

		$.post( url, data )
			.done( function( returnData ) {
				if( id ) {
					// Update existing eventgroup
					StudyEdit.design.subjectGroups.groups.update( data );
				} else {
					// Add new eventgroup
					StudyEdit.design.subjectGroups.groups.add( returnData );
				}
				
				// Update timeline and overlay
				StudyEdit.design.subjectGroups.updateTimeline();
			})
			.fail(function() {
				alert( "Failure in saving subject group data. Please try again." );
			})
			.always(function() {
				StudyEdit.spinner.hide();
			})
		
		return true;
	},

	deleteItem: function( id ) {
		var url = $( 'form#subjectGroup' ).attr( 'action' ) + "Delete";
		var data = { 
			id: id
		};
		
		if( confirm( "Deleting this subjectgroup will also delete all samples that originated from it, and remove all treatment&sample groups in the subjectgroup. Are you sure you want to delete the subjectgroup?" ) ) {
			$.post( url, data, function() {
			});
			
			StudyEdit.design.subjectGroups.groups.deleteItem( id );
			StudyEdit.design.subjectGroups.updateTimeline();
			
			return true;
		} else {
			return false;
		}
	},

	updateTimeline: function() {
		StudyEdit.design.subjectGroups.updateOverlay();
		
		if( StudyEdit.design.subjectGroups.groups.size() > 0 ) {
			StudyEdit.design.timelineObject.redraw();
		}
	},
	
	getDataUrl: function( group ) {
		return $( '#subjectGroup' ).attr( "action" ) + "Details/" + group.id;
	}
};
