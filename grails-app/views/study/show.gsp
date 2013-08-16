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
	
	<r:require modules="tiptip,timeline" />
	<script type="text/javascript">
		// Set a flag to indicate that the timeline has not been loaded yet
		var timelineLoaded = false;
		
		// Number of timelines that should be loaded
		var numTimelines = ${studyList?.size()};

		// This method is called on the event body.onLoad
		$(function() {
			$("#tabs").tabs({
				cache: true,
                show: function(event, ui) {
                  // If the events tab is shown, the timeline should be redrawn, if it has not been shown yet
                  if( ui.tab.hash == '#events-timeline' && !timelineLoaded ) {
						timelineLoaded = true;
						                      
                  		$.get( '<g:createLink action="timelineData" id="${studyList.id.join(',')}"/>', function( data ) {
							// Loop througbh all studies
                      		for( studyId in data ) {
                          		var container = document.getElementById( 'eventstimeline-' + studyId );
                          		if( container ) {
                      				var timeline = new links.Timeline( container );

                      				// Convert dates in the data into javascript date objects
                      				var studyEventGroups = $.map( data[ studyId ].eventGroups, function( el, idx ) {
                          				el.start = new Date( el.start );
                          				el.end = new Date( el.end );

                          				return el;
                          			});
                      				
                      				timeline.draw( studyEventGroups );
                          		}
                          	}
                      	})
                      	.fail( function() { 
                          	$( '#events-timeline' ).html( "Couldn't load this tab. We'll try to fix this as soon as possible."); 
                        });
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

	</script>
	<link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'studies.css')}"/>
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
				<li tab="study"><a href="#study">Study Information</a></li>
				<li tab="subjects"><a href="<g:createLink action="show_subjects" id="${studyList.id.join(',')}"/>" title="Subjects"><span>Subjects</span></a></li>
				<li tab="events"><a href="#events-timeline"><span>Events timeline</span></a></li>
				<li tab="events"><a href="<g:createLink action="show_events_table" id="${studyList.id.join(',')}"/>" title="Events table"><span>Events table</span></a></li>
				<li tab="assays"><a href="<g:createLink action="show_assays" id="${studyList.id.join(',')}"/>" title="Assays"><span>Assays</span></a></li>
				<li tab="samples"><a href="<g:createLink action="show_samples" id="${studyList.id.join(',')}"/>" title="Samples"><span>Samples</span></a></li>
				<li tab="study"><a href="<g:createLink action="show_persons" id="${studyList.id.join(',')}"/>" title="Persons"><span>Persons</span></a></li>
				<li tab="study"><a href="<g:createLink action="show_publications" id="${studyList.id.join(',')}"/>" title="Publications"><span>Publications</span></a></li>
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
											<af:showTemplateField field="${field}" entity="${studyInstance}"/>
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
				  <h4>${study.title}</h4>
				  <div class="eventstimeline" id="eventstimeline-${study.id}"></div>
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
					<span class="button">
						<g:link class="edit linktips" title="Edit this stuy" onclick="getTab(); return false;">
							${message(code: 'default.button.edit.label', default: 'Edit')}
						</g:link>
					</span>
					<%--<span class="button"><g:link class="edit linktips" title="Edit the basic properties of this study" controller="simpleWizard" action="index" id="${studyInstance?.id}">Simple edit</g:link></span>--%>
				</g:if>
				<g:if test="${studyInstance.isOwner(loggedInUser) || loggedInUser?.hasAdminRights()}">
					<span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')} WHOLE STUDY" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
				</g:if>
			</g:if>

			<script type="text/javascript">
				function getTab() {
					var tab = $('li.ui-state-active', $('ul.ui-tabs-nav')).attr('tab');
					var url = '<g:createLink controller="studyWizard" params="[jump:'edit']" id="${studyInstance?.id}" />&tab='+tab;
					document.location = url;
				}
			</script>

			<span class="button"><g:link class="backToList" action="list">Back to list</g:link></span>
		</g:form>
	</div>
</div>
</body>
</html>