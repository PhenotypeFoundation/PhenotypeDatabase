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
			showCustomTime: false
		} );
	    var timeline = new dbnp.study.Timeline( $(container).get(0) );
	    timeline.draw( data, options );
    	
	    if( dragFrom ) {
		    $( dragFrom ).find( "li:not(.add)" ).draggable({ helper: 'clone' });

	    }
	    
	    return timeline;
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
	save: function() {},
	delete: function( id ) {}
};

StudyEdit.samplingEvents = {
	add: function() {},
	edit: function( id ) {},
	save: function() {},
	delete: function( id ) {}
};