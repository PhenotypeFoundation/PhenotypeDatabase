if( typeof( StudyEdit ) === "undefined" ) { 
	StudyEdit = {};
};

StudyEdit.design.eventGroups = {
	timelineObject: null,
	dialog: {
		get: function() {
			return $( '#eventGroupDialog' );
		},
		open: function() {
			// open the dialog
			StudyEdit.design.eventGroups.dialog.get().dialog( 'open' );
		},
		close: function() {
			// Close the dialog
			StudyEdit.design.eventGroups.dialog.get().dialog( 'close' );
		},
		
		timeline: {
			create: function( studyStartDate ) {
				var dialog = StudyEdit.design.eventGroups.dialog.get();
				
				if( $( '.timeline-frame', dialog ).length == 0 ) {
					StudyEdit.design.eventGroups.timelineObject = StudyEdit.design.editableTimeline( 
						"#timeline-events", 
						[], 
						{ 
							dragFrom: "#events, #sampling_events",
							t0: studyStartDate
						},
						StudyEdit.design.eventGroups.eventListeners 
					);
				} else {
					StudyEdit.design.eventGroups.dialog.timeline.clear();
				}				
			},
			clear: function() {
				StudyEdit.design.eventGroups.timelineObject.clearItems();
				StudyEdit.design.eventGroups.timelineObject.repaint();
			},
			hide: function() {
				var dialog = StudyEdit.design.eventGroups.dialog.get();
				dialog.find( "#timeline-events, #design-meta" ).hide();
			}
		}
	},
	
	/**
	 * Shows a for to add a new eventgroup
	 */
	add: function() {
		var dialog = StudyEdit.design.eventGroups.dialog.get();
		dialog.dialog( "option", "title", "Add eventgroup" );
		StudyEdit.design.eventGroups.dialog.open()

		// Hide the timeline (if it exists), to prevent the user from adding items to a not existing timeline
		StudyEdit.design.eventGroups.dialog.timeline.hide();
	},
	save: function() {},
	edit: function( id, dataUrl ) {
		var dialog = StudyEdit.design.eventGroups.dialog.get();
		dialog.dialog( "option", "title", "Edit eventgroup" );
		dialog.data( "eventgroup-id", id );
		
		StudyEdit.design.eventGroups.dialog.open()

		// Create a timeline, if it doesn't exist
		StudyEdit.design.eventGroups.dialog.timeline.create( dialog.data( "studyStartDate" ) );
		
		// Load the data for the timeline
		$.get( dataUrl, function( data ) {
			var convertedData = $.map( data.events, function( event, idx ) { 
				event.start = new Date( event.start );
				
				if( event.end ) {
					event.end = new Date( event.end );
				}
				
				return event;
			} );
			StudyEdit.design.eventGroups.timelineObject.setData( convertedData );
			StudyEdit.design.eventGroups.timelineObject.setVisibleChartRange( new Date( data.start ), new Date( data.end ), true );
		});
	},
	update: function() {},
	delete: function( id ) {
		var url = $( 'form#eventGroup' ).attr( 'action' ) + "Delete";
		var data = { 
			id: id
		};
		
		if( confirm( "Deleting this eventgroup will also delete all samples that originated from it, and remove all instances of the eventgroup. Are you sure you want to delete the eventgroup?" ) ) {
			$.post( url, data, function() {
				console.log( "Eventgroup deleted" );
			});
			
			// Also delete the eventgroup from the timeline with subjectEventgroups
			var data = [].concat( StudyEdit.design.timelineObject.getData() );
			var toRemove = [];
			var doRender = false;
			
			$.each( data, function( idx, el ) {
				if( el.data.eventGroupId == id ) {
					doRender = true
					StudyEdit.design.timelineObject.deleteItem( idx, true );
				}
			});
			
			if( doRender )
				StudyEdit.design.timelineObject.render();
			
			// Delete the list item as well
			$( '#eventgroups li[data-origin-id=' + id + ']' ).remove();
			
			return true;
		} else {
			return false;
		}
	},
	
	initialize: function( studyStartDate ) {
		// Create a dialog to add or edit event groups
		$( '#eventGroupDialog' ).dialog( { 
			modal: true, 
			autoOpen: false,
			width: 900,
			buttons: {
				Ok: function() {
					StudyEdit.design.eventGroups.save();
					StudyEdit.design.eventGroups.closeDialog();
				},
				Cancel: function() {
					StudyEdit.design.eventGroups.closeDialog();
				}
			},
			// Disable the main timeline when opening dialog, to prevent dropping events on the main timeline
			// See http://stackoverflow.com/questions/11997053/jqueryui-droppable-stop-propagation-to-overlapped-sibling
			open: function( event, ui ) {
				$( "#timeline-eventgroups .ui-droppable" ).droppable( "disable" );
			},
			
			// Enable the main timeline again when closing dialog
			// See http://stackoverflow.com/questions/11997053/jqueryui-droppable-stop-propagation-to-overlapped-sibling
			close: function( event, ui ) {
				$( "#timeline-eventgroups .ui-droppable" ).droppable( "enable" );
			},

		}).data( "studyStartDate", studyStartDate );
		
		// Enable buttons on existing event groups
		$(document).on( "click", "#eventgroups .edit", function() {
			var li = $(this).closest( "li" );
			StudyEdit.design.eventGroups.edit( li.data( "origin-id" ), li.data( "url" ) ); return false; 
		} );
		$(document).on( "click", "#eventgroups .delete", function() {
			var li = $(this).closest( "li" );
			StudyEdit.design.eventGroups.delete( li.data( "origin-id" ) );
			return false;
		} );
	},

	/**
	 * A set of event listeners to respond to events on the eventGroup timeline
	 */
	eventListeners: {
	    add: function() { 
	    	var timeline = StudyEdit.design.eventGroups.timelineObject;
	    	
	    	var selectedRow = timeline.getSelectedRow();
	    	var selectedIndex = timeline.getSelectedIndex();
	    	
	    	var eventId = timeline.getIdFromClassName( "dragged-origin-id-", selectedRow );
	    	var eventGroupId = $( '#eventGroupDialog' ).data( "eventgroup-id" );
	    	var eventType = timeline.getPropertyFromClassName( "dragged-origin-type-", selectedRow );
	    	var studyId = $( "#design" ).find( "#id" ).val();
	    	
	    	StudyEdit.design.eventGroups.contents.add( eventType, studyId, selectedRow.start, eventId, eventGroupId, function( element ) {
	    		timeline.updateData( selectedIndex, { data: { 
	    			id: element.id, 
	    			hasSamples: false, 
	    			type: element.type
	    		} } );
	    	});
	    },
	    
	    change: function() {
	    	var timeline = StudyEdit.design.eventGroups.timelineObject;
	    	
	    	var selectedRow = timeline.getSelectedRow();
	    	var selectedIndex = timeline.getSelectedIndex();
	    	
	    	if( selectedRow.data ) {
		    	var id = selectedRow.data.id;
		    	var eventType = selectedRow.data.type;
		    	var hasSamples = selectedRow.data.hasSamples;
		    	
		    	var changed = StudyEdit.design.eventGroups.contents.update( eventType, id, selectedRow.start, hasSamples, function(element) {
		    		var newData = {
		    			id: id,
		    			hasSamples: ( groupChanged ? false : hasSamples ),
		    			type: element.type
		    		}
				    		
			    	timeline.updateData( selectedIndex, { data: newData } );
		    		
		    	} );
		    	
		    	// The change can be cancelled if the user chooses to, or an error occurs. In that case, the original state must be restored
		    	if( !changed ) {
		    		timeline.cancelChange();
		    	}
	    	} else {
	    		console.log( "Invalid data from timeline: ", selectedRow );
	    		timeline.cancelChange();
	    	}
	    },
	    
	    delete: function() {
	    	var timeline = StudyEdit.design.eventGroups.timelineObject;
	    	var selectedRow = timeline.getSelectedRow();

	    	if( selectedRow.data ) {
		    	var id = selectedRow.data.id;
		    	var eventType = selectedRow.data.type;
		    	var hasSamples = selectedRow.data.hasSamples;
		    	
		    	// Delete the SubjectEventGroup itself. It if fails, also don't delete it from the timeline 
		    	if( !StudyEdit.design.eventGroups.contents.delete( eventType, id, hasSamples ) ) {
		    		timeline.cancelDelete();
		    	}
	    	} else {
	    		timeline.cancelDelete();
	    	}
	    }
	},
	
	/**
	 * Methods to handle adding, updating or deleting the contents of an event/samplingevent on the eventgroup timeline
	 * That is, it updates the EventInEventGroup of SamplingEventInEventgroup objects
	 */
	contents: {
		add: function( eventType, studyId, start, eventId, eventGroupId, afterAdd ) {
			var url = $( 'form#' + eventType + "InEventGroup" ).attr( 'action' ) + "Add";
			var data = {
				id: studyId,
				start: start.getTime(),
				eventId: eventId,
				eventGroupId: eventGroupId
			};
			$.post( url, data, function( returnData ) {
				if( typeof( afterAdd ) != "undefined" ) {
					afterAdd( returnData );
				}
			});
		},
		update: function( eventType, id, start, hasSamples, afterChange ) {
			var url = $( 'form#' + eventType + "InEventGroup" ).attr( 'action' ) + "Update";
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
		delete: function( eventType, id, hasSamples ) {
			var url = $( 'form#' + eventType + "InEventGroup" ).attr( 'action' ) + "Delete";
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
	
};

StudyEdit.design.events = {
	add: function() {},
	edit: function( id ) {},
	update: function( id, item ) {
		
	},
	delete: function( id ) {}
};

StudyEdit.design.samplingEvents = {
	add: function() {},
	edit: function( id ) {},
	save: function() {},
	delete: function( id ) {}
};
