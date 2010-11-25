/*==================================================
 *  Relative Date Labeller
 *==================================================
 */

Timeline.RelativeDateLabeller = function(locale, timeZone, startDate) {
    this._locale = locale;
    this._timeZone = timeZone;
	this._startDate = startDate;
};

Timeline.RelativeDateLabeller.dayNames = [];
Timeline.RelativeDateLabeller.labelIntervalFunctions = [];

Timeline.RelativeDateLabeller.getMonthName = function(month, locale) {
    return Timeline.GregorianDateLabeller.monthNames[locale][month];
};

Timeline.RelativeDateLabeller.prototype.labelInterval = function(date, intervalUnit) {
    var f = Timeline.RelativeDateLabeller.labelIntervalFunctions[this._locale];
    if (f == null) {
        f = Timeline.RelativeDateLabeller.prototype.defaultLabelInterval;
    }
    return f.call(this, date, intervalUnit);
};

Timeline.RelativeDateLabeller.prototype.labelPrecise = function(date) {
    return SimileAjax.DateTime.removeTimeZoneOffset(
        date,
        this._timeZone //+ (new Date().getTimezoneOffset() / 60)
    ).toUTCString();
};

Timeline.RelativeDateLabeller.prototype.defaultLabelInterval = function(date, intervalUnit) {
    var text;
    var emphasized = false;

    date = SimileAjax.DateTime.removeTimeZoneOffset(date, this._timeZone);

	// Compute the relative value
	var diff = date.getTime() - this._startDate.getTime();
	var diffUnit = diff / SimileAjax.DateTime.gregorianUnitLengths[intervalUnit];

    switch(intervalUnit) {
		case SimileAjax.DateTime.MILLISECOND:
			text = diffUnit + 'ms';
			break;
		case SimileAjax.DateTime.SECOND:
			text = diffUnit + 's';
			break;
		case SimileAjax.DateTime.MINUTE:
			text = diffUnit + 'm';
		case SimileAjax.DateTime.HOUR:
			text = diffUnit + 'hr';
			break;
		case SimileAjax.DateTime.DAY:
			text = diffUnit + 'd';
			break;
		case SimileAjax.DateTime.WEEK:
			text = diffUnit + 'w';
			break;
		case SimileAjax.DateTime.MONTH:
			text = diffUnit + 'm';
			break;
		case SimileAjax.DateTime.YEAR:
			text = diffUnit + 'y';
			break;
		case SimileAjax.DateTime.DECADE:
			text = diffUnit + '0y';
			break;
		case SimileAjax.DateTime.CENTURY:
			text = diffUnit + 'c';
			break;
		case SimileAjax.DateTime.MILLENNIUM:
			text = diffUnit + '0c';
			break;
		default:
			text = diffUnit;
    }
    return { text: text, emphasized: emphasized };
}


/*==================================================
 *  Relative time Ether Painter
 *==================================================
 */

Timeline.RelativeDateEtherPainter = function(params) {
    this._params = params;
    
	if( params.theme ) 
		this._theme = params.theme;

	if( params.unit )
		this._unit = params.unit;
	else
		this._unit = Timeline.DateTime.DAY;

	if( params.multiple )
		this._multiple = params.multiple;
	else
		this._multiple = 1;

	this._startDate = params.startDate;
};

Timeline.RelativeDateEtherPainter.prototype.initialize = function(band, timeline) {
    this._band = band;
    this._timeline = timeline;

    this._backgroundLayer = band.createLayerDiv(0);
    this._backgroundLayer.setAttribute("name", "ether-background"); // for debugging
    this._backgroundLayer.style.background = this._theme.ether.backgroundColors[band.getIndex()];

    this._markerLayer = null;
    this._lineLayer = null;

    var align = ("align" in this._params) ? this._params.align :
        this._theme.ether.interval.marker[timeline.isHorizontal() ? "hAlign" : "vAlign"];
    var showLine = ("showLine" in this._params) ? this._params.showLine :
        this._theme.ether.interval.line.show;

    this._intervalMarkerLayout = new Timeline.EtherIntervalMarkerLayout(
        this._timeline, this._band, this._theme, align, showLine);

    this._highlight = new Timeline.EtherHighlight(
        this._timeline, this._band, this._theme, this._backgroundLayer);
}

Timeline.RelativeDateEtherPainter.prototype.setHighlight = function(startDate, endDate) {
    this._highlight.position(startDate, endDate);
}

Timeline.RelativeDateEtherPainter.prototype.paint = function() {
    if (this._markerLayer) {
        this._band.removeLayerDiv(this._markerLayer);
    }
    this._markerLayer = this._band.createLayerDiv(100);
    this._markerLayer.setAttribute("name", "ether-markers"); // for debugging
    this._markerLayer.style.display = "none";

    if (this._lineLayer) {
        this._band.removeLayerDiv(this._lineLayer);
    }

    this._lineLayer = this._band.createLayerDiv(1);
    this._lineLayer.setAttribute("name", "ether-lines"); // for debugging
    this._lineLayer.style.display = "none";

    var minDate = this._band.getMinDate();
    var maxDate = this._band.getMaxDate();

    var timeZone = this._band.getTimeZone();
    var labeller = this._band.getLabeller();

	// Compute the position to start (should be on a multiple of the chosen unit from the
	// mindate (e.g. at three days from the start date)
	var diff = minDate.getTime() - this._startDate.getTime();
	minDate.setTime(minDate.getTime() - ( diff % ( this._multiple * SimileAjax.DateTime.gregorianUnitLengths[this._unit] ) ) );

    var p = this;
    var incrementDate = function(date) {
        for (var i = 0; i < p._multiple; i++) {
            SimileAjax.DateTime.incrementByInterval(date, p._unit);
        }
    };

    while (minDate.getTime() < maxDate.getTime()) {
        this._intervalMarkerLayout.createIntervalMarker(
            minDate, labeller, this._unit, this._markerLayer, this._lineLayer);

        incrementDate(minDate);
    }

    this._markerLayer.style.display = "block";
    this._lineLayer.style.display = "block";
};

Timeline.RelativeDateEtherPainter.prototype.softPaint = function() {
};
