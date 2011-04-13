<%@ page import="dbnp.studycapturing.*" %>
<%@ page import="org.dbnp.gdt.*" %>
<g:if test="${studyList*.eventGroups?.flatten()?.size()==0}">
  No event groups in this study
</g:if>
<g:else>
  <%
	// Determine a union of the event templates for all different
	// eventgroups in all studies, in order to show a proper list.
	// We want every field to appear just once,
	// so the list is filtered for unique values
	def showTemplates = studyList*.giveAllEventTemplates()?.flatten().unique()

	def showProperties = [:];
	def allEvents = studyList*.events.flatten() + studyList*.samplingEvents.flatten();
	def eventColumns = 0;

	showTemplates.each { template ->
	  // We want to show all properties only once. If the properties are never filled
	  // we shouldn't show them at all.
	  def showFields = []
	  template.fields.each { field ->
		if( field.isFilledInList( allEvents.findAll { it.template == template } ) ) {
		  showFields << field;
		}
	  }

	  showProperties[ template.name ] = showFields;

	  // Compute the total number of columns under 'Events' (the +1 is
	  // because of the 'start time' column)
	  eventColumns += [ 1, showFields.size() + 1 ].max();
	}

  %>
  <table>
  <thead>
	<tr>
	  <g:if test="${multipleStudies}">
		<th></th>
	  </g:if>
	  <th>Name</th>
	  <th colspan="${eventColumns}">Events</th>
	  <th>Subjects</th>
	</tr>
	<tr>
	  <g:if test="${multipleStudies}">
		<th></th>
	  </g:if>
	  <th></th>
	  <g:each in="${showTemplates}" var="eventTemplate">
		<th colspan="${[1, showProperties[ eventTemplate.name ].size() + 1 ].max()}">${eventTemplate.name}</th>
	  </g:each>
	  <th></th>
	</tr>
	<tr class="templateFields">
	  <g:if test="${multipleStudies}">
		<th></th>
	  </g:if>
	  <th></th>
	  <g:each in="${showTemplates}" var="eventTemplate">
		<th>start time</th>
		<g:if test="${showProperties[ eventTemplate.name ].size() > 0}">
		  <g:each in="${showProperties[ eventTemplate.name ]}" var="field">
			<th>${field.name}</th>
		  </g:each>
		</g:if>
	  </g:each>
	  <th></th>
	</tr>
  </thead>

  <g:set var="i" value="${1}" />

  <g:each in="${studyList}" var="studyInstance">
	<%
	  // Sort the groups by name
	  def sortedEventGroups = studyInstance.eventGroups.sort( { a, b ->
		  return a.name <=> b.name;
	  }  as Comparator );

	  // Determine the number of rows per group (depending on the max
	  // number of events per template in a group)
	  def maxNumberEventsPerTemplate = [:];
	  def rowsPerStudy = 0;
	  sortedEventGroups.each { group ->
		def max = 1;
		showTemplates.each { template ->
		  def num = ( group.events + group.samplingEvents ).findAll { it.template == template }.size();
		  if( num > max )
			max = num;
		}
		maxNumberEventsPerTemplate[group.name] = max;
		rowsPerStudy += max;
	  }

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
	  <g:set var="n" value="${1}" />
	  <g:while test="${n <= maxNumberEventsPerTemplate[ eventGroup.name ]}">

		<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
		  <g:if test="${n == 1}">
			<g:if test="${multipleStudies && j==0}">
			  <td class="studytitle" rowspan="${rowsPerStudy}">
				${studyInstance.title}
			  </td>
			</g:if>
			<td rowspan="${maxNumberEventsPerTemplate[ eventGroup.name ]}">${eventGroup.name}</td>
		  </g:if>

		  <g:each in="${showTemplates}" var="currentEventTemplate">
			  <%
				def templateEvents = (eventGroup.events + eventGroup.samplingEvents).findAll { it.template == currentEventTemplate }.sort { a, b -> a.startTime <=> b.startTime }.asType(List)
				def event = templateEvents.size() >= n ? templateEvents[ n - 1 ] : null;
			  %>
			  <td class="templateFieldValue"><g:if test="${event}">${new RelTime( event.startTime ).toString()}</g:if></td>
			  <g:each in="${showProperties[ currentEventTemplate.name ]}" var="field">
				<td class="templateFieldValue"><af:showTemplateField field="${field}" entity="${event}" /></td>
			  </g:each>
		  </g:each>

		  <g:if test="${n == 1}">
			<% sortedGroupSubjects = eventGroup.subjects.sort( { a, b -> a.name <=> b.name } as Comparator )  %>

			<td rowspan="${maxNumberEventsPerTemplate[ eventGroup.name ]}" title="${sortedGroupSubjects.name.join( ', ' )}">
				<g:if test="${eventGroup.subjects.size()==0}">
					-
				</g:if>
				<g:else>
					<g:each in="${eventGroup.subjects.species.unique()}" var="currentSpecies" status="k">
						<g:if test="${k > 0}">,</g:if>
						<%=eventGroup.subjects.findAll { return it.species == currentSpecies; }.size() %>
						${currentSpecies}
					</g:each>
				</g:else>
			</td>
		  </g:if>
		</tr>


		<g:set var="n" value="${n+1}" />
	  </g:while>

	  <g:set var="i" value="${i + 1}" />
	</g:each>

  </g:each>

  </table>
</g:else>