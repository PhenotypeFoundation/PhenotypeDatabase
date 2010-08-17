
<%@ page import="dbnp.studycapturing.Study" %>
<%@ page import="dbnp.studycapturing.EventGroup" %>
<%@ page import="dbnp.studycapturing.RelTime" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
    <script type="text/javascript">
      // Flag whether the timelines have been loaded
      var timelineloaded = false;
      
      // Number of timelines that should be loaded
      var numTimelines = ${studyList?.size()};
      
      // This method is called on the event body.onLoad
      $(function() {
              $("#tabs").tabs({
                show: function(event, ui) {
                  // If the events tab is shown, the timeline should be redrawn
                  if( ui.tab.hash == '#events-timeline' && !timelineloaded ) {
                    loadTimeline( 'eventstimeline', 'eventtitles', 0 );
                    timelineloaded = true;
                  }
                }
              });
      });
    </script>
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'studies.css')}"/>

    <!-- Include scripts for the SIMILE timeline. See http://simile-widgets.org/wiki/ -->
    <script type="text/javascript">
      Timeline_ajax_url="${resource(dir: 'js', file: 'timeline-simile/timeline_ajax/simile-ajax-api.js')}";
      Timeline_urlPrefix='${resource(dir: 'js', file: 'timeline-simile/')}';
      Timeline_parameters='bundle=true';
    </script>
    <script src="${resource(dir: 'js', file: 'timeline-simile/timeline-api.js')}" type="text/javascript"></script>
    <script src="${resource(dir: 'js', file: 'timeline-simile/custom-timeline.js')}" type="text/javascript"></script>
    <script src="${resource(dir: 'js', file: 'jquery-callback-1.2.js')}" type="text/javascript"></script>

    <!-- Create the JSON objects for the timeline with events -->
    <script type="text/javascript">
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
          var emptyEtherPainter = new Timeline.EmptyEtherPainter( { theme: theme } )

          // Now create the bands for all studies, and add them to one timeline
          // Multiple timeline on one page do not seem to work
          <g:set var="bandNr" value="${0}" />
          <g:each in="${studyList}" var="study" status="timelineNr">
              // The date that the timeline should start on
              var dateStr = "<g:formatDate format="yyyy/MM/dd HH:mm:ss" date="${study.startDate}"/>";
              firstDate = new Date ( dateStr );

              //------------- Eventgroup overview ---------------

              <g:set var="datesBandNr" value="${bandNr}" />
              // Add an empty band to show the dates
              bandInfos[${bandNr}] =
                     Timeline.createBandInfo({
                        width:          40,
                        intervalUnit:   Timeline.DateTime.DAY,
                        intervalPixels: 40,
                        showEventText:  false,
                        date:           firstDate,
                        timeZone:       +1,
                        layout:         'original',
                        theme:          theme
                     });
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
                           intervalPixels: 40,
                           date:           firstDate,
                           timeZone:       +1,
                           syncWith:       1,
                           layout:         'original',
                           theme:          theme
                       });

                // Make sure the date isn't printed by using the empty ether painter
                bandInfos[${bandNr}].etherPainter = emptyEtherPainter;

                // Add a title to the bandinfo
                <% 
                  sortedGroupSubjects = eventGroup.subjects.sort( { a, b -> a.name <=> b.name } as Comparator );
                  def simpleSubjects = sortedGroupSubjects.name.join( ', ' );

                  // We can only show appr. 30 characters per line and as many lines as there are events
                  def charsPerLine = 30;
                  def numEvents = eventGroup.events?.size();
                  def maxChars = numEvents * charsPerLine;

                  // If the subjects will fit, show them all
                  if( simpleSubjects?.size() < maxChars ) {
                    showSubjects = simpleSubjects;
                  } else {
                    // Always add the first name
                    def subjectNames = sortedGroupSubjects[0]?.name;

                    // Continue adding names until the length is to long
                    id = 0;
                    sortedGroupSubjects.each { subject ->
                      if( id > 0 ) {
                        println( "ID: " + id + " - " + subjectNames?.size() + " - " + subject.name?.size() + " - " + maxChars );
                        if( subjectNames?.size() + subject.name?.size() < maxChars - 15 ) {
                          subjectNames += ", " + subject.name;
                        } else {
                          return;
                        }
                      }
                      id++;
                    }

                    // Add a postfix
                    subjectNames += " and " + ( sortedGroupSubjects?.size() - id ) + " more";

                    showSubjects = subjectNames;
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
     </script>
</head>
<body>

  <div class="body" id="studies">
    <h1><g:message code="default.show.label" args="[entityName]" /></h1>
    <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>
    <div class="dialog">
      <div id="tabs">
        <ul>
          <li><a href="#study">Study Information</a></li>
          <li><a href="#subjects">Subjects</a></li>
          <li><a href="#events-timeline">Events timeline</a></li>
          <li><a href="#events-table">Events table</a></li>
          <li><a href="#assays">Assays</a></li>
          <li><a href="#samples">Samples</a></li>
          <li><a href="#persons">Persons</a></li>
          <li><a href="#publications">Publications</a></li>
        </ul>

        <div id="study">
          <table>
            <!-- only show the head section if there are multiple studies -->
            <g:if test="${multipleStudies}">
              <thead>
                <tr>
                  <th></th>
                  <g:each in="${studyList}" var="studyInstance">
                    <th>${studyInstance.title}</th>
                  </g:each>
                </tr>
              </thead>
            </g:if>
            <% 
              // Determine a union of the fields from all studies, in order
              // to show a proper list. We want every field to appear just once,
              // so the list is filtered for unique values
              studyFields = studyList[0].giveDomainFields() + studyList*.giveTemplateFields()?.flatten().unique()
            %>
            <!-- Show all template and domain fields, if filled -->
            <g:each in="${studyFields}" var="field">
              <%
                // If a value is not set for any of the selected studies, the
                // field should not appear in the list
                showField = true in studyList.collect { it.fieldExists( field.name ) && it.getFieldValue( field.name ) != null }?.flatten()
              %>
              <g:if test="${showField}">
                <tr>
                  <td>${field}</td>
                  <g:each in="${studyList}" var="studyInstance">
                    <td>${studyInstance.getFieldValue(field.name)}</td>
                  </g:each>
                </tr>
              </g:if>
            </g:each>

            <!-- Add some extra fields -->
            <tr>
              <td>Events</td>
              <g:each in="${studyList}" var="studyInstance">
                <td>
                  <g:if test="${studyInstance.giveEventTemplates()?.size()==0}">
                    -
                  </g:if>
                  <g:else>
                   ${studyInstance.giveEventTemplates().name.join(", ")}
                  </g:else>
                </td>
              </g:each>
            </tr>
            <tr>
              <td>Sampling events</td>
              <g:each in="${studyList}" var="studyInstance">
                <td>
                  <g:if test="${studyInstance.giveSamplingEventTemplates()?.size()==0}">
                    -
                  </g:if>
                  <g:else>
                   ${studyInstance.giveSamplingEventTemplates().name.join(", ")}
                  </g:else>
                </td>
              </g:each>
            </tr>
            <tr>
              <td>Readers</td>
              <g:each in="${studyList}" var="studyInstance">
                <td>
                  <g:if test="${studyInstance.readers?.size()==0}">
                    -
                  </g:if>
                  <g:else>
                    <g:each in="${studyInstance.readers}" var="r" status="i">
                      <g:if test="${i > 0}">, </g:if>
                      <g:link controller="user" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link>
                    </g:each>
                  </g:else>
                </td>
              </g:each>
            </tr>
            <tr>
              <td>Editors</td>
              <g:each in="${studyList}" var="studyInstance">
                <td>
                  <g:if test="${studyInstance.editors?.size()==0}">
                    -
                  </g:if>
                  <g:else>
                    <g:each in="${studyInstance.editors}" var="r" status="i">
                      <g:if test="${i > 0}">, </g:if>
                      <g:link controller="user" action="show" id="${r.id}">${r?.encodeAsHTML()}</g:link>
                    </g:each>
                  </g:else>
                </td>
              </g:each>
            </tr>

          </table>
        </div>

        <div id="subjects">

          <g:if test="${studyList*.subjects?.flatten()?.size()==0}">
            No subjects in the selected studies
          </g:if>
          <g:else>
            <table>
              <thead>
                <tr>
                  <g:if test="${multipleStudies}">
                    <th></th>
                  </g:if>
                  <g:each in="${new dbnp.studycapturing.Subject().giveDomainFields()}" var="field">
                    <th>${field}</th>
                  </g:each>

                  <%
                    // Determine a union of the fields for all different
                    // subjects in all studies. In order to show a proper list. 
                    // We want every field to appear just once,
                    // so the list is filtered for unique values
                    subjectTemplates = studyList*.giveSubjectTemplates()?.flatten().unique()
                    if( !subjectTemplates ) {
                      subjectTemplates = [];
                      subjectFields = [];
                    } else {
                      subjectFields = subjectTemplates*.fields?.flatten().unique()
                      if( !subjectFields ) {
                        subjectFields = [];
                      }
                    }

                    /*
                     * These lines are rewritten because
                     * performance sucked
                     *
                     *   // These took about 9 seconds (for 31 subjects and
                     *   allSubjects = studyList*.subjects?.flatten()
                     *
                     *   subjectFields = subjectFields.findAll { subjectField ->
                     *     ( true in allSubjects.collect { subject -> subject.fieldExists( subjectField.name ) && subject.getFieldValue( subjectField.name ) != null }.flatten() )
                     *   }
                     */

                    // Filter out all fields that are left blank for all subjects
                    allSubjects = studyList*.subjects?.flatten()

                    showSubjectFields = []
                    subjectFields.each { subjectField ->
                      for( subject in allSubjects )
                      {
                        // If the field is filled for this subject, we have to
                        // show the field and should not check any other
                        // subjects (hence the break)
                        if( subject.fieldExists( subjectField.name ) && subject.getFieldValue( subjectField.name ) ) {
                          showSubjectFields << subjectField;
                          break;
                        }
                      }
                    }
                  %>

                  <g:each in="${showSubjectFields}" var="field">
                    <th>${field}</th>
                  </g:each>

                </tr>
              </thead>

              <g:set var="i" value="${1}" />

              <g:each in="${studyList}" var="studyInstance">
                <%
                  // Sort subjects by name
                  subjects = studyInstance.subjects;
                  sortedSubjects = subjects.sort( { a, b -> a.name <=> b.name } as Comparator )
                %>

                <g:each in="${sortedSubjects}" var="subject" status="j">
                  <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <g:if test="${multipleStudies && j==0}">
                      <td class="studytitle" rowspan="${sortedSubjects?.size()}">
                        ${studyInstance.title}
                      </td>
                    </g:if>
                    <g:each in="${subject.giveDomainFields()}" var="field">
                      <td>${subject.getFieldValue(field.name)}</td>
                    </g:each>
                  
                    <g:each in="${showSubjectFields}" var="field">
                      <td>
                        <g:if test="${subject.fieldExists(field.name)}">
                          ${subject.getFieldValue(field.name)}
                        </g:if>
                        <g:else>
                          N/A
                        </g:else>
                      </td>
                    </g:each>

                  </tr>
                  <g:set var="i" value="${i + 1}" />
                </g:each>
              </g:each>
            </table>
          </g:else>
        </div>

        <div id="events-timeline">
          <g:if test="${studyList*.events?.flatten()?.size()==0 && studyInstance*.samplingEvents?.flatten()?.size()==0 }">
            No events in these studies
          </g:if>
          <g:else>
            <g:each in="${studyList}" var="study" status="i">
              <div style="margin: 10px; ">
                <div class="eventtitles" id="eventtitles-${i}"></div>
                <div class="eventstimeline" id="eventstimeline-${i}"></div>
              </div>
            </g:each>
            <noscript>
              <table>
                <thead>
                  <tr>
                    <g:if test="${multipleStudies}">
                      <th></th>
                    </g:if>
                    <th>Start time</th>
                    <th>Duration</th>
                    <th>Type</th>
                    <th>Sampling event</th>
                    <th>Parameters</th>
                  </tr>
                </thead>

                <g:set var="i" value="${1}" />

                <g:each in="${studyList}" var="studyInstance">
                  <%
                    // Sort events by starttime and duration
                    events = studyInstance.events + studyInstance.samplingEvents;
                    sortedEvents = events.sort( { a, b ->
                          //a.startTime == b.startTime ?
                            //a.getDuration().getValue() <=> b.getDuration().getValue() :
                          a.startTime <=> b.startTime
                      } as Comparator )
                  %>

                  <g:each in="${sortedEvents}" var="event" status="j">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                      <g:if test="${multipleStudies && j==0}">
                        <td class="studytitle" rowspan="${sortedEvents?.size()}">
                          ${studyInstance.title}
                        </td>
                      </g:if>
                      <td>${event.getStartTimeString()}</td>
                      <td>${((event.getClass() == 'Event') ? event.getDurationString() : '')}</td>
                      <td>${event.template.name}</td>
                      <td>
                        <g:if test="${event instanceof dbnp.studycapturing.SamplingEvent}">
                          <g:checkBox name="samplingEvent" disabled="${true}" value="${true}"/>
                        </g:if>
                        <g:else>
                          <g:checkBox name="event" disabled="${true}" value="${false}" />
                        </g:else>
                      </td>
                      <td>
                        <g:set var="fieldCounter" value="${1}" />
                        <g:each in="${event.giveTemplateFields()}" var="field">
                          <g:if test="${event.getFieldValue(field.name)}">
                            <g:if test="${fieldCounter > 1}">, </g:if>
                              ${field.name} = ${event.getFieldValue( field.name )}
                            <g:set var="fieldCounter" value="${fieldCounter + 1}" />
                          </g:if>
                        </g:each>
                      </td>
                    </tr>

                    <g:set var="i" value="${i + 1}" />
                  </g:each>
                </g:each>
              </table>

            </noscript>

          </g:else>
        </div>

        <div id="events-table">
          <g:if test="${studyList*.eventGroups?.flatten()?.size()==0}">
            No event groups in this study
          </g:if>
          <g:else>
            <%
              // Determine a union of the event templates for all different
              // eventgroups in all studies, in order to show a proper list.
              // We want every field to appear just once,
              // so the list is filtered for unique values
              groupTemplates = studyList*.giveAllEventTemplates()?.flatten().unique()
            %>
            <table>
              <thead>
                <tr>
                  <g:if test="${multipleStudies}">
                    <th></th>
                  </g:if>
                  <th>Name</th>
                  <th colspan="${groupTemplates?.size()}">Events</th>
                  <th>Subjects</th>
                </tr>
                <tr>
                  <g:if test="${multipleStudies}">
                    <th></th>
                  </g:if>
                  <th></th>
                  <g:each in="${groupTemplates}" var="eventTemplate">
                    <th>${eventTemplate.name}</th>
                  </g:each>
                  <th></th>
                </tr>
              </thead>

              <g:set var="i" value="${1}" />

              <g:each in="${studyList}" var="studyInstance">
                <%
                  def sortedEventGroups = studyInstance.eventGroups.sort( { a, b ->
                      return a.name <=> b.name;
                  }  as Comparator );

                  def orphans = studyInstance.getOrphanEvents();
                  if( orphans?.size() > 0 ) {
                    sortedEventGroups.add( new EventGroup(
                      id: -1,
                      name: 'No group',
                      events: orphans,
                      subjects: []
                    ));
                  }

                %>
                <g:each in="${sortedEventGroups}" var="eventGroup" status="j">
                  <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <g:if test="${multipleStudies && j==0}">
                      <td class="studytitle" rowspan="${sortedEventGroups?.size()}">
                        ${studyInstance.title}
                      </td>
                    </g:if>
                    <td>${eventGroup.name}</td>

                    <g:each in="${groupTemplates}" var="currentEventTemplate">
                      <td>
                        <g:each in="${eventGroup.events}" var="event">
                          <g:if test="${event.template.name==currentEventTemplate.name}">

                            <g:set var="fieldCounter" value="${1}" />
                            <g:each in="${event.giveTemplateFields()}" var="field">
                              <g:if test="${event.getFieldValue(field.name)}">
                                <g:if test="${fieldCounter > 1}">, </g:if>
                                  ${field.name} = ${event.getFieldValue( field.name )}
                                <g:set var="fieldCounter" value="${fieldCounter + 1}" />
                              </g:if>
                            </g:each>
                          </g:if>
                        </g:each>
                      </td>
                    </g:each>
                    <td>
                      <% sortedGroupSubjects = eventGroup.subjects.sort( { a, b -> a.name <=> b.name } as Comparator )  %>
                      ${sortedGroupSubjects.name.join( ', ' )}
                    </td>
                  </tr>

                  <g:set var="i" value="${i + 1}" />
                </g:each>

              </g:each>

            </table>
          </g:else>
        </div>

        <div id="assays">
          <g:if test="${studyList*.assays?.flatten()?.size()==0}">
            No assays in these studies
          </g:if>
          <g:else>
            <table>
              <thead>
                <tr>
                  <g:if test="${multipleStudies}">
                    <th></th>
                  </g:if>
                  <th width="100">Assay Name</th>
                  <th width="100">Module</th>
                  <th>Type</th>
                  <th width="150">Platform</th>
                  <th>Url</th>
                  <th>Samples</th>
                </tr>
              </thead>
              <g:set var="i" value="${1}" />

              <g:each in="${studyList}" var="studyInstance">
                <g:each in="${studyInstance.assays}" var="assay" status="j">
                  <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <g:if test="${multipleStudies && j==0}">
                      <td class="studytitle" rowspan="${studyInstance.assays?.size()}">
                        ${studyInstance.title}
                      </td>
                    </g:if>
                    <td>${assay.name}</td>
                    <td>${assay.module.name}</td>
                    <td>${assay.module.platform}</td>
                    <td>${assay.module.url}</td>
                    <td>
                      <% sortedAssaySamples = assay.samples.sort( { a, b -> a.name <=> b.name } as Comparator )  %>
                      ${sortedAssaySamples.name.join( ', ' )}
                    </td>
                  </tr>
                  <g:set var="i" value="${i + 1}" />

                </g:each>
              </g:each>
            </table>
          </g:else>
        </div>

        <div id="samples">

          <g:if test="${studyList*.samples.flatten()?.size()==0}">
            No samples in the selected studies
          </g:if>
          <g:else>
            <table>
              <thead>
                <tr>
                  <g:if test="${multipleStudies}">
                    <th></th>
                  </g:if>
                  <g:each in="${new dbnp.studycapturing.Sample().giveDomainFields()}" var="field">
                    <th>${field}</th>
                  </g:each>

                  <%
                    // Determine a union of the fields for all different
                    // samples in all studies. In order to show a proper list.
                    // We want every field to appear just once,
                    // so the list is filtered for unique values
                    sampleTemplates = studyList*.giveSampleTemplates().flatten().unique()

                    if( !sampleTemplates ) {
                      sampleTemplates = [];
                      sampleFields = [];
                      showSampleFields = [];
                    } else {
                      sampleFields = sampleTemplates*.fields.flatten().unique()
                      if( !sampleFields ) {
                        sampleFields = [];
                        showSampleFields = [];
                      } else {
                        // Filter out all fields that are left blank for all samples
                        allSamples = studyList*.samples.flatten()

                        showSampleFields = [];
                        sampleFields.each { sampleField ->
                          for( sample in allSamples )
                          {
                            // If the field is filled for this subject, we have to
                            // show the field and should not check any other
                            // samples (hence the break)
                            if( sample.fieldExists( sampleField.name ) && sample.getFieldValue( sampleField.name ) ) {
                              showSampleFields << sampleField;
                              break;
                            }
                          }
                        }
                      }
                    }
                  %>

                  <g:each in="${showSampleFields}" var="field">
                    <th>${field}</th>
                  </g:each>

                </tr>
              </thead>

              <g:set var="i" value="${1}" />

              <g:each in="${studyList}" var="studyInstance">
                <%
                  // Sort samples by name
                  samples = studyInstance.samples;
                  sortedSamples = samples.sort( { a, b -> a.name <=> b.name } as Comparator )
                %>

                <g:each in="${sortedSamples}" var="sample" status="j">
                  <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <g:if test="${multipleStudies && j==0}">
                      <td class="studytitle" rowspan="${sortedSamples?.size()}">
                        ${studyInstance.title}
                      </td>
                    </g:if>
                    <g:each in="${sample.giveDomainFields()}" var="field">
                      <td>${sample.getFieldValue(field.name)}</td>
                    </g:each>

                    <g:each in="${showSampleFields}" var="field">
                      <td>
                        <g:if test="${sample.fieldExists(field.name)}">
                          ${sample.getFieldValue(field.name)}
                        </g:if>
                        <g:else>
                          N/A
                        </g:else>
                      </td>
                    </g:each>

                  </tr>
                  <g:set var="i" value="${i + 1}" />
                </g:each>
              </g:each>

            </table>
          </g:else>
        </div>

        <div id="persons">
          <%
            // Determine a list of all persons
            allPersons = studyList*.persons*.person.flatten().unique()
          %>
          <g:if test="${allPersons?.size()==0}">
            No persons involved in these studies
          </g:if>
          <g:else>
            <table>
              <tr>
                <thead>
                  <th>Name</th>
                  <th>Affiliations</th>
                  <th>Phone</th>
                  <th>Email</th>
                  <g:if test="${multipleStudies}">
                    <g:each in="${studyList}" var="studyInstance">
                      <th>${studyInstance.title}</th>
                    </g:each>
                  </g:if>
                  <g:else>
                    <th>Role</th>
                  </g:else>
                </thead>
              </tr>
              <g:each in="${allPersons}" var="person" status="i">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                  <td>${person.firstName} ${person.prefix} ${person.lastName}</td>
                  <td>
                    ${person.affiliations.join(', ')}
                  </td>
                  <td>${person.phone}</td>
                  <td>${person.email}</td>
                  <g:each in="${studyList}" var="studyInstance">
                    <%
                      studyperson = studyInstance.persons.find { it.person == person }
                    %>
                    <td>
                      <g:if test="${studyperson}">
                        ${studyperson.role.name}
                      </g:if>
                     </td>
                  </g:each>

                </tr>
              </g:each>
            </table>
          </g:else>
        </div>

        <div id="publications">
          <%
            // Determine a list of all persons
            allPublications = studyList*.publications.flatten().unique()
          %>
          <g:if test="${allPublications?.size()==0}">
            No publications attached to these studies
          </g:if>
          <g:else>
            <table>
              <tr>
                <thead>
                  <th>Title</th>
                  <th>Authors</th>
                  <th>Comments</th>

                  <g:if test="${multipleStudies}">
                    <g:each in="${studyList}" var="studyInstance">
                      <th>${studyInstance.title}</th>
                    </g:each>
                  </g:if>
                </thead>
              </tr>
              <g:each in="${allPublications}" var="publication" status="i">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                  <td>${publication.title}</td>
                  <td>
                    ${publication.authorsList}
                  </td>
                  <td>${publication.comments}</td>
                  <g:if test="${multipleStudies}">
                    <g:each in="${studyList}" var="studyInstance">
                      <td>
                        <g:if test="${publication in studyInstance.publications}">
                          x
                        </g:if>
                      </td>
                    </g:each>
                  </g:if>
                </tr>
              </g:each>
            </table>
          </g:else>
        </div>

      </div>
    </div>
    <br>
    <div class="buttons">
      <g:form>
        <g:if test="${studyList?.size() == 1}">
          <g:set var="studyInstance" value="${studyList[0]}" />
          <g:hiddenField name="id" value="${studyInstance?.id}" />
          <span class="button"><g:link class="edit" controller="wizard" params="[jump:'edit']" id="${studyInstance?.id}">${message(code: 'default.button.edit.label', default: 'Edit')}</g:link></span>
          <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
        </g:if>
        <span class="button"><g:link class="backToList" action="list">Back to list</g:link></span>
      </g:form>
    </div>

  </div>
</body>
</html>
