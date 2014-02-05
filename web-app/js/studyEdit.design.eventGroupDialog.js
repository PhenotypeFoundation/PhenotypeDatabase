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
							dragFrom: "#events, #samplingEvents",
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
			},
			show: function() {
				var dialog = StudyEdit.design.eventGroups.dialog.get();
				dialog.find( "#timeline-events, #design-meta" ).show();
			}
			
		}
	},
	
	initialize: function( studyStartDate ) {
		// Create a dialog to add or edit event groups
		$( '#eventGroupDialog' ).dialog( { 
			modal: true, 
			autoOpen: false,
			width: 900,
			buttons: {
				"Save name": function() {
					StudyEdit.design.eventGroups.save();
					$(this).dialog( "close" );
					StudyEdit.design.eventGroups.dialog.close( this );
				},
				Cancel: function() {
					// Update the main timeline to reflect the (new) length and name of the eventgroup
					StudyEdit.design.eventGroups.updateEventGroupInMainOverview();
					StudyEdit.design.eventGroups.dialog.close();
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
			StudyEdit.design.eventGroups.edit( li.data( "originId" ), li.data( "url" ) ); return false; 
		} );
		$(document).on( "click", "#eventgroups .delete", function() {
			var li = $(this).closest( "li" );
			StudyEdit.design.eventGroups.delete( li.data( "originId" ) );
			return false;
		} );
		
		// Create a dialog to add/edit existing events/samplingevents
		StudyEdit.design.eventGroups.contents.dialog.create(); 
		
		// Enable buttons on existing events and samplingevents
		$(document).on( "click", "#events .edit", function() {
			var li = $(this).closest( "li" );
			StudyEdit.design.events.edit( "event", li.data( "originId" ), li.data( "url" ) ); return false; 
		} );
		$(document).on( "click", "#events .delete", function() {
			var li = $(this).closest( "li" );
			StudyEdit.design.events.delete( "event", li.data( "originId" ) );
			return false;
		} );
		
		// Enable buttons on existing event samplingEvents
		$(document).on( "click", "#samplingEvents .edit", function() {
			var li = $(this).closest( "li" );
			StudyEdit.design.events.edit( "samplingEvent", li.data( "originId" ), li.data( "url" ) ); return false; 
		} );
		$(document).on( "click", "#samplingEvents .delete", function() {
			var li = $(this).closest( "li" );
			StudyEdit.design.events.delete( "samplingEvent", li.data( "originId" ) );
			return false;
		} );
		
	},
	
	
	/**
	 * Shows a to add a new eventgroup
	 */
	add: function() {
		var dialog = StudyEdit.design.eventGroups.dialog.get();
		dialog.dialog( "option", "title", "Add eventgroup" );
		dialog.data( "eventgroup-id", null );
		StudyEdit.design.eventGroups.dialog.open()

		// Hide the timeline (if it exists), to prevent the user from adding items to a not existing timeline
		StudyEdit.design.eventGroups.dialog.timeline.hide();
		
		// Clear the name box
		$( '[name=eventgroup-name]' ).val( "" );
	},
	
	/**
	 * Saves the data entered in the eventgroup dialog
	 */
	save: function() {
		var dialog = StudyEdit.design.eventGroups.dialog.get();
		var id = dialog.data( "eventgroup-id" );
		var action = ( id ? "Update" : "Add" );
		var url = $( 'form#eventGroup' ).attr( 'action' ) + action;
				
		var data = { 
			name: $( '[name=eventgroup-name]' ).val()
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
				StudyEdit.design.eventGroups.updateEventGroupInMainOverview();
			} else {
				// Add new eventgroup
				StudyEdit.design.eventGroups.addEventGroupInMainOverview( returnData );
			}
		});
		
		return true;
	},
	
	edit: function( id, dataUrl ) {
		var dialog = StudyEdit.design.eventGroups.dialog.get();
		dialog.dialog( "option", "title", "Edit treatment & sample group" );
		dialog.data( "eventgroup-id", id );
		
		StudyEdit.design.eventGroups.dialog.open()

		// Create a timeline, if it doesn't exist
		StudyEdit.design.eventGroups.dialog.timeline.create( dialog.data( "studyStartDate" ) );
		
		// Load the data for the timeline
		$.get( dataUrl, function( data ) {
			var convertedData = $.map( data.events, function( event, idx ) {
				// Check whether the event should be shown as a range or as a box
				if( event.data.type == "event" ) {
					if( event.start == event.end ) {
						event.type = "box";
					}
					
				}
				
				// Convert dates to javascript objects
				event.start = new Date( event.start );
				
				if( event.end ) {
					event.end = new Date( event.end );
				}
				
				return event;
			} );
			StudyEdit.design.eventGroups.dialog.timeline.show();
			StudyEdit.design.eventGroups.timelineObject.setData( convertedData );
			StudyEdit.design.eventGroups.timelineObject.setVisibleChartRange( new Date( data.start ), new Date( data.end ), true );
			
			// Clear the name box
			$( '[name=eventgroup-name]' ).val( data.name );
		});
	},
	
	// Adds a new eventgroup to the main overview
	addEventGroupInMainOverview: function( eventGroupData ) {
		// Add a new eventgroup
		var li = $( '<li>' )
			.data( "duration", eventGroupData.duration )
			.data( "origin-id", eventGroupData.id )
			.data( "url", eventGroupData.url )
			.attr( "id", "eventgroup-" + eventGroupData.id )
			.append( $( "<span class='name'>" ).text( eventGroupData.name ) )
			.append( $( "<span class='events'>" ) )
			.append( 
				$( "<span class='buttons'>" )
					.append( '<a href="#" class="edit">edit</a>') 
					.append( '<a href="#" class="delete">del</a>') 
			);
		$( '#eventgroups ul .add' ).before( li );
		
		// Make sure it can be added to the timeline
		li.draggable({ helper: 'clone' });
	},
	
	// Update the main timeline with the (new) length of an updated eventgroup
	updateEventGroupInMainOverview: function() {
		var dialog = StudyEdit.design.eventGroups.dialog.get();
		var id = dialog.data( "eventgroup-id" );
		var dataUrl = $( 'li#eventgroup-' + id ).data( 'url' );
		
		// Load the data for this eventgroup
		$.get( dataUrl, function( eventGroupData ) {
			// Update the duration and nameof the eventgroup on the timeline
			var data = StudyEdit.design.timelineObject.getData();
			var doRender = false;
			
			$.each( data, function( idx, el ) {
				if( el.data.eventGroupId == id ) {
					// Update the duration and type of the item
					var end = new Date();
					var duration = eventGroupData.duration;
					end.setTime( el.start.getTime() + duration * 1000 );
					var type = duration == 0 ? "box" : "range";
					StudyEdit.design.timelineObject.updateData( idx, { content: eventGroupData.name, end: end, type: type } );
					doRender = true
				}
			});
			
			if( doRender )
				StudyEdit.design.timelineObject.redraw();
			
			// Update the duration and text in the eventgroup box
			$( '#eventgroup-' + eventGroupData.id ).data( "duration", eventGroupData.duration );
			$( '#eventgroup-' + eventGroupData.id + ' .name' ).text( eventGroupData.name );
		});
	},
	
	delete: function( id ) {
		var url = $( 'form#eventGroup' ).attr( 'action' ) + "Delete";
		var data = { 
			id: id
		};
		
		if( confirm( "Deleting this treatment&sample group will also delete all samples that originated from it, and remove all instances of this group. Are you sure you want to delete the treatment&sample group?" ) ) {
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
			$( '#eventgroups li#eventgroup-' + id ).remove();
			
			return true;
		} else {
			return false;
		}
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
	    	
	    	// If the user drags in an event, he should specify the duration
	    	var duration = 0;
	    	if( eventType == "event" ) {
	    		duration = prompt( "Please specify the duration of this treatment type. Specify 0 for no duration." );
	    		
	    		if( duration == null ) {
		    		timeline.cancelAdd();
		    		return;
	    		}
	    	}
	    	
	    	StudyEdit.design.eventGroups.contents.add( eventType, studyId, selectedRow.start, duration,  eventId, eventGroupId, function( element ) {
	    		// Determine how to show the box on the timeline: 
	    		// By default, events are shown as range, samplignEvents as box. However
	    		// that raises issues with events with duration = 0. For that reason
	    		// the type for those events is set to 'dot'
	    		var drawType = ( eventType == 'samplingEvent' || duration == 0 ) ? "box" : "range"
	    		var end = eventType == "event" ? new Date( element.end ) : undefined; 
	    		timeline.updateData( selectedIndex, {
	    			type: drawType,
	    			end: end,
	    			data: { 
		    			id: element.id, 
		    			hasSamples: false, 
		    			eventId: element.eventId,
		    			type: element.type
	    			} 
	    		} );
	    		
	    		timeline.redraw();
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
		    	var eventId = selectedRow.data.eventId;
		    	
		    	var changed = StudyEdit.design.eventGroups.contents.update( eventType, id, selectedRow.start, selectedRow.end, hasSamples, function(element) {
		    		var newData = {
		    			id: id,
		    			eventId: eventId,
		    			hasSamples: hasSamples,
		    			type: eventType
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
		dialog: {
			obj: null,
			create: function() {
				StudyEdit.design.eventGroups.contents.dialog.obj = StudyEdit.design.eventGroups.contents.dialog.getDiv().dialog({ 
					modal: true, 
					autoOpen: false,
					width: 900,
					buttons: {
						"Save name": function() {
							StudyEdit.design.events.save();
						},
						Cancel: function() {
							StudyEdit.design.eventGroups.contents.dialog.close( this );
						}
					},
				});
				
				return StudyEdit.design.eventGroups.contents.dialog.obj;
			},
			getDiv: function() {
				return $( '#eventGroupContentsDialog' );
			},
			get: function() {
				return $( '#eventGroupContentsDialog' );
			},
			open: function() {
				// open the dialog
				StudyEdit.design.eventGroups.contents.dialog.get().dialog( 'open' );
			},
			close: function( dialog ) {
				// Close the dialog
				StudyEdit.design.eventGroups.contents.dialog.get().dialog( 'close' );
			},
		},		
		
		add: function( eventType, studyId, start, duration, eventId, eventGroupId, afterAdd ) {
			var url = $( 'form#' + eventType + "InEventGroup" ).attr( 'action' ) + "Add";
			var data = {
				id: studyId,
				start: start.getTime(),
				duration: duration,
				eventId: eventId,
				eventGroupId: eventGroupId
			};
			$.post( url, data, function( returnData ) {
				if( typeof( afterAdd ) != "undefined" ) {
					afterAdd( returnData );
				}
			});
		},
		update: function( eventType, id, start, end, hasSamples, afterChange ) {
			var url = $( 'form#' + eventType + "InEventGroup" ).attr( 'action' ) + "Update";
			var data = { 
				id: id, 
				start: start.getTime(),
				end: ( typeof( end ) != "undefined" ? end.getTime() : null )
			};
			
			var doUpdate = true;
			
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
			
			if( eventType == "event" || confirm( "Deleting this sample type will also delete all samples that originated from it. Are you sure you want to delete the sample type?" ) ) {
				$.post( url, data, function() {
					console.log( eventType + " deleted" );
				});
				
				return true;
			} else {
				return false;
			}
		}
	}
	
};

/**
 * Handles both events and sampling events
 */
StudyEdit.design.events = {
	add: function( eventType ) {
		var dialog = StudyEdit.design.eventGroups.contents.dialog.get();
		dialog.dialog( 'option', 'title', 'Add ' + eventType  );
		StudyEdit.design.eventGroups.contents.dialog.getDiv().load( $( '#' + eventType + 's .add' ).data( 'url' ), function() {
			StudyEdit.design.events.onLoad( eventType );
			dialog.dialog( "open" );
		});
	},
	save: function() {
		var dialog = StudyEdit.design.eventGroups.contents.dialog.getDiv();
		dialog.find( "[name=_action]" ).val( "save" );
		dialog.find( 'form' ).submit();
	},
	edit: function( eventType, id ) {
		var dialog = StudyEdit.design.eventGroups.contents.dialog.get();
		dialog.dialog( 'option', 'title', 'Edit ' + eventType );
		StudyEdit.design.eventGroups.contents.dialog.getDiv().load( $( '#' + eventType + 's #' + eventType + '-' + id ).data( 'url' ), function() {
			StudyEdit.design.events.onLoad( eventType );
			dialog.dialog( "open" );
		});
	},
	delete: function( eventType, id ) {
		var url = $( 'form#' + eventType ).attr( 'action' ) + "Delete";
		var data = { 
			id: id
		};
		
		if( eventType == "event" || confirm( "Deleting this sample type will also delete all samples that originated from it. Are you sure you want to delete the sample type?" ) ) {
			$.post( url, data, function() {
				console.log( eventType + " deleted" );
				
    			// Update the list of events/samplingEvents
    			StudyEdit.design.events.reloadList( eventType );
			});
			
			// Also delete the eventgroup from the timeline with subjectEventgroups
			var data = [].concat( StudyEdit.design.eventGroups.timelineObject.getData() );
			var toRemove = [];
			var doRender = false;
			
			$.each( data, function( idx, el ) {
				if( el.data.type == eventType && el.data.eventId == id ) {
					doRender = true
					StudyEdit.design.eventGroups.timelineObject.deleteItem( idx, true );
				}
			});
			
			if( doRender )
				StudyEdit.design.eventGroups.timelineObject.render();
			
			return true;
		} else {
			return false;
		}		
	},
	
	reloadList: function( eventType ) {
		var list = $( '#' + eventType + 's' );
		list.load( list.data( "url" ), function() {
			// Make sure it can be added to the timeline
			list.find( 'li:not(.add)' ).draggable({ helper: 'clone' });
		});
	},
	
	/**
	 * Handles loading new data into the popup dialog
	 */
	onLoad: function( eventType ) {
		var dialog = StudyEdit.design.eventGroups.contents.dialog.get();
		// Handle form with ajax
		//callback handler for form submit
		StudyEdit.design.eventGroups.contents.dialog.getDiv().find( 'form' ).submit(function(e)
		{
		    var postData = $(this).serializeArray();
		    var form = $(this);
		    var formURL = $(this).attr("action");
		    $.ajax({
		        url : formURL,
		        type: "POST",
		        data : postData
		    })
		    .done(function(data, textStatus, jqXHR) 
		        {
		    		if( jqXHR.status == 210 ) {
		    			// Everything is OK
		    			dialog.dialog( "close" );
		    			
		    			// Update the list of events/samplingEvents
		    			StudyEdit.design.events.reloadList( eventType );
		    			
		    			// Also update the event on the timeline
		    			var id = form.find( "[name=id]" ).val();
		    			var name = form.find( "[name=name]" ).val();
		    			
		    			if( id ) {
			    			var data = [].concat( StudyEdit.design.eventGroups.timelineObject.getData() );
			    			var toRemove = [];
			    			var doRender = false;
			    			
			    			$.each( data, function( idx, el ) {
			    				if( el.data.type == eventType && el.data.eventId == id ) {
			    					StudyEdit.design.eventGroups.timelineObject.updateData( 
			    						idx, 
			    						{ content: name ? name : "[no name]" } 
			    					);
			    					doRender = true
			    				}
			    			});
			    			
			    			if( doRender ) {
			    				StudyEdit.design.eventGroups.timelineObject.redraw();
			    			}
		    			}
		    			
		    		} else {
		    			StudyEdit.design.eventGroups.contents.dialog.getDiv().html( data );
		    			StudyEdit.design.events.onLoad( eventType );
		    		}
		    		
		        })
		    .fail( function(jqXHR, textStatus, errorThrown) {
		    		StudyEdit.design.eventGroups.contents.dialog.getDiv().html( jqXHR.responseText );   
	    			StudyEdit.design.events.onLoad( eventType );
		        }
		    );
		    e.preventDefault(); //STOP default action
		});
	}
};
