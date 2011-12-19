<% response.contentType = "text/javascript" %>
<%@ page import="dbnp.studycapturing.EventGroup" %>
<%@ page import="dbnp.studycapturing.Subject" %>
/*
  * Creates timeline bands for displaying different timelines
  *
  * @returns array with BandInfo objects, as described on http://simile-widgets.org/wiki/Timeline_GettingStarted
  */
function createTimelineBands( timelineNr ) {
  var bandInfos = [];
  var eventSources = [];
  var overviewEventSource = new Timeline.DefaultEventSource();

  // The way the timeline should look. See http://www.linuxjournal.com/article/9301
  var theme = Timeline.ClassicTheme.create();
  //theme.mouseWheel = 'zoom';  // Code needed for zooming
  var emptyEtherPainter = new Timeline.EmptyEtherPainter( { theme: theme } )

  // Now create the bands for all studies, and add them to one timeline
  // Multiple timelines on one page do not seem to work
  <g:set var="bandNr" value="${0}" />
  <g:each in="${studyList}" var="study" status="timelineNr">

	// The date that the timeline should start and finish on
	<%
	    def dateMap = study.getMinMaxEventDate();
	%>
	var firstDate = new Date ( "<g:formatDate format="yyyy/MM/dd HH:mm:ss" date="${dateMap.minDate}"/>" );
	var lastDate = new Date ( "<g:formatDate format="yyyy/MM/dd HH:mm:ss" date="${dateMap.maxDate}"/>" );

    // Calculate number of pixels per interval
    var intTotalWidth = jQuery(".eventstimeline").innerWidth();

    var intDays = days_between(firstDate,lastDate);

    var meanDay = days_mean(firstDate,lastDate);

    if(intDays<=0) {
        intDays = 1;
    }

    var intIntervalPixels = Math.floor(intTotalWidth/intDays);

    // Make sure the header is displaying weeks if there is to little space for days
    var objUnit = Timeline.DateTime.DAY;
    if(intIntervalPixels<20) {
        objUnit = Timeline.DateTime.WEEK;
    }

	//------------- Eventgroup overview ---------------
	<g:set var="datesBandNr" value="${bandNr}" />
	// Add an empty band to show the dates
	bandInfos[${bandNr}] =
		   Timeline.createBandInfo({
			  width:          40,
			  intervalUnit:   Timeline.DateTime.DAY,
			  intervalPixels: intIntervalPixels,
			  showEventText:  false,
			  date:           meanDay,
			  timeZone:       +1,
			  layout:         'original',
			  theme:          theme/*, // Code needed for zooming
			  zoomIndex:      10,
              zoomSteps:      new Array(
                  {pixelsPerInterval: 280,  unit: Timeline.DateTime.HOUR},
                  {pixelsPerInterval: 140,  unit: Timeline.DateTime.HOUR},
                  {pixelsPerInterval:  70,  unit: Timeline.DateTime.HOUR},
                  {pixelsPerInterval:  35,  unit: Timeline.DateTime.HOUR},
                  {pixelsPerInterval: 400,  unit: Timeline.DateTime.DAY},
                  {pixelsPerInterval: 200,  unit: Timeline.DateTime.DAY},
                  {pixelsPerInterval: 100,  unit: Timeline.DateTime.DAY},
                  {pixelsPerInterval:  50,  unit: Timeline.DateTime.DAY},
                  {pixelsPerInterval: 400,  unit: Timeline.DateTime.MONTH},
                  {pixelsPerInterval: 200,  unit: Timeline.DateTime.MONTH},
                  {pixelsPerInterval: 100,  unit: Timeline.DateTime.MONTH} // DEFAULT zoomIndex
              )*/
		   });

	// Make sure the date is printed using the relative time
	bandInfos[${bandNr}].etherPainter = new Timeline.RelativeDateEtherPainter( { theme: theme, startDate: firstDate, unit: objUnit } );
	bandInfos[${bandNr}].labeller = new Timeline.RelativeDateLabeller( "en", 0, firstDate );

	bandTitleInfo[ timelineNr ][ ${bandNr} ] = {
	  title: "${study.title}",
	  subjects: "",
	  className: "studytitle"
	};

	<g:set var="bandNr" value="${bandNr+1}" />
	<%
	  def sortedEventGroups = study.eventGroups.sort( { a, b ->
		  return a.name <=> b.name;
	  }  as Comparator );

	  def orphans = study.getOrphanEvents();
	  if( orphans?.size() > 0 ) {
		sortedEventGroups.add( new EventGroup(
		  id: -1,
		  name: 'No group',
		  events: orphans,
		  subjects: []
		));
	  }

	%>
	<g:each in="${sortedEventGroups}" var="eventGroup" status="i">
	  //------------- Eventgroup ${bandNr} ---------------

	  // Create an eventsource for all events
	  eventSources[${bandNr}] = new Timeline.DefaultEventSource();

	  // Load events for this eventsource (using jquery)
	  var event_url = '${createLink(controller:'study', action:'events', id:( eventGroup.id ? eventGroup.id : -1 ), params: [ startDate: study.startDate.getTime(), study: study.id ])}';
	  $.getJSON(event_url, $.callback( _loadJSONEvents, [0, ${bandNr}, eventSources[${bandNr}], overviewEventSource, event_url] ) );

	  // Create a new timeline band
	  bandInfos[${bandNr}] =
			 Timeline.createBandInfo({
				 eventSource:    eventSources[${bandNr}],
				 width:          30,
				 intervalUnit:   Timeline.DateTime.DAY,
				 intervalPixels: intIntervalPixels,
				 date:           meanDay,
				 timeZone:       +1,
				 syncWith:       1,
				 layout:         'original',
				 theme:          theme/*,  // Code needed for zooming
                 zoomIndex:      10,
                 zoomSteps:      new Array(
                     {pixelsPerInterval: 280,  unit: Timeline.DateTime.HOUR},
                     {pixelsPerInterval: 140,  unit: Timeline.DateTime.HOUR},
                     {pixelsPerInterval:  70,  unit: Timeline.DateTime.HOUR},
                     {pixelsPerInterval:  35,  unit: Timeline.DateTime.HOUR},
                     {pixelsPerInterval: 400,  unit: Timeline.DateTime.DAY},
                     {pixelsPerInterval: 200,  unit: Timeline.DateTime.DAY},
                     {pixelsPerInterval: 100,  unit: Timeline.DateTime.DAY},
                     {pixelsPerInterval:  50,  unit: Timeline.DateTime.DAY},
                     {pixelsPerInterval: 400,  unit: Timeline.DateTime.MONTH},
                     {pixelsPerInterval: 200,  unit: Timeline.DateTime.MONTH},
                     {pixelsPerInterval: 100,  unit: Timeline.DateTime.MONTH} // DEFAULT zoomIndex
                 )*/
			 });

	  // Make sure the date isn't printed by using the empty ether painter
	  bandInfos[${bandNr}].etherPainter = emptyEtherPainter;

	  // Add a title to the bandinfo
	  <%
		if (eventGroup.subjects) {
			ArrayList<Subject> sortedGroupSubjects = eventGroup.subjects.sort( { a, b -> a.name <=> b.name } as Comparator );

			// We can only show appr. 30 characters per line and as many lines as there are events
			def charsPerLine = 40;
			def numEvents = eventGroup.events?.size() + eventGroup.samplingEvents?.size();
			Integer maxChars = new Integer( numEvents * charsPerLine );

			showSubjects = Subject.trimSubjectNames( sortedGroupSubjects, maxChars );
		} else {
			showSubjects = ''
		}
	  %>

	  bandTitleInfo[ timelineNr ][ ${bandNr} ] = {
		title: "${eventGroup.name}",
		className: "<g:if test="${ eventGroup.id == -1 || !eventGroup.id  }">no_group</g:if>",
		subjects: "${showSubjects}"
	  };

	  <g:set var="bandNr" value="${bandNr+1}" />
	</g:each>

	// Synchronize all bands
	<g:each in="${sortedEventGroups}" var="eventGroup" status="i">
	  bandInfos[${i + datesBandNr +1}].syncWith = ${datesBandNr};
	</g:each>

  </g:each>

  return bandInfos;
}

// http://www.mcfedries.com/javascript/daysbetween.asp
function days_between(date1, date2) {

    // The number of milliseconds in one day
    var ONE_DAY = 1000 * 60 * 60 * 24

    // Convert both dates to milliseconds
    var date1_ms = date1.getTime()
    var date2_ms = date2.getTime()

    // Calculate the difference in milliseconds
    var difference_ms = Math.abs(date1_ms - date2_ms)

    // Convert back to days and return
    return Math.round(difference_ms/ONE_DAY)

}

function days_mean(date1, date2) {
    // Convert both dates to milliseconds
    var date1_ms = date1.getTime()
    var date2_ms = date2.getTime()

    // Mean date
    var date_mean = (date1_ms+date2_ms)/2;
    date_mean = Math.floor(date_mean);

    // Convert back to Date()
    return new Date(date_mean);
}
