if( typeof( StudyEdit ) === "undefined" ) { 
	StudyEdit = {};
};

StudyEdit.design = {
	timeline: null,
	initialize: function( data, studyStartDate ) {
		StudyEdit.design.timeline = StudyEdit.design.editableTimeline( 
			"#timeline-eventgroups", 
			data, 
			{ 
				dragAreaWidth: 0, 
				dragFrom: "#eventgroups",
				t0: studyStartDate
			}
		);
		StudyEdit.eventGroups.initialize( studyStartDate );
	},
	
	editableTimeline: function( container, data, options ) {
		if( typeof( options ) === "undefined" )
			options = {}
		
		var dragFrom = false;
		if( options.dragFrom ) {
			dragFrom = options.dragFrom;
			delete options.dragFrom;
		}
		
		options = $.extend( options, { 
			editable: true, 
			groupsChangeable: true,
			showCurrentTime: false,
			showCustomTime: false,
			droppable: ( dragFrom ? true : false )
		} );
	    var timeline = new dbnp.study.Timeline( $(container).get(0) );
	    timeline.draw( data, options );
    	
	    if( dragFrom ) {
		    $( dragFrom ).find( "li:not(.add)" ).draggable({ helper: 'clone' });
	    }
	    
	    // Add event listeners
	    links.events.addListener(timeline, 'add', function() {
	    	var selectedRow = StudyEdit.design.getSelectedRow( timeline );
	    	var selectedIndex = StudyEdit.design.getSelectedIndex( timeline );
	    	var eventGroupId = StudyEdit.design.getIdFromClassName( "dragged-origin-id-", selectedRow );
	    	var studyId = $( "#design" ).find( "#id" ).val();
	    	StudyEdit.subjectEventGroups.add( studyId, selectedRow.start, selectedRow.group, eventGroupId, function( element ) {
	    		timeline.updateData( selectedIndex, { data: { id: element.id, group: element.subjectGroup, hasSamples: false } } );
	    	});
	    });
	    
	    links.events.addListener(timeline, 'change', function() {
	    	var selectedRow = StudyEdit.design.getSelectedRow( timeline );
	    	var selectedIndex = StudyEdit.design.getSelectedIndex( timeline );
	    	
	    	console.log( "Start update: ", selectedRow );
	    	
	    	if( selectedRow.data ) {
		    	var id = selectedRow.data.id;
		    	var originalGroup = selectedRow.data.group;
		    	var groupChanged = originalGroup != selectedRow.group;
		    	var hasSamples = selectedRow.data.hasSamples;
		    	
		    	var changed = StudyEdit.subjectEventGroups.update( id, selectedRow.start, selectedRow.group, originalGroup, hasSamples );
		    	
		    	// The change can be cancelled if the user chooses to, or an error occurs. In that case, the original state must be restored
		    	if( changed ) {
		    		var newData = {
		    			id: id,
		    			group: selectedRow.group,
		    			hasSamples: ( groupChanged ? false : hasSamples )
		    		}
			    		
		    		timeline.updateData( selectedIndex, { data: newData } );
		    	} else {
		    		timeline.cancelChange();
		    		// Restore original group, since the timeline script doesn't do that properly
		    		timeline.updateData( selectedIndex, { group: originalGroup } );
		    	}
	    	} else {
	    		console.log( "Invalid data from timeline: ", selectedRow );
	    		timeline.cancelChange();
	    		// Restore original group
	    		timeline.updateData( selectedIndex, { group: originalGroup } );
	    	}
	    });
	    
	    links.events.addListener(timeline, 'delete', function() {
	    	var selectedRow = StudyEdit.design.getSelectedRow( timeline );

	    	if( selectedRow.data ) {
		    	var id = selectedRow.data.id;
		    	var hasSamples = selectedRow.data.hasSamples;
		    	
		    	// Delete the SubjectEventGroup itself. It if fails, also don't delete it from the timeline 
		    	if( !StudyEdit.subjectEventGroups.delete( id, hasSamples ) ) {
		    		timeline.cancelDelete();
		    	}
	    	} else {
	    		timeline.cancelDelete();
	    	}
	    });
	    
	    
	    return timeline;
	},
	
	getIdFromClassName: function( idPrefix, item ) {
    	// The IDs are retrieved from the className, because there is currently no 
    	// possibility to store extra data in the events
    	var regex = new RegExp( idPrefix + "([0-9]+)" )
    	var result = regex.exec( item.className );
		
    	if( result ) {
    		return result[ 1 ];
    	} else {
    		return null;
    	}
	},
	
	getSelectedRow: function( timeline ) {
    	var index = StudyEdit.design.getSelectedIndex( timeline );
    	
    	// We don't use the getItem method for now, since that doesn't return the className properly
    	// However, in future versions this will be fixed (see https://github.com/almende/chap-links-library/commit/344718605c44c2f42ab44fb1f398a43247d4bde2)
    	// and then 
    	//		timeline.getItem( index )
    	// is the prefered way to go.
    	var data = timeline.getData();
    	return data[ index ];
	},
	
	getSelectedIndex: function( timeline ) {
    	var selection= timeline.getSelection();
    	
    	if( !selection )
    		return null;
    	
    	return selection[0].row;
	}

};

StudyEdit.eventGroups = {
	timeline: null,
	add: function() {
		var dialog = $( '#eventGroupDialog' );
		dialog.dialog( "option", "title", "Add eventgroup" );
		
		StudyEdit.eventGroups.openDialog();

		// Create a timeline, if it doesn't exist
		StudyEdit.eventGroups.createEmptyTimeline( dialog.data( "studyStartDate" ) );
	},
	save: function() {},
	edit: function( id, dataUrl ) {
		var dialog = $( '#eventGroupDialog' );
		dialog.dialog( "option", "title", "Edit eventgroup" );
		
		StudyEdit.eventGroups.openDialog();

		// Create a timeline, if it doesn't exist
		StudyEdit.eventGroups.createEmptyTimeline( dialog.data( "studyStartDate" ) );
		
		// Load the data for the timeline
		$.get( dataUrl, function( data ) {
			var convertedData = $.map( data.events, function( event, idx ) { 
				event.start = new Date( event.start );
				
				if( event.end ) {
					event.end = new Date( event.end );
				}
				
				return event;
			} );
			StudyEdit.eventGroups.timeline.setData( convertedData );
			
			StudyEdit.eventGroups.timeline.setVisibleChartRange( new Date( data.start ), new Date( data.end ), true );
		});
	},
	update: function() {},
	delete: function( id ) {},
	
	initialize: function( studyStartDate ) {
		// Create a dialog to add or edit event groups
		$( '#eventGroupDialog' ).dialog( { 
			modal: true, 
			autoOpen: false,
			width: 900,
			buttons: {
				Ok: function() {
					StudyEdit.eventGroups.save();
					StudyEdit.eventGroups.closeDialog();
				},
				Cancel: function() {
					StudyEdit.eventGroups.closeDialog();
				}
			},
			
			// Disable the main timeline when opening dialog, to prevent dropping events on the main timeline
			// See http://stackoverflow.com/questions/11997053/jqueryui-droppable-stop-propagation-to-overlapped-sibling
			open: function( event, ui ) {
				$( "#timeline-eventgroups" ).droppable( "disable" );
			},
			
			// Enable the main timeline again when closing dialog
			// See http://stackoverflow.com/questions/11997053/jqueryui-droppable-stop-propagation-to-overlapped-sibling
			close: function( event, ui ) {
				$( "#timeline-eventgroups" ).droppable( "enable" );
			},

		}).data( "studyStartDate", studyStartDate );
		
		// Enable buttons on existing event groups
		$(document).on( "click", "#eventgroups .edit", function() {
			var li = $(this).closest( "li" );
			StudyEdit.eventGroups.edit( li.data( "origin-id" ), li.data( "url" ) ); return false; 
		} );
		$(document).on( "click", "#eventgroups .delete", function() {} );
	},
	
	createEmptyTimeline: function( studyStartDate ) {
		var dialog = $( '#eventGroupDialog' );
		
		if( $( '.timeline-frame', dialog ).length == 0 ) {
			StudyEdit.eventGroups.timeline = StudyEdit.design.editableTimeline( "#timeline-events", [], { 
				dragFrom: "#events, #sampling_events",
				t0: studyStartDate
			} );
		} else {
			StudyEdit.eventGroups.timeline.clearItems();
			StudyEdit.eventGroups.timeline.repaint();
		}
	},
	
	openDialog: function() {
		// Open the dialog
		$( '#eventGroupDialog' ).dialog( 'open' );
	},
	
	closeDialog: function() {
		// Close the dialog
		$( '#eventGroupDialog' ).dialog( 'close' );
	},
};

StudyEdit.events = {
	add: function() {},
	edit: function( id ) {},
	update: function( id, item ) {
		
	},
	delete: function( id ) {}
};

StudyEdit.samplingEvents = {
	add: function() {},
	edit: function( id ) {},
	save: function() {},
	delete: function( id ) {}
};

StudyEdit.subjectEventGroups = {
	add: function( studyId, start, group, eventGroupId, afterAdd ) {
		var url = $( 'form#subjectEventGroup' ).attr( 'action' ) + "Add";
		var data = {
			id: studyId,
			start: start.getTime(),
			subjectGroup: group,
			eventGroupId: eventGroupId
		};
		$.post( url, data, function( returnData ) {
			if( typeof( afterAdd ) != "undefined" ) {
				afterAdd( returnData );
			}
		});
	},
	update: function( id, start, group, originalGroup, hasSamples, afterChange ) {
		var url = $( 'form#subjectEventGroup' ).attr( 'action' ) + "Update";
		var data = { 
			id: id, 
			start: start.getTime(),
			subjectGroup: group
		};
		
		var doUpdate = true;
		
		if( hasSamples && originalGroup != group ) {
			doUpdate = confirm( "Moving this eventgroup to another subjectgroup will delete all samples that originated from it. Are you sure you want to move the eventgroup?" );
		}
		
		if( doUpdate ) {
			$.post( url, data, function( returnData ) {
				if( typeof( afterChange ) != "undefined" ) {
					afterChange( returnData );
				}
			});
		}
		
		return doUpdate;
	},
	delete: function( id, hasSamples ) {
		var url = $( 'form#subjectEventGroup' ).attr( 'action' ) + "Delete";
		var data = { 
			id: id
		};
		
		if( !hasSamples || confirm( "Deleting this eventgroup will also delete all samples that originated from it. Are you sure you want to delete the eventgroup?" ) ) {
			$.post( url, data, function() {
				console.log( "SubjectEventgroup deleted" );
			});
			
			return true;
		} else {
			return false;
		}
	}
}