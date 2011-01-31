<%@ page import="dbnp.studycapturing.*" %>
<%@ page import="org.dbnp.gdt.*" %>
<html>
<head>
	<meta name="layout" content="main"/>
	<g:if test="${studyList.size() == 1}">
	<meta property="og:title" content="${studyList[0].title}"/>
	<meta property="og:description" content="${(studyList[0].getFieldValue('description')) ? studyList[0].getFieldValue('description') : 'A study in the Generic Study Capture Framework'}"/>
	</g:if>
	<g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}"/>
	<title><g:message code="default.show.label" args="[entityName]"/></title>
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
                },
				ajaxOptions: {
					error: function(xhr, status, index, anchor) {
						$(anchor.hash).html(
							"Couldn't load this tab. We'll try to fix this as soon as possible.");
					}
				}
			});
		});

		// Parameters for the SIMILE timeline
		Timeline_ajax_url = "${resource(dir: 'js', file: 'timeline-simile/timeline_ajax/simile-ajax-api.js')}";
		Timeline_urlPrefix = '${resource(dir: 'js', file: 'timeline-simile/')}';
		Timeline_parameters = 'bundle=true';

	</script>
	<link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'studies.css')}"/>

	<!-- Include scripts for the SIMILE timeline. See http://simile-widgets.org/wiki/ -->
	<script src="${resource(dir: 'js', file: 'timeline-simile/timeline-api.js')}" type="text/javascript"></script>
	<script src="${resource(dir: 'js', file: 'timeline-simile/custom-timeline.js')}" type="text/javascript"></script>
	<script src="${resource(dir: 'js', file: 'timeline-simile/relative-time.js')}" type="text/javascript"></script>
	<script src="${resource(dir: 'js', file: 'jquery-callback-1.2.js')}" type="text/javascript"></script>

	<!-- Create the JSON objects for the timeline with events -->
	<script type="text/javascript" src="<g:createLink action="createTimelineBandsJs" id="${studyList.id.join(',')}"/>" type="text/javascript"></script>
</head>
<body>

<div class="body" id="studies">
	<h1><g:message code="default.show.label" args="[entityName]"/></h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div class="dialog">
		<div id="tabs">
			<ul>
				<li><a href="#study">Study Information</a></li>
				<li><a href="<g:createLink action="show_subjects" id="${studyList.id.join(',')}"/>" title="Subjects"><span>Subjects</span></a></li>
				<li><a href="#events-timeline"><span>Events timeline</span></a></li>
				<li><a href="<g:createLink action="show_events_table" id="${studyList.id.join(',')}"/>" title="Events table"><span>Events table</span></a></li>
				<li><a href="<g:createLink action="show_assays" id="${studyList.id.join(',')}"/>" title="Assays"><span>Assays</span></a></li>
				<li><a href="<g:createLink action="show_samples" id="${studyList.id.join(',')}"/>" title="Samples"><span>Samples</span></a></li>
				<li><a href="<g:createLink action="show_persons" id="${studyList.id.join(',')}"/>" title="Persons"><span>Persons</span></a></li>
				<li><a href="<g:createLink action="show_publications" id="${studyList.id.join(',')}"/>" title="Publications"><span>Publications</span></a></li>
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
						<g:if test="${field && field?.isFilledInList( studyList )}">
							<tr>
								<td>${field}</td>
								<g:each in="${studyList}" var="studyInstance">
									<td>
										<g:if test="${studyInstance.fieldExists(field.name)}">
											<wizard:showTemplateField field="${field}" entity="${studyInstance}"/>
										</g:if>
										<g:else>
											-
										</g:else>
									</td>
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
						<td>Public</td>
						<g:each in="${studyList}" var="studyInstance">
							<td>
								${studyInstance.publicstudy}
							</td>
						</g:each>
					</tr>
					<tr>
						<td>Owner</td>
						<g:each in="${studyList}" var="studyInstance">
							<td>
								${studyInstance.owner?.username}
							</td>
						</g:each>
					</tr>
					<tr>
						<td>Readers</td>
						<g:each in="${studyList}" var="studyInstance">
							<td>
								<g:if test="${studyInstance.readers.size() == 0}">
									-
								</g:if>
								<g:else>
									${studyInstance.readers.username.join(", ")}
								</g:else>
							</td>
						</g:each>
					</tr>
					<tr>
						<td>Writers</td>
						<g:each in="${studyList}" var="studyInstance">
							<td>
								<g:if test="${studyInstance.writers?.size()==0}">
									-
								</g:if>
								<g:else>
									${studyInstance.writers.username.join(", ")}
								</g:else>
							</td>
						</g:each>
					</tr>
				</table>
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
				Javascript is needed for showing the timeline, but it has been disabled in your browser. Please enable javascript or use
				the events table instead.
			  </noscript>
			</g:else>
		  </div>

			<% /*
		  All other tabs are moved to separate views and are loaded using
		  ajax calls when a tab is opened. See http://jqueryui.com/demos/tabs/#ajax
		*/ %>

		</div>
	</div>
	<br>
	<div class="buttons">
		<g:form action="delete">
			<g:if test="${studyList?.size() == 1}">
				<g:set var="studyInstance" value="${studyList[0]}"/>
				<g:hiddenField name="id" value="${studyInstance?.id}"/>
				<g:if test="${studyInstance.canWrite(loggedInUser)}">
					<span class="button"><g:link class="edit" controller="studyWizard" params="[jump:'edit']" id="${studyInstance?.id}">${message(code: 'default.button.edit.label', default: 'Edit')}</g:link></span>
				</g:if>
				<g:if test="${studyInstance.isOwner(loggedInUser)}">
					<span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
				</g:if>
			</g:if>
			<span class="button"><g:link class="backToList" action="list">Back to list</g:link></span>
		</g:form>
	</div>
</div>
</body>
</html>