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
		
		delete: function( id ) {
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
			StudyEdit.design.subjectGroups.dialog.get().dialog( 'open' );
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
					StudyEdit.design.subjectGroups.delete( id );
					StudyEdit.design.subjectGroups.dialog.close( this );
				}
			}
			
			if( cancel ) {
				buttons.Cancel = function() {
					StudyEdit.design.subjectGroups.dialog.close();
				}
			}

			StudyEdit.design.subjectGroups.dialog.get().dialog( "option", "buttons", buttons );
		} 
	},
	
	initialize: function() {
		// Create a dialog to add or edit subject groups
		StudyEdit.design.subjectGroups.dialog.create();
		
		// Enable doubleclick edit
		$( "#timeline-eventgroups" ).on( "dblclick", ".timeline-groups-text", function( e ) {
			StudyEdit.design.subjectGroups.edit( $(e.target).text() );
		});		
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
		
		// Clear the name box
		$( '[name=subjectgroup-name]' ).val( "" );
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
		$.get( StudyEdit.design.subjectGroups.getDataUrl( subjectGroup ), function( data ) {
			// Clear the name box
			$( '[name=subjectgroup-name]' ).val( data.name );
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
				
		var data = { 
			name: $( '[name=subjectgroup-name]' ).val()
		};
		
		if( id ) {
			// On update, the eventgroupId should be sent
			data.id = id;
		} else {
			// On add, the studyId must be put in place
			data.id = $( "#design" ).find( "#id" ).val();
		}
		
		$.post( url, data, function( returnData ) {
			if( id ) {
				// Update existing eventgroup
				StudyEdit.design.subjectGroups.groups.update( data );
			} else {
				// Add new eventgroup
				StudyEdit.design.subjectGroups.groups.add( returnData );
			}
			
			StudyEdit.design.subjectGroups.updateTimeline();
		});
		
		return true;
	},

	delete: function( id ) {
		var url = $( 'form#subjectGroup' ).attr( 'action' ) + "Delete";
		var data = { 
			id: id
		};
		
		if( confirm( "Deleting this subjectgroup will also delete all samples that originated from it, and remove all eventgroups in the subjectgroup. Are you sure you want to delete the subjectgroup?" ) ) {
			$.post( url, data, function() {
				console.log( "Subjectgroup deleted" );
			});
			
			StudyEdit.design.subjectGroups.groups.delete( id );
			StudyEdit.design.subjectGroups.updateTimeline();
			
			return true;
		} else {
			return false;
		}
	},

	updateTimeline: function() {
		StudyEdit.design.timelineObject.redraw();
	},
	
	getDataUrl: function( group ) {
		return $( '#subjectGroup' ).attr( "action" ) + "Details/" + group.id;
	}
};
