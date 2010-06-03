var timeline = new Array();
var firstDate = new Array();
var numEvents = new Array();
var totalEvents = new Array();
var heightSet = new Array();

var bandTitleInfo = new Array();

var bandTitles = new Array();
var bandSubjects = new Array();

var resizeTimerID = null;

/* 
 * Loads the timeline and retrieves the needed information
 * Should be called once when initializing the timeline. This can be done
 * in the onLoad event of de body-element. However, the timeline div should
 * be visible when it's layout is drawn. So if you use tabs, you should call
 * this function when showing the tab.
 */
function loadTimeline( divID, titleDivID, timelineNr ) {
    var postfix = "";
    if( timelineNr != undefined )
    {
        postfix = "-" + timelineNr
    }
    
    // Initialize global variables
    heightSet[ timelineNr ]   = false;
    numEvents[ timelineNr ]   = 0;
    totalEvents[ timelineNr ] = 0;
    bandTitleInfo[ timelineNr ] = new Array();

    // Initialize timeline bands (function to be written in the view)
    var bandInfos = createTimelineBands( timelineNr );

    // Create the timeline itself and show the div
    var timelineDiv = document.getElementById(divID + postfix);

    // Display the timeline
    timelineDiv.style.display = 'block';
    document.getElementById( titleDivID + postfix ).style.display = 'block';

    // Create the timeline itself
    timeline[ timelineNr ] = Timeline.create(timelineDiv, bandInfos);
    timeline[ timelineNr ].layout();
}

/*
 * Loads JSON events to the timeline and to the overview
 * This method is used as a callback for the AJAX calls loading the data.
 *
 * When all bands are loaded, the afterLoad method is called
 */
function _loadJSONEvents( json, returnvalue, httpRequest, timelineNr, bandNr, eventsource, overviewEventSource, event_url ) {
    // Load events into the event sources
    eventsource.loadJSON( json, event_url );
    overviewEventSource.loadJSON( json, event_url );

    // Make the band large enough (sort of autosizing; the default simile auto
    // sizing does not work properly)
    setBandWidth( timelineNr, bandNr, json.events.length );

    // If all bands are filled with events (i.e. when the numEvents array
    // is filled), we can compute the total height of the timeline
    if( numEvents[ timelineNr ] == timeline[ timelineNr ].getBandCount() - numTimelines && !heightSet[ timelineNr ] )
    {
        afterLoad( timelineNr );
    }
}

/*
 * Sets the bandwidth of the specified band, depending on the number of events
 */
function setBandWidth( timelineNr, bandNr, numEventsCurrentBand )
{
    // Make the band large enough (8 for margin and 20 per event )
    w = 8 + 20 * numEventsCurrentBand;
    timeline[ timelineNr ].getBand( bandNr )._bandInfo.width = w;

    // Save the number of events
    numEvents[ timelineNr ] += 1;
    totalEvents[ timelineNr ] += numEventsCurrentBand;
}

/*
 * Method to be called when the data of all bands is loaded. Does some auto-sizing
 * functions and adds titles to the timeline
 */
function afterLoad( timelineNr ) {
    setTotalHeight( timelineNr );
    addTitles( timelineNr );

    timeline[ timelineNr ].layout();

    // The center can only be set when the tab is visible
    setCenter( timelineNr, firstDate );
}

/*
 * Sets the total height of the timeline div
 */
function setTotalHeight( timelineNr ) {
    var postfix = "";
    if( timelineNr != undefined ) {
        postfix = "-" + timelineNr;
    }

    heightSet[ timelineNr ] = true;

    // First determine the total number of bands and events
    totalBands = timeline[ timelineNr ].getBandCount();
    totalRealBands = numEvents[ timelineNr ];

    // Compute the width of the overviewBand (= band nr 1)
    // 8 for margin and 6 per event
    // overviewWidth = totalRealBands * 5 + 40;
    // timeline.getBand(1)._bandInfo.width = overviewWidth

    // Set the total size
    totalWidth = 0;
    for( var j = 0; j < totalBands; j++ ) {
        totalWidth += timeline[ timelineNr ].getBand(j)._bandInfo.width;
    }
    document.getElementById( 'eventstimeline' + postfix ).style.height = totalWidth + "px";
}

/*
 * Adds titles to the timeline, if the 'eventtitles' div exists.
 */
function addTitles( timelineNr ) {
    var postfix = "";
    if( timelineNr != undefined ) {
        postfix = "-" + timelineNr;
    }

    // Top starts at 1px because of the border around the timeline
    var top = 1;
    var height = 0;
    var titlesDiv = document.getElementById( 'eventtitles' + postfix );
    
    if( titlesDiv ) {
        for( var i = 0; i < timeline[ timelineNr ].getBandCount(); i++ ) {
            var band = timeline[ timelineNr ].getBand(i);
            height = band._bandInfo.width - 10; // Adjusting for padding

            if( bandTitleInfo[ timelineNr ][i] ) {
                var E = document.createElement("div");
                E.className="timeline-title " + ( i % 2 == 0 ? 'even' : 'odd' );

                if( bandTitleInfo[ timelineNr ][ i ].className ) {
                    E.className += " " + bandTitleInfo[ timelineNr ][ i ].className;
                }
                E.style.top = Math.round( top ) + "px";
                E.style.height = Math.round( height ) + "px";
                E.appendChild( document.createTextNode( bandTitleInfo[ timelineNr ][i].title ) );

                var subjects = document.createElement( "div" );
                subjects.className = "timeline-subjects";
                subjects.appendChild( document.createTextNode( bandTitleInfo[ timelineNr ][i].subjects ) );
                E.appendChild(subjects);

                titlesDiv.appendChild(E);
            }

            top += band._bandInfo.width;
        }
    }
}

/*
 *  Can be called in the onResize of the body. Adjusts the autowidth of the div
 */
function resizeTimeline() {
    if (resizeTimerID == null) {
        resizeTimerID = window.setTimeout(function() {
            resizeTimerID = null;

            for( var i = 0; i < numTimelines; i++ )
            {
                timeline[i].layout();
            }
        }, 500);
    }
}

/*
 * Method to set the center of all bands to a specific date
 */
function setCenter( timelineNr, newDate ) {
    timeline[ timelineNr ].getBand( 0 ).setCenterVisibleDate( newDate );
}

/*
 * Extra painter method for the timeline, that doesn't show any dates or times
 */
Timeline.EmptyEtherPainter=function(A){this._params=A;
    this._theme=A.theme;
    this._startDate=SimileAjax.DateTime.parseGregorianDateTime(A.startDate);
};
Timeline.EmptyEtherPainter.prototype.initialize=function(C,B){this._band=C;
    this._timeline=B;
    this._backgroundLayer=C.createLayerDiv(0);
    this._backgroundLayer.setAttribute("name","ether-background");
    this._backgroundLayer.className="timeline-ether-bg";
    this._markerLayer=null;
    this._lineLayer=null;
    var D=("align" in this._params)?this._params.align:this._theme.ether.interval.marker[B.isHorizontal()?"hAlign":"vAlign"];
    var A=("showLine" in this._params)?this._params.showLine:this._theme.ether.interval.line.show;
    this._intervalMarkerLayout=new Timeline.EtherIntervalMarkerLayout(this._timeline,this._band,this._theme,D,A);
    this._highlight=new Timeline.EtherHighlight(this._timeline,this._band,this._theme,this._backgroundLayer);
};
Timeline.EmptyEtherPainter.prototype.setHighlight=function(A,B){this._highlight.position(A,B);
};
Timeline.EmptyEtherPainter.prototype.paint=function(){};
Timeline.EmptyEtherPainter.prototype.softPaint=function(){};
