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
		StudyEdit.eventGroups.initialize();
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
	    	StudyEdit.subjectEventGroups.add( studyId, selectedRow.start, selectedRow.group, eventGroupId, function( id ) {
	    		timeline.updateData( selectedIndex, { className: "eventgroup-id-" + id } );
	    	});
	    });
	    
	    links.events.addListener(timeline, 'change', function() {
	    	var selectedRow = StudyEdit.design.getSelectedRow( timeline );
	    	var id = StudyEdit.design.getIdFromClassName( "eventgroup-id-", selectedRow );
	    	StudyEdit.subjectEventGroups.update( id, selectedRow.start, selectedRow.group );
	    });
	    
	    links.events.addListener(timeline, 'delete', function() {
	    	var selectedRow = StudyEdit.design.getSelectedRow( timeline );
	    	var id = StudyEdit.design.getIdFromClassName( "eventgroup-id-", selectedRow );
	    	StudyEdit.subjectEventGroups.delete( id );
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
		if( $( '.timeline-frame', dialog ).length == 0 ) {
			StudyEdit.eventGroups.timeline = StudyEdit.design.editableTimeline( "#timeline-events", [], { dragFrom: "#events, #sampling_events" } );
		} else {
			StudyEdit.eventGroups.timeline.clearItems();
			StudyEdit.eventGroups.timeline.repaint();
		}
	},
	edit: function( id ) {},
	save: function() {},
	delete: function( id ) {},
	
	initialize: function() {
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

		} );
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
				afterAdd( returnData.id );
			}
		});
	},
	update: function( id, start, group ) {
		var url = $( 'form#subjectEventGroup' ).attr( 'action' ) + "Update";
		var data = { 
			id: id, 
			start: start.getTime(),
			subjectGroup: group
		};
		$.post( url, data, function() {
			console.log( "SubjectEventgroup updated" );
		});
	},
	delete: function( id ) {
		var url = $( 'form#subjectEventGroup' ).attr( 'action' ) + "Delete";
		var data = { 
			id: id
		};
		$.post( url, data, function() {
			console.log( "SubjectEventgroup deleted" );
		});
	}
}