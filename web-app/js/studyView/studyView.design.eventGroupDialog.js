if( typeof(StudyView) === "undefined" ) {
	StudyView = {};
};

StudyView.design.eventGroups = {
	timelineObject: null,
	dialog: {
		get: function() {
			return $( '#eventGroupDialog' );
		},
		open: function() {
			// open the dialog
			StudyView.design.eventGroups.dialog.get().dialog( 'open' );
		},
		close: function() {
			// Close the dialog
			StudyView.design.eventGroups.dialog.get().dialog( 'close' );
		},

		timeline: {
			create: function( studyStartDate ) {
				var dialog = StudyView.design.eventGroups.dialog.get();

				if( $( '.timeline-frame', dialog ).length == 0 ) {
					StudyView.design.eventGroups.timelineObject = StudyView.design.timeline(
						"#timeline-events",
						[],
						{
							t0: studyStartDate
						}
					);
				} else {
					StudyView.design.eventGroups.dialog.timeline.clear();
				}
			},
			clear: function() {
				StudyView.design.eventGroups.timelineObject.clearItems();
				StudyView.design.eventGroups.timelineObject.repaint();
			},
			hide: function() {
				var dialog = StudyView.design.eventGroups.dialog.get();
				dialog.find( "#timeline-events, #design-meta" ).hide();
			},
			show: function() {
				var dialog = StudyView.design.eventGroups.dialog.get();
				dialog.find( "#timeline-events, #design-meta" ).show();
			}

		}
	},

	initialize: function( studyStartDate ) {

		// attach dialog subjectEventGroup
		$( function() {
			$.each( StudyView.design.timelineObject.getData(), function( idx, item ) {
				if( item.data && item.data.id ) {
					var element = $( ".eventgroup-id-" + item.data.id );

					element.on( "dblclick", function() {
						StudyView.design.eventGroups.show( item.data.eventGroupId, item.data.dataUrl )
					});
				}
			});
		});


		// Create a dialog to show event groups
		$( '#eventGroupDialog' ).dialog( {
			modal: true,
			autoOpen: false,
			width: 900,
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
	},

	show: function( id, dataUrl ) {
		var dialog = StudyView.design.eventGroups.dialog.get();
		dialog.data( "eventgroup-id", id );

		StudyView.design.eventGroups.dialog.open()

		// Create a timeline, if it doesn't exist
		StudyView.design.eventGroups.dialog.timeline.create( dialog.data( "studyStartDate" ) );

		// Load the data for the timeline
		$.get( dataUrl, function( data ) {

            dialog.dialog( "option", "title", "Sample & treatment group: "+data.name );

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
			StudyView.design.eventGroups.dialog.timeline.show();
			StudyView.design.eventGroups.timelineObject.setData( convertedData );
			StudyView.design.eventGroups.timelineObject.setVisibleChartRange( new Date( data.start ), new Date( data.end ), true );

			// Clear the name box
			$( '[name=eventgroup-name]' ).val( data.name );
		});
	},
};
