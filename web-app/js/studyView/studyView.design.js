if( typeof( StudyView ) === "undefined" ) {
	StudyView = {};
};

StudyView.design = {
	timelineObject: null,
	studyStartDate: null,
	initialize: function( data, studyStartDate, groupReference ) {
		StudyView.design.studyStartDate = studyStartDate;
		StudyView.design.timelineObject = StudyView.design.timeline(
			"#timeline-eventgroups",
			data,
			{
				t0: studyStartDate
			}
		);
		StudyView.design.timelineObject.groupReference = groupReference;
		StudyView.design.eventGroups.initialize( studyStartDate );
		StudyView.design.tooltips.initialize();
		StudyView.design.subjectGroups.initialize();
	},

	readableScaledItems: function () {
        StudyView.design.timelineObject.repaintItems(true);
        StudyView.design.timelineObject.stackItems();
    },

    timeScaledItems: function () {
        StudyView.design.timelineObject.repaintItems(false);
        StudyView.design.timelineObject.stackItems();
    },

	determineRelativeTime: function( d ) {
		return RelTime.fromDates( StudyView.design.studyStartDate, d );
	},

	tooltips: {
		initialize: function( studyStartDate ) {
			// attach tooltip to every subjectEventGroup
			$( function() {
				$.each( StudyView.design.timelineObject.getData(), function( idx, item ) {
					if( item.data && item.data.id ) {
						var element = $( ".eventgroup-id-" + item.data.id );
						//console.log( "adding qtip to ", element );

						StudyView.design.tooltips.attachTooltip( element, StudyView.design.tooltips.determineContents( item ) );
					}
				});
			});
		},

		determineContents: function( item ) {
			var contents = "<b>" + item.content + "</b><br />";

			contents += "Start: " + StudyView.design.determineRelativeTime( item.start ).toString() + "<br />";
			if( item.end )
				contents += "End: " + StudyView.design.determineRelativeTime( item.end ).toString() + "<br />";

			return contents;
		},

		attachTooltip: function( element, contents ) {
			$(element).qtip({
				content: 'topMiddle',
				position: {
					corner: {
						tooltip: 'bottomMiddle',
						target: 'topMiddle'
					}
				},
				style: {
					border: {
						width: 3,
						radius: 6
					},
					padding: 10,
					textAlign: 'center',
					tip: true,
					name: 'blue'
				},
				content: contents,
				show: 'mouseover',
				hide: 'mouseout',
				api: {
					beforeShow: function() {
						// not used at this moment
					}
				}
			});
		}
	},

	timeline: function( container, data, options, eventHandlers ) {
		if( typeof( options ) === "undefined" )
			options = {};

		options = $.extend( options, {
			editable: false,
			groupsChangeable: false,
			showCurrentTime: false,
			showCustomTime: false,
			droppable: false
		} );

		var timeline = new dbnp.study.Timeline( $(container).get(0) );
		timeline.draw( data, options );

		return timeline;
	},
};