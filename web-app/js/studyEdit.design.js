if( typeof( StudyEdit ) === "undefined" ) { 
	StudyEdit = {};
};

StudyEdit.design = {
	timelineObject: null,
	initialize: function( data, studyStartDate ) {
		StudyEdit.design.timelineObject = StudyEdit.design.editableTimeline( 
			"#timeline-eventgroups", 
			data, 
			{ 
				dragAreaWidth: 0, 
				dragFrom: "#eventgroups",
				t0: studyStartDate
			},
			StudyEdit.design.subjectEventGroups.eventListeners
		);
		StudyEdit.design.eventGroups.initialize( studyStartDate );
	},
	
	editableTimeline: function( container, data, options, eventHandlers ) {
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
	    links.events.addListener(timeline, 'dragin', eventHandlers.add );
	    links.events.addListener(timeline, 'change', eventHandlers.change);
	    links.events.addListener(timeline, 'delete', eventHandlers.delete);
	    
	    // Prevent adding new items by doubleclick
	    links.events.addListener(timeline, 'add', function() { timeline.cancelAdd(); } );
	    
	    return timeline;
	},


};

StudyEdit.design.subjectEventGroups = {
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
	},
	eventListeners: {
	    add: function() { 
	    	var timeline = StudyEdit.design.timelineObject;
	    	
	    	var selectedRow = timeline.getSelectedRow();
	    	var selectedIndex = timeline.getSelectedIndex();
	    	var eventGroupId = timeline.getIdFromClassName( "dragged-origin-id-", selectedRow );
	    	var studyId = $( "#design" ).find( "#id" ).val();
	    	
	    	StudyEdit.design.subjectEventGroups.add( studyId, selectedRow.start, selectedRow.group, eventGroupId, function( element ) {
	    		timeline.updateData( selectedIndex, { data: { 
	    			id: element.id, 
	    			group: element.subjectGroup, 
	    			hasSamples: false, 
	    			eventGroupId: element.eventGroupId, 
	    			subjectgroupId: element.subjectGroupid 
	    		} } );
	    	});
	    },
	    
	    change: function() {
	    	var timeline = StudyEdit.design.timelineObject;
	    	
	    	var selectedRow = timeline.getSelectedRow();
	    	var selectedIndex = timeline.getSelectedIndex();
	    	
	    	if( selectedRow.data ) {
		    	var id = selectedRow.data.id;
		    	var originalGroup = selectedRow.data.group;
		    	var groupChanged = originalGroup != selectedRow.group;
		    	var hasSamples = selectedRow.data.hasSamples;
		    	
		    	var changed = StudyEdit.design.subjectEventGroups.update( id, selectedRow.start, selectedRow.group, originalGroup, hasSamples, function(element) {
		    		var newData = {
		    			id: id,
		    			group: selectedRow.group,
		    			hasSamples: ( groupChanged ? false : hasSamples ),
		    			eventGroupId: element.eventGroupId, 
		    			subjectgroupId: element.subjectGroupid 
		    		}
				    		
			    	timeline.updateData( selectedIndex, { data: newData } );
		    		
		    	} );
		    	
		    	// The change can be cancelled if the user chooses to, or an error occurs. In that case, the original state must be restored
		    	if( !changed ) {
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
	    },
	    
	    delete: function() {
	    	var timeline = StudyEdit.design.timelineObject;
	    	var selectedRow = timeline.getSelectedRow();

	    	if( selectedRow.data ) {
		    	var id = selectedRow.data.id;
		    	var hasSamples = selectedRow.data.hasSamples;
		    	
		    	// Delete the SubjectEventGroup itself. It if fails, also don't delete it from the timeline 
		    	if( !StudyEdit.design.subjectEventGroups.delete( id, hasSamples ) ) {
		    		timeline.cancelDelete();
		    	}
	    	} else {
	    		timeline.cancelDelete();
	    	}
	    }
	}
}