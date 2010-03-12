<%
/**
 * Events page
 *
 * @author  Jeroen Wesbeek
 * @since   20100212
 * @package wizard
 * @see     dbnp.studycapturing.WizardTagLib::previousNext
 * @see     dbnp.studycapturing.WizardController
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<wizard:pageContent>
	<span class="info">
		<span class="title">Define all events and their duration that occur in your study</span>
		In the previous screen you defined the unique event types, in this screen you need to define
		all events of a specific event type that occur in time. Select the type of event, and the
		start and stop time of an event. As it is frequently the case that <i>sets</i> of events act
		upon (groups of) subjects, you can define event groups, and add events to a particular group.<br/>
		<i>Note that you can edit multiple events at once by selecting multpiple rows by either
		ctrl-clicking them or dragging a selection over them.</i>
	</span>

	<wizard:selectElement name="eventDescription" description="Event Description" error="eventDescription" from="${eventDescriptions}" value="${values?.eventDescription}">
		The event description for this event
	</wizard:selectElement>
	<wizard:timeElement name="startTime" description="Start Time" error="startTime" value="${values?.startTime}">
		The start time of the study
	</wizard:timeElement>
	<wizard:timeElement name="endTime" description="End time" error="endTimee" value="${values?.endTime}">
		The end time of the study
	</wizard:timeElement>
	<wizard:buttonElement name="add" value="Add" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()"/>
<g:if test="${events}">	
	<div class="table">
		<div class="header">
			<div class="firstColumn">#</div>
			<div class="firstColumn"></div>
			<div class="column">eventDescription</div>
			<div class="column">startTime</div>
			<div class="column">endTime</div>
			<div class="column">duration</div>
			<g:if test="${eventGroups}"><g:each var="eventGroup" status="i" in="${eventGroups}">
			<div class="column">
				<g:textField name="eventGroup_${i}_name" value="${eventGroup.name}" />
				<wizard:ajaxButton name="deleteEventGroup" src="../images/icons/famfamfam/delete.png" alt="delete this eventgroup" class="famfamfam" value="-" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" before="\$(\'input[name=do]\').val(${i});" afterSuccess="onWizardPage()" />
			</div>
			</g:each></g:if>
			<div class="column">
				<wizard:ajaxButton name="addEventGroup" src="../images/icons/famfamfam/add.png" alt="add a new eventgroup" class="famfamfam" value="+" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />
			</div>
		</div>
	<g:each var="event" status="i" in="${events}">
		<div class="row">
			<div class="firstColumn">${i+1}</div>
			<div class="firstColumn">
				<wizard:ajaxButton name="deleteEvent" src="../images/icons/famfamfam/delete.png" alt="delete this event" class="famfamfam" value="-" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" before="\$(\'input[name=do]\').val(${i});" afterSuccess="onWizardPage()" />				
			</div>
			<div class="column">${event.eventDescription}</div>
			<div class="column"><g:formatDate format="dd/MM/yyyy hh:mm" date="${event.startTime}" /></div>
			<div class="column"><g:formatDate format="dd/MM/yyyy hh:mm" date="${event.endTime}" /></div>
			<div class="column">${event.getShortDuration()}</div>
			<g:if test="${eventGroups}"><g:each var="eventGroup" status="j" in="${eventGroups}">
			<div class="column">
				<g:if test="${eventGroup.events.find{ it == event} }">
					<input type="checkbox" name="event_${i}_group_${j}" checked="checked" />
				</g:if><g:else>
					<input type="checkbox" name="event_${i}_group_${j}"/>
				</g:else>
			</div>
			</g:each></g:if>
			<div class="column"></div>
		</div>
	</g:each>
	</div>
	<div class="sliderContainer">
		<div class="slider"/>
	</div>
</g:if>

	<span class="info">
		<span class="todo">TODO</span>
		<ul>
			<li>parameter values should still be asked and implemented</li>
		</ul>
	</span>
</wizard:pageContent>