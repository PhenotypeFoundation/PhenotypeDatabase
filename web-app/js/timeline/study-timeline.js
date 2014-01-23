if( typeof( dbnp ) === 'undefined' ) {
	dbnp = {};
    // important: do not use var, as "var links = {};" will overwrite 
    //            the existing links variable value with undefined in IE8, IE7.  	
}

if( typeof( dbnp.study ) === 'undefined' ) {
	dbnp.study = {};
    // important: do not use var, as "var links = {};" will overwrite 
    //            the existing links variable value with undefined in IE8, IE7.  	
}

// Inherit from links.Timeline object, and use their constructor
// See http://phrogz.net/JS/classes/OOPinJS2.html
dbnp.study.Timeline = function(container) {
	links.Timeline.call( this, container );
	this.setRelativeTime();
};
dbnp.study.Timeline.prototype = new links.Timeline();
dbnp.study.Timeline.prototype.constructor = dbnp.study.Timeline;
dbnp.study.Timeline.prototype.timeline = links.Timeline.prototype;
dbnp.study.Timeline.prototype.setRelativeTime = function() {
	this.step = new dbnp.StepRelativeDate( new Date() );
};
dbnp.study.Timeline.prototype.draw = function( data, options ) {
	if( typeof( options ) == 'undefined' )
		options = {};
	
	// Initialize relative time
	if( options.t0 ) {
		this.step.t0 = options.t0; 
	}
	
	this.timeline.draw.call( this, data, options );

	// Enable the droppable behavior
	if( options && options.editable && options.droppable ) {
	     //$( "#eventGroups li" ).draggable({ helper: 'clone' });
	     var timeline = this;
	     
	    $( timeline.dom.content ).droppable({ 
		   	 drop: function( event, ui ) {
		   		 var offsetLeft = ui.offset.left - links.Timeline.getAbsoluteLeft( timeline.dom.content );
		   		 var offsetTop = ui.offset.top - links.Timeline.getAbsoluteTop( timeline.dom.content );
		   		 
	    		 if( !isNaN( offsetLeft ) && !isNaN( offsetTop ) && offsetTop > 0 && offsetLeft > 0 ) {
	    			var start = timeline.screenToTime( offsetLeft );
	    			
	    			// Snap events to whole date/time
	                if (timeline.options.snapEvents) {
	                    timeline.step.snap(start);	
	                }
	    			
	    	        var group = timeline.getGroupFromHeight( offsetTop );
	    		    
	    	        var itemOptions = {
	    		        'start': start,
	    		        'content': ui.helper.find( '.name' ).text(),
	    		        'group': timeline.getGroupName( group ),
	    		        'className': 'dragged-origin-id-' + ui.draggable.data( "origin-id" ) + ' dragged-origin-type-' + ui.draggable.data( "origin-type" ) + ' ' + ui.draggable.data( "origin-type" )
	    		    };
	    	        
	    	        // Determine the duration of this event. Set to 0 if the event has a 
	    	        // duration but it is not determined yet
	    	        var duration = $( ui.draggable ).data( "duration" );
	    	        
	    	        if( typeof( duration ) != "undefined" ) {
	    	        	if( duration == 0 ) {
	    	        		itemOptions.type = "box";
	    	        	} else { 
		    	        	// A single point event
		    	        	itemOptions.end = new Date( start.getTime() + duration * 1000 );
	    	        	}
	    	        }
	    	        
	    	        // Add the item, and select it
	    	        timeline.addItem( itemOptions );
	    	        var index = timeline.items.length - 1;
                    timeline.selectItem(index);
	    	        timeline.applyAdd = true;

	                // fire a dragin event.
	                // Note that the change can be canceled from within an event listener if
	                // this listener calls the method cancelAdd().
	    	        timeline.trigger( "dragin" );

	                if (timeline.applyAdd) {
	                    // render and select the item
	                	timeline.render({animate: false});
	                	timeline.selectItem(index);
	                }
	                else {
	                    // undo an add
	                	timeline.deleteItem(index);
	                }	    	        
	    		 }
		     }
	    });	     
	}
}

dbnp.study.Timeline.prototype.deleteGroups = function() {
	// Delete groups
	this.timeline.deleteGroups.call( this );
	
	// Add all groups again, in order to have the proper groups all the time
	if( typeof( this.groupReference ) != "undefined" ) {
		for( i in this.groupReference ) {
			this.getGroup( this.groupReference[ i ].name );
		}
	}
}

dbnp.study.Timeline.prototype.getIdFromClassName = function( prefix, item ) {
	return this.getPropertyFromClassName( prefix, item, "([0-9]+)" )
}

dbnp.study.Timeline.prototype.getPropertyFromClassName = function( prefix, item, regex ) {
	if( typeof( regex ) == 'undefined' )
		regex = "(\\w+)"

	// Convert regex into proper regex
	regex = new RegExp( prefix + regex ) 
	
	var result = regex.exec( item.className );
	
	if( result ) {
		return result[ 1 ];
	} else {
		return null;
	}	
}

dbnp.study.Timeline.prototype.getSelectedRow = function() {
	var index = this.getSelectedIndex();
	
	// We don't use the getItem method for now, since that doesn't return the className properly
	// However, in future versions this will be fixed (see https://github.com/almende/chap-links-library/commit/344718605c44c2f42ab44fb1f398a43247d4bde2)
	// and then 
	//		timeline.getItem( index )
	// is the prefered way to go.
	var data = this.getData();
	return data[ index ];
},

dbnp.study.Timeline.prototype.getSelectedIndex = function() {
	var selection= this.getSelection();
	
	if( !selection )
		return null;
	
	return selection[0].row;
}

/** ------------------------------------------------------------------------ **/

//Setup a class that can show relative dates on the timeline, based on the default
//axis renderer
dbnp.StepRelativeDate =  function( t0, start, end, minimumStep ) {
	 this.t0 = t0;
	 
    // variables
    this.current = new Date();
    this.setCurrentRelTime();
    this._start = new Date();
    this._end = new Date();

    this.autoScale  = true;
    this.scale = dbnp.StepRelativeDate.SCALE.DAY;
    this.step = 1;

    // initialize the range
    this.setRange(start, end, minimumStep);	 
}

/// enum scale
dbnp.StepRelativeDate.SCALE = links.Timeline.StepDate.SCALE;
dbnp.StepRelativeDate.SCALE.WEEK = 9;

dbnp.StepRelativeDate.prototype = new links.Timeline.StepDate();
dbnp.StepRelativeDate.prototype.parent = links.Timeline.StepDate.prototype;
dbnp.StepRelativeDate.prototype.getRelTime = function( date ) {
	return RelTime.fromDates( this.t0, date );
}

dbnp.StepRelativeDate.prototype.setCurrentRelTime = function() {
	this.currentRelTime = this.getRelTime( this.current );	
}

dbnp.StepRelativeDate.prototype.setCurrentFromCurrentRelTime = function() {
	this.current = new Date( this.t0.getTime() + this.currentRelTime.time * 1000 );
}

/**
 * Set the step iterator to the start date.
 * @Override
 */
dbnp.StepRelativeDate.prototype.start = function() {
    this.current = new Date(this._start.valueOf());
    this.setCurrentRelTime();
    this.roundToMinor();
};

/**
 * Round the current date to the first minor date value
 * This must be executed once when the current date is set to start Date
 */
dbnp.StepRelativeDate.prototype.roundToMinor = function() {
    // round to floor
    switch (this.scale) {
        case dbnp.StepRelativeDate.SCALE.YEAR: 		 this.currentRelTime.roundDown( 'y', this.step ); break;
        case dbnp.StepRelativeDate.SCALE.WEEK:        this.currentRelTime.roundDown( 'w', this.step ); break;
        case dbnp.StepRelativeDate.SCALE.DAY:          // intentional fall through
        case dbnp.StepRelativeDate.SCALE.WEEKDAY:      this.currentRelTime.roundDown( 'd', this.step ); break;
        case dbnp.StepRelativeDate.SCALE.HOUR:         this.currentRelTime.roundDown( 'h', this.step ); break;
        case dbnp.StepRelativeDate.SCALE.MINUTE:       this.currentRelTime.roundDown( 'm', this.step ); break;
        case dbnp.StepRelativeDate.SCALE.SECOND:       this.currentRelTime.roundDown( 's', this.step ); break;
    }
    this.setCurrentFromCurrentRelTime();
    
};

/**
 * Do the next step
 */
dbnp.StepRelativeDate.prototype.next = function() {
    var prev = this.current.valueOf();

    var scale = null;
    switch (this.scale) {
	    case dbnp.StepRelativeDate.SCALE.SECOND:	scale = 's'; break;
	    case dbnp.StepRelativeDate.SCALE.MINUTE:	scale = 'm'; break;
	    case dbnp.StepRelativeDate.SCALE.HOUR:		scale = 'h'; break;
	    case dbnp.StepRelativeDate.SCALE.WEEKDAY:      // intentional fall through
	    case dbnp.StepRelativeDate.SCALE.DAY:		scale = 'd'; break;
	    case dbnp.StepRelativeDate.SCALE.WEEK:		scale = 'w'; break;
	    case dbnp.StepRelativeDate.SCALE.YEAR:		scale = 'y'; break;
	    default:                      break;
    }
    
    if( scale ) {
    	this.currentRelTime.add( scale, this.step );
    	this.setCurrentFromCurrentRelTime();
    }
    
    // safety mechanism: if current time is still unchanged, move to the end
    if (this.current.valueOf() == prev) {
        this.current = new Date(this._end.valueOf());
    	this.setCurrentRelTime();
    }
};

/**
 * Automatically determine the scale that bests fits the provided minimum step
 * @param {Number} minimumStep  The minimum step size in milliseconds
 */
dbnp.StepRelativeDate.prototype.setMinimumStep = function(minimumStep) {
    if (minimumStep == undefined) {
        return;
    }

    var stepYear       = (1000 * 60 * 60 * 24 * 365);
    var stepWeek       = (1000 * 60 * 60 * 24 * 7);
    var stepDay        = (1000 * 60 * 60 * 24);
    var stepHour       = (1000 * 60 * 60);
    var stepMinute     = (1000 * 60);
    var stepSecond     = (1000);
    var stepMillisecond= (1);

    // find the smallest step that is larger than the provided minimumStep
    if (stepYear*1000 > minimumStep)        {this.scale = dbnp.StepRelativeDate.SCALE.YEAR;        this.step = 1000;}
    if (stepYear*500 > minimumStep)         {this.scale = dbnp.StepRelativeDate.SCALE.YEAR;        this.step = 500;}
    if (stepYear*100 > minimumStep)         {this.scale = dbnp.StepRelativeDate.SCALE.YEAR;        this.step = 100;}
    if (stepYear*50 > minimumStep)          {this.scale = dbnp.StepRelativeDate.SCALE.YEAR;        this.step = 50;}
    if (stepYear*10 > minimumStep)          {this.scale = dbnp.StepRelativeDate.SCALE.YEAR;        this.step = 10;}
    if (stepYear*5 > minimumStep)           {this.scale = dbnp.StepRelativeDate.SCALE.YEAR;        this.step = 5;}
    if (stepYear > minimumStep)             {this.scale = dbnp.StepRelativeDate.SCALE.YEAR;        this.step = 1;}
    if (stepWeek*13 > minimumStep)          {this.scale = dbnp.StepRelativeDate.SCALE.WEEK;        this.step = 13;}
    if (stepWeek*4 > minimumStep)           {this.scale = dbnp.StepRelativeDate.SCALE.WEEK;        this.step = 4;}
    if (stepWeek > minimumStep)             {this.scale = dbnp.StepRelativeDate.SCALE.WEEK;        this.step = 1;}
    
    if (stepDay * 2> minimumStep)           {this.scale = dbnp.StepRelativeDate.SCALE.DAY;         this.step = 2;}
    if (stepDay > minimumStep)              {this.scale = dbnp.StepRelativeDate.SCALE.DAY;         this.step = 1;}
    if (stepHour*12 > minimumStep)           {this.scale = dbnp.StepRelativeDate.SCALE.HOUR;       this.step = 12;}
    if (stepHour*4 > minimumStep)           {this.scale = dbnp.StepRelativeDate.SCALE.HOUR;        this.step = 4;}
    if (stepHour > minimumStep)             {this.scale = dbnp.StepRelativeDate.SCALE.HOUR;        this.step = 1;}
    if (stepMinute*15 > minimumStep)        {this.scale = dbnp.StepRelativeDate.SCALE.MINUTE;      this.step = 15;}
    if (stepMinute*10 > minimumStep)        {this.scale = dbnp.StepRelativeDate.SCALE.MINUTE;      this.step = 10;}
    if (stepMinute*5 > minimumStep)         {this.scale = dbnp.StepRelativeDate.SCALE.MINUTE;      this.step = 5;}
    if (stepMinute > minimumStep)           {this.scale = dbnp.StepRelativeDate.SCALE.MINUTE;      this.step = 1;}
    if (stepSecond*15 > minimumStep)        {this.scale = dbnp.StepRelativeDate.SCALE.SECOND;      this.step = 15;}
    if (stepSecond*10 > minimumStep)        {this.scale = dbnp.StepRelativeDate.SCALE.SECOND;      this.step = 10;}
    if (stepSecond*5 > minimumStep)         {this.scale = dbnp.StepRelativeDate.SCALE.SECOND;      this.step = 5;}
    if (stepSecond > minimumStep)           {this.scale = dbnp.StepRelativeDate.SCALE.SECOND;      this.step = 1;}
    
    // No support for milliseconds
};

/**
 * Snap a date to a rounded value. The snap intervals are dependent on the
 * current scale and step.
 * @param {Date} date   the date to be snapped
 */
dbnp.StepRelativeDate.prototype.snap = function(date) {
	var reltime = RelTime.fromDates( this.t0, date );

    switch (this.scale) {
	    case dbnp.StepRelativeDate.SCALE.YEAR: 		  reltime.round( 'y', this.step ); break;
	    case dbnp.StepRelativeDate.SCALE.WEEK:        reltime.round( 'w', this.step ); break;
	    case dbnp.StepRelativeDate.SCALE.DAY:          // intentional fall through
	    case dbnp.StepRelativeDate.SCALE.WEEKDAY:     reltime.round( 'd', this.step ); break;
	    case dbnp.StepRelativeDate.SCALE.HOUR:        reltime.round( 'h', this.step ); break;
	    case dbnp.StepRelativeDate.SCALE.MINUTE:      reltime.round( 'm', this.step ); break;
	    case dbnp.StepRelativeDate.SCALE.SECOND:      reltime.round( 's', this.step ); break;
	}	
	
    date.setTime( this.t0.getTime() + reltime.time * 1000 );
    
};

/**
 * Check if the current step is a major step (for example when the step
 * is DAY, a major step is each first day of the MONTH)
 * @return {boolean} true if current date is major, else false.
 */
dbnp.StepRelativeDate.prototype.isMajor = function() {
	var reltime = this.currentRelTime.time;
    switch (this.scale) {
        case dbnp.StepRelativeDate.SCALE.SECOND:
            return reltime % RelTime.duration.m == 0;
        case dbnp.StepRelativeDate.SCALE.MINUTE:
            return reltime % RelTime.duration.h == 0;
        case dbnp.StepRelativeDate.SCALE.HOUR:
            return reltime % RelTime.duration.d == 0;
        case dbnp.StepRelativeDate.SCALE.WEEKDAY: // intentional fall through
        case dbnp.StepRelativeDate.SCALE.DAY:
            return reltime % RelTime.duration.w == 0;
        case dbnp.StepRelativeDate.SCALE.WEEK:
            return reltime % RelTime.duration.y == 0;
        case dbnp.StepRelativeDate.SCALE.YEAR:
            return false;
        default:
            return false;
    }
};


/**
 * Returns formatted text for the minor axislabel, depending on the current
 * date and the scale. For example when scale is MINUTE, the current time is
 * formatted as "hh:mm".
 * @param {Object} options
 * @param {Date} [date] custom date. if not provided, current date is taken
 */
dbnp.StepRelativeDate.prototype.getLabelMinor = function(options, date) {
	if (date == undefined) {
        date = this.current;
    }

    var reltime = RelTime.fromDates( this.t0, date );
    
    var labelRelTime = null
    switch (this.scale) {
        case dbnp.StepRelativeDate.SCALE.SECOND:		labelRelTime = reltime.getModulo( "s" ); break;
        case dbnp.StepRelativeDate.SCALE.MINUTE:		labelRelTime = reltime.getModulo( "m" ); break;
        case dbnp.StepRelativeDate.SCALE.HOUR:			labelRelTime = reltime.getModulo( "h" ); break;
        case dbnp.StepRelativeDate.SCALE.WEEKDAY:		// intentional fall through
        case dbnp.StepRelativeDate.SCALE.DAY:			
        	if( this.step == 1 ) {
        		labelRelTime = reltime.getModulo( "d" ); break;
        	} else {
        		// We want the number of days within a two-week period (so from 0 to 13)
        		// This is non standard functionality, so implement it ourselves
        		var numDays = Math.round( reltime.time % ( RelTime.duration.w * this.step ) / RelTime.duration.d );
        		if( numDays != 0 )
        			return numDays + "d";
        		else
        			return "0";
        	}
        case dbnp.StepRelativeDate.SCALE.WEEK:     		labelRelTime = reltime.getModulo( "w" ); break;
        case dbnp.StepRelativeDate.SCALE.YEAR:         	labelRelTime = reltime.getModulo( "y" ); break;
    }
    
    if( labelRelTime )
    	return labelRelTime.toString();
    else
    	return "";
};


/**
 * Returns formatted text for the major axislabel, depending on the current
 * date and the scale. For example when scale is MINUTE, the major scale is
 * hours, and the hour will be formatted as "hh".
 * @param {Object} options
 * @param {Date} [date] custom date. if not provided, current date is taken
 */
dbnp.StepRelativeDate.prototype.getLabelMajor = function(options, date) {
	if (date == undefined) {
        date = this.current;
    }
	reltime = RelTime.fromDates( this.t0, date );
    switch (this.scale) {
        case dbnp.StepRelativeDate.SCALE.SECOND:
        	reltime.roundDown( 'm' );
        	return reltime.toString();
        case dbnp.StepRelativeDate.SCALE.MINUTE:
        	reltime.roundDown( 'h' );
        	return reltime.toString();
        case dbnp.StepRelativeDate.SCALE.HOUR:
        	reltime.roundDown( 'd' );
        	return reltime.toString();
        case dbnp.StepRelativeDate.SCALE.WEEKDAY:
        case dbnp.StepRelativeDate.SCALE.DAY:
        	reltime.roundDown( 'w' );
        	return reltime.toString();
        case dbnp.StepRelativeDate.SCALE.WEEK:
        	reltime.roundDown( 'y' );
        	return reltime.toString();
        default:
            return "";
    }
};


