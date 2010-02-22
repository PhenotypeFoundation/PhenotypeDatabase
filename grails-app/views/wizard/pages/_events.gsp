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
	<wizard:selectElement name="eventDescription" description="Event Description" error="eventDescription" from="${eventDescriptions}" value="${eventDescription}">
		The event description for this event
	</wizard:selectElement>
	<wizard:timeElement name="startTime" description="Start Time" error="startTime" value="${startTime}">
		The start time of the study
	</wizard:timeElement>
	<wizard:timeElement name="endTime" description="End time" error="endTimee" value="${endTime}">
		The end time of the study
	</wizard:timeElement>
	<wizard:buttonElement name="add" value="Add" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()"/>
<g:if test="${events}">
	<div class="table">
		<div class="header">
			<div class="firstColumn">#</div>
			<div class="column">eventDescription</div>
			<div class="column">startTime</div>
			<div class="column">endTime</div>
			<div class="column">duration</div>
			<g:if test="${eventGroups}"><g:each var="eventGroup" status="i" in="${eventGroups}">
			<div class="column"><g:textField name="eventGroup_${i}_name" value="${eventGroup.name}" /></div>
			</g:each></g:if>
			<div class="column"><wizard:ajaxButton name="addEventGroup" value="+" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" /></div>
		</div>
	<g:each var="event" status="i" in="${events}">
		<div class="row">
			<div class="firstColumn">${i+1}</div>
			<div class="column">${event.eventDescription}</div>
			<div class="column"><g:formatDate format="dd/MM/yyyy hh:mm" date="${event.startTime}" /></div>
			<div class="column"><g:formatDate format="dd/MM/yyyy hh:mm" date="${event.endTime}" /></div>
			<div class="column">${event.getShortDuration()}</div>
			<g:if test="${eventGroups}"><g:each var="eventGroup" status="j" in="${eventGroups}">
			<div class="column"><input type="checkbox" name="event_${i}_group_${j}"/></div>
			</g:each></g:if>
			<div class="column"></div>
		</div>
	</g:each>
	</div>
</g:if>
</wizard:pageContent>